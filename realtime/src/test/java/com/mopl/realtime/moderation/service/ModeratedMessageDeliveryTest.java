package com.mopl.realtime.moderation.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.mopl.core.common.enums.*;
import com.mopl.core.domain.content.entity.Content;
import com.mopl.core.domain.message.entity.*;
import com.mopl.core.domain.user.entity.User;
import com.mopl.realtime.contentchat.controller.ContentChatWebSocketController;
import com.mopl.realtime.contentchat.dto.*;
import com.mopl.realtime.contentchat.repository.ContentChatMessageRepository;
import com.mopl.realtime.contentchat.repository.ContentRepository;
import com.mopl.realtime.contentchat.repository.UserRepository;
import com.mopl.realtime.contentchat.service.ContentChatService;
import com.mopl.realtime.directmessage.controller.DirectMessageWebSocketController;
import com.mopl.realtime.directmessage.dto.*;
import com.mopl.realtime.directmessage.repository.*;
import com.mopl.realtime.directmessage.service.DirectMessageService;
import com.mopl.realtime.moderation.dto.*;
import com.mopl.realtime.moderation.exception.ModerationException;
import com.mopl.realtime.moderation.repository.ChatRestrictionStore;
import com.mopl.realtime.moderation.review.*;
import com.mopl.realtime.moderation.rule.ProfanityRule;
import com.mopl.realtime.moderation.support.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
class ModeratedMessageDeliveryTest {
    private final UUID userId=UUID.randomUUID();
    private final UUID targetId=UUID.randomUUID();
    private final User sender=mock(User.class);
    private final User receiver=mock(User.class);
    private final Content content=mock(Content.class);
    private final Conversation conversation=mock(Conversation.class);
    private final ContentRepository contents=mock(ContentRepository.class);
    private final UserRepository users=mock(UserRepository.class);
    private final ContentChatMessageRepository chats=mock(ContentChatMessageRepository.class);
    private final ConversationRepository conversations=mock(ConversationRepository.class);
    private final DirectMessageRepository messages=mock(DirectMessageRepository.class);
    private final MessageReviewService messageReview=mock(MessageReviewService.class);
    private final ModerationLogService logs=mock(ModerationLogService.class);
    private final ModerationReviewDispatcher sanctions=mock(ModerationReviewDispatcher.class);
    private final ChatRestrictionStore restrictions=mock(ChatRestrictionStore.class);
    private final SimpMessagingTemplate broadcasts=mock(SimpMessagingTemplate.class);
    private ContentChatWebSocketController chatController;
    private DirectMessageWebSocketController dmController;
    @BeforeEach void setup() {
        when(sender.getId()).thenReturn(userId);
        when(receiver.getId()).thenReturn(UUID.randomUUID());
        when(conversation.getId()).thenReturn(targetId);
        when(conversation.getUser1()).thenReturn(sender);when(conversation.getUser2()).thenReturn(receiver);
        when(contents.findByIdAndDeletedAtIsNull(targetId)).thenReturn(Optional.of(content));
        when(users.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(sender));
        when(conversations.findWithParticipantsById(targetId)).thenReturn(Optional.of(conversation));
        when(chats.save(any(ContentChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messages.save(any(DirectMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var settings=ModerationTestSettings.defaults();
        var moderation=new MessageModerationService(restrictions,new ProfanityRule(settings,MessageReviewTestSettings.defaults()),
            messageReview,logs,sanctions,settings,new SimpleMeterRegistry());
        chatController=new ContentChatWebSocketController(new ContentChatService(contents,users,chats,moderation),broadcasts);
        dmController=new DirectMessageWebSocketController(new DirectMessageService(conversations,messages,moderation),broadcasts);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void ruleMasksBeforePersistenceAndBroadcastWhileEvidenceRetainsOriginal(MessageType type) {
        send(type,"너는 병신!");
        assertDelivery(type,"너는 **!");
        verify(logs).record(userId,type,targetId,"너는 병신!",DetectionSource.RULE,ModerationCategory.PROFANITY,ModerationAction.MASK);
        verifyNoInteractions(messageReview);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void llmMaskPersistsAndBroadcastsValidatedContent(MessageType type) {
        when(messageReview.review(userId,type,targetId,"너는 멍청이!"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.MASK,"너는 *!"));
        send(type,"너는 멍청이!");assertDelivery(type,"너는 *!");
        verify(logs).record(userId,type,targetId,"너는 멍청이!",DetectionSource.LLM,ModerationCategory.HARMFUL,ModerationAction.MASK);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void failOpenPersistsAndBroadcastsOriginalWithoutViolation(MessageType type) {
        when(messageReview.review(userId,type,targetId,"꺼져"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.FAIL_OPEN_TIMEOUT,"꺼져"));
        send(type,"꺼져");assertDelivery(type,"꺼져");verifyNoInteractions(logs,sanctions);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void llmBlockDoesNotPersistOrBroadcast(MessageType type) {
        when(messageReview.review(userId,type,targetId,"꺼져"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.BLOCK,"꺼져"));
        assertThatThrownBy(() -> send(type,"꺼져")).isInstanceOf(ModerationException.class);
        verify(chats,never()).save(any());verify(messages,never()).save(any());verifyNoInteractions(broadcasts);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void restrictionStillBlocksBeforeContextAndPersistence(MessageType type) {
        when(restrictions.restricted(userId)).thenReturn(true);
        assertThatThrownBy(() -> send(type,"정상 메시지")).isInstanceOf(ModerationException.class);
        verify(chats,never()).save(any());verify(messages,never()).save(any());
        verifyNoInteractions(broadcasts,messageReview,logs,sanctions);
    }
    private void send(MessageType type,String text) {
        Principal principal=() -> userId.toString();
        if(type==MessageType.DM) dmController.send(targetId,new DirectMessageSendRequest(text),principal);
        else chatController.send(targetId,new ContentChatSendRequest(text),principal);
    }
    private void assertDelivery(MessageType type,String expected) {
        if(type==MessageType.DM) {
            var saved=ArgumentCaptor.forClass(DirectMessage.class);verify(messages).save(saved.capture());
            assertThat(saved.getValue().getContent()).isEqualTo(expected);
            var response=ArgumentCaptor.forClass(DirectMessageResponse.class);
            verify(broadcasts).convertAndSend(eq("/sub/conversations/"+targetId+"/direct-messages"),response.capture());
            assertThat(response.getValue().content()).isEqualTo(expected);
        } else {
            var saved=ArgumentCaptor.forClass(ContentChatMessage.class);verify(chats).save(saved.capture());
            assertThat(saved.getValue().getMessage()).isEqualTo(expected);
            var response=ArgumentCaptor.forClass(ContentChatResponse.class);
            verify(broadcasts).convertAndSend(eq("/sub/contents/"+targetId+"/chat"),response.capture());
            assertThat(response.getValue().content()).isEqualTo(expected);
        }
    }
}
