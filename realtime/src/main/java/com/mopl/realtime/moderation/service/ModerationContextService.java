package com.mopl.realtime.moderation.service;

import com.mopl.core.common.enums.DetectionSource;
import com.mopl.core.common.enums.MessageType;
import com.mopl.core.common.enums.ModerationAction;
import com.mopl.realtime.moderation.dto.MessageReviewContext;
import com.mopl.realtime.contentchat.repository.ContentChatMessageRepository;
import com.mopl.realtime.directmessage.repository.DirectMessageRepository;
import com.mopl.realtime.moderation.config.ModerationProperties;
import com.mopl.realtime.moderation.repository.MessageModerationLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationContextService {
    private final MessageModerationLogRepository logs;
    private final ContentChatMessageRepository chats;
    private final DirectMessageRepository messages;
    private final ModerationProperties properties;

    @Transactional(readOnly = true)
    public ReviewContext load(UUID userId, MessageType type, UUID targetId, String currentMessage) {
        var page = PageRequest.of(0, properties.contextLimit());
        var since = LocalDateTime.now().minus(properties.violationWindow());
        var violations = logs.findBySender_IdAndDetectionSourceInAndModerationActionInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            userId, List.of(DetectionSource.RULE, DetectionSource.LLM),
            List.of(ModerationAction.MASK, ModerationAction.BLOCK), since, page).stream()
            .map(log -> new ContextMessage(userId, log.getCreatedAt(), log.getOriginalContent())).toList();
        var recent = recent(type, targetId);
        return new ReviewContext(userId, type, currentMessage, recent, violations);
    }

    @Transactional(readOnly = true)
    public MessageReviewContext loadMessage(UUID userId, MessageType type, UUID targetId, String currentMessage) {
        var recent = recent(type, targetId).stream()
            .map(message -> new MessageReviewContext.ConversationMessage(message.senderId().equals(userId), message.text())).toList();
        return new MessageReviewContext(currentMessage, recent);
    }

    private List<ContextMessage> recent(MessageType type, UUID targetId) {
        var page = PageRequest.of(0, properties.contextLimit());
        return type == MessageType.DM
            ? messages.findByConversation_IdOrderByCreatedAtDesc(targetId, page).stream()
                .map(message -> new ContextMessage(message.getSender().getId(), message.getCreatedAt(), message.getContent())).toList()
            : chats.findByContent_IdOrderByCreatedAtDesc(targetId, page).stream()
                .map(message -> new ContextMessage(message.getSender().getId(), message.getCreatedAt(), message.getMessage())).toList();
    }

    public record ContextMessage(UUID senderId, LocalDateTime createdAt, String text) {}
    public record ReviewContext(UUID reviewedUserId, MessageType messageType, String currentMessage,
                                List<ContextMessage> recentConversation, List<ContextMessage> violations) {}
}
