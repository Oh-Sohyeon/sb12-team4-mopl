package com.mopl.realtime.moderation.service;

import static org.assertj.core.api.Assertions.*;

import com.mopl.core.common.enums.*;
import com.mopl.core.domain.content.entity.Content;
import com.mopl.core.domain.message.entity.ContentChatMessage;
import com.mopl.core.domain.message.entity.Conversation;
import com.mopl.core.domain.message.entity.DirectMessage;
import com.mopl.core.domain.user.entity.User;
import com.mopl.realtime.moderation.config.ModerationProperties;
import com.mopl.realtime.moderation.exception.ModerationException;
import com.mopl.realtime.moderation.repository.MessageModerationLogRepository;
import com.mopl.realtime.moderation.support.ModerationTestSettings;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.ai.model.chat=none"})
@Import({ModerationLogService.class, ModerationContextService.class, ModerationPersistenceTest.Settings.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ModerationPersistenceTest {
    @TestConfiguration static class Settings {
        @Bean ModerationProperties properties() { return ModerationTestSettings.defaults(); }
    }
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactions;
    @Autowired ModerationLogService logs;
    @Autowired MessageModerationLogRepository repository;
    @Autowired ModerationContextService contexts;
    private UUID userId;
    private UUID contentId;
    private UUID conversationId;
    private TransactionTemplate tx;

    @BeforeEach void setup() {
        tx = new TransactionTemplate(transactions);
        tx.executeWithoutResult(status -> {
            User user = new User(UUID.randomUUID() + "@example.com", "password", "sender", null, UserRole.USER);
            User other = new User(UUID.randomUUID() + "@example.com", "password", "receiver", null, UserRole.USER);
            Content content = new Content(ContentType.MOVIE, "영화", "설명", null, ExternalSource.MANUAL,
                null, null, null, null, null);
            entityManager.persist(user); entityManager.persist(other); entityManager.persist(content);
            Conversation conversation = new Conversation(user, other); entityManager.persist(conversation);
            entityManager.persist(new ContentChatMessage(content, other, "최근 채팅"));
            entityManager.persist(new DirectMessage(conversation, other, user, "최근 DM"));
            userId = user.getId(); contentId = content.getId(); conversationId = conversation.getId();
        });
    }
    @Test void blockedMessageRollbackDoesNotLoseLogAndBothChannelsCountTogether() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            logs.record(userId, MessageType.DM, conversationId, "씨발", DetectionSource.RULE, ModerationCategory.PROFANITY, ModerationAction.MASK);
            throw new ModerationException(ModerationException.ErrorCode.MESSAGE_BLOCKED);
        })).isInstanceOf(ModerationException.class);
        logs.record(userId, MessageType.CONTENT_CHAT, contentId, "병신", DetectionSource.LLM, ModerationCategory.HARMFUL, ModerationAction.BLOCK);
        assertThat(logs.recentCount(userId)).isEqualTo(2);
        var records = repository.findBySender_IdAndDetectionSourceInAndModerationActionInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            userId, java.util.List.of(DetectionSource.RULE, DetectionSource.LLM),
            java.util.List.of(ModerationAction.MASK, ModerationAction.BLOCK), LocalDateTime.now().minusMinutes(10),
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(records).hasSize(2);
        assertThat(records).extracting(log -> log.getOriginalContent()).containsExactlyInAnyOrder("씨발", "병신");
        assertThat(records).extracting(log -> log.getDetectionSource()).containsExactlyInAnyOrder(DetectionSource.RULE, DetectionSource.LLM);
    }
    @Test void contextIsBoundedAndChannelSpecificWhileViolationsAreShared() {
        logs.record(userId, MessageType.DM, conversationId, "씨발", DetectionSource.RULE, ModerationCategory.PROFANITY, ModerationAction.MASK);
        var dm = contexts.load(userId, MessageType.DM, conversationId, "씨발");
        var chat = contexts.load(userId, MessageType.CONTENT_CHAT, contentId, "병신");
        assertThat(dm.recentConversation()).extracting(ModerationContextService.ContextMessage::text).containsExactly("최근 DM");
        assertThat(chat.recentConversation()).extracting(ModerationContextService.ContextMessage::text).containsExactly("최근 채팅");
        assertThat(chat.violations()).extracting(ModerationContextService.ContextMessage::text).containsExactly("씨발");
        assertThat(dm.violations()).hasSize(1);
    }
    @Test void confirmedRuleAndLlmViolationsReachThresholdTogether() {
        logs.record(userId, MessageType.DM, conversationId, "씨발", DetectionSource.RULE, ModerationCategory.PROFANITY, ModerationAction.MASK);
        logs.record(userId, MessageType.CONTENT_CHAT, contentId, "병신", DetectionSource.RULE, ModerationCategory.PROFANITY, ModerationAction.MASK);
        logs.record(userId, MessageType.DM, conversationId, "꺼져", DetectionSource.LLM, ModerationCategory.HARMFUL, ModerationAction.BLOCK);
        assertThat(logs.recentCount(userId)).isEqualTo(3);
        assertThat(contexts.load(userId, MessageType.DM, conversationId, "현재").violations()).hasSize(3);
    }
    @Test void allowedEvidenceCannotEnterCountOrSanctionContext() {
        tx.executeWithoutResult(status -> entityManager.persist(com.mopl.core.domain.message.entity.MessageModerationLog.forDirectMessage(
            entityManager.getReference(User.class,userId), entityManager.getReference(Conversation.class,conversationId),
            null, DetectionSource.LLM, ModerationCategory.HARMFUL, ModerationAction.ALLOW, "꺼져")));
        assertThat(logs.recentCount(userId)).isZero();
        assertThat(contexts.load(userId,MessageType.DM,conversationId,"현재").violations()).isEmpty();
        assertThatThrownBy(() -> logs.record(userId,MessageType.DM,conversationId,"현재",DetectionSource.LLM,
            ModerationCategory.HARMFUL,ModerationAction.ALLOW)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void messageContextIsBoundedAndDoesNotIncludeEvidence() {
        tx.executeWithoutResult(status -> {
            User user=entityManager.getReference(User.class,userId);
            Conversation conversation=entityManager.getReference(Conversation.class,conversationId);
            for(int i=0;i<15;i++) entityManager.persist(new DirectMessage(conversation,user,user,"과거 메시지"));
        });
        logs.record(userId,MessageType.DM,conversationId,"증거 전용",DetectionSource.LLM,ModerationCategory.HARMFUL,ModerationAction.BLOCK);
        var context=contexts.loadMessage(userId,MessageType.DM,conversationId,"현재");
        assertThat(context.currentMessage()).isEqualTo("현재");
        assertThat(context.recentConversation()).hasSize(10);
        assertThat(context.recentConversation()).extracting(com.mopl.realtime.moderation.dto.MessageReviewContext.ConversationMessage::text)
            .doesNotContain("증거 전용");
        var chat=contexts.loadMessage(userId,MessageType.CONTENT_CHAT,contentId,"현재");
        assertThat(chat.recentConversation()).extracting(com.mopl.realtime.moderation.dto.MessageReviewContext.ConversationMessage::text)
            .containsExactly("최근 채팅");
    }
    @Test void oldViolationDoesNotCount() {
        logs.record(userId, MessageType.DM, conversationId, "씨발", DetectionSource.RULE, ModerationCategory.PROFANITY, ModerationAction.MASK);
        tx.executeWithoutResult(status -> entityManager.createNativeQuery(
            "update message_moderation_logs set created_at = :old where sender_id = :sender")
            .setParameter("old", LocalDateTime.now().minusDays(1)).setParameter("sender", userId.toString()).executeUpdate());
        assertThat(logs.recentCount(userId)).isZero();
    }
}
