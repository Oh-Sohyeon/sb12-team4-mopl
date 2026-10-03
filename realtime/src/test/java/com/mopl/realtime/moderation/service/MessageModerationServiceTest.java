package com.mopl.realtime.moderation.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.mopl.core.common.enums.*;
import com.mopl.realtime.moderation.dto.*;
import com.mopl.realtime.moderation.exception.ModerationException;
import com.mopl.realtime.moderation.repository.ChatRestrictionStore;
import com.mopl.realtime.moderation.review.MessageReviewService;
import com.mopl.realtime.moderation.review.ModerationReviewDispatcher;
import com.mopl.realtime.moderation.rule.ProfanityRule;
import com.mopl.realtime.moderation.support.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class MessageModerationServiceTest {
    @Mock ChatRestrictionStore restrictions;
    @Mock ModerationLogService logs;
    @Mock ModerationReviewDispatcher reviews;
    @Mock MessageReviewService messageReviews;
    private MessageModerationService service;
    private SimpleMeterRegistry meters;
    private final UUID user = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();
    @BeforeEach void setup() {
        var settings = ModerationTestSettings.defaults();
        meters = new SimpleMeterRegistry();
        service = new MessageModerationService(restrictions, new ProfanityRule(settings, MessageReviewTestSettings.defaults()),
            messageReviews, logs, reviews, settings, meters);
    }
    @Test void normalMessageSkipsLogsAndBothAiRoles() {
        service.assertCanSend(user);
        assertThat(service.inspect(user, MessageType.DM, target, "안녕하세요")).isEqualTo("안녕하세요");
        verifyNoInteractions(logs, reviews, messageReviews);
        assertThat(meters.get("moderation.message.received").counter().count()).isEqualTo(1);
    }
    @Test void ruleMaskPersistsRawEvidenceAndSkipsMessageAi() {
        when(logs.recentCount(user)).thenReturn(2L);
        assertThat(service.inspect(user, MessageType.DM, target, "너는 병신!")).isEqualTo("너는 **!");
        verify(logs).record(user, MessageType.DM, target, "너는 병신!", DetectionSource.RULE,
            ModerationCategory.PROFANITY, ModerationAction.MASK);
        verifyNoInteractions(reviews, messageReviews);
    }
    @ParameterizedTest @EnumSource(MessageType.class)
    void thresholdSchedulesExistingSanctionReviewerInBothChannels(MessageType type) {
        when(logs.recentCount(user)).thenReturn(3L);
        assertThat(service.inspect(user, type, target, "씨발")).isEqualTo("**");
        verify(reviews).submit(user, type, target, "씨발");
    }
    @Test void llmAllowNeverCountsAsViolation() {
        when(messageReviews.review(user, MessageType.DM, target, "꺼져"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.ALLOW, "꺼져"));
        assertThat(service.inspect(user, MessageType.DM, target, "꺼져")).isEqualTo("꺼져");
        verifyNoInteractions(logs, reviews);
    }
    @ParameterizedTest @EnumSource(value=MessageReviewOutcome.class, names={"FAIL_OPEN_TIMEOUT","FAIL_OPEN_ERROR",
        "FAIL_OPEN_INVALID_RESPONSE","FAIL_OPEN_QUEUE_FULL"})
    void failOpenPreservesOriginalWithoutEvidenceOrThreshold(MessageReviewOutcome outcome) {
        when(messageReviews.review(user, MessageType.DM, target, "꺼져"))
            .thenReturn(new MessageReviewResult(outcome, "꺼져"));
        assertThat(service.inspect(user, MessageType.DM, target, "꺼져")).isEqualTo("꺼져");
        verifyNoInteractions(logs, reviews);
        assertThat(meters.get("moderation.messages").tag("outcome","fail_open").counter().count()).isEqualTo(1);
        assertThat(meters.find("moderation.messages").tag("outcome","allowed").counter()).isNull();
    }
    @Test void llmMaskUsesOriginalEvidenceAndServerMaskedContent() {
        when(messageReviews.review(user, MessageType.DM, target, "너는 멍청이!"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.MASK, "너는 *!"));
        when(logs.recentCount(user)).thenReturn(3L);
        assertThat(service.inspect(user, MessageType.DM, target, "너는 멍청이!")).isEqualTo("너는 *!");
        verify(logs).record(user,MessageType.DM,target,"너는 멍청이!",DetectionSource.LLM,ModerationCategory.HARMFUL,ModerationAction.MASK);
        verify(reviews).submit(user,MessageType.DM,target,"너는 멍청이!");
    }
    @Test void llmBlockRecordsEvidenceThenBlocksAndSchedulesSanction() {
        when(messageReviews.review(user,MessageType.CONTENT_CHAT,target,"꺼져"))
            .thenReturn(new MessageReviewResult(MessageReviewOutcome.BLOCK,"꺼져"));
        when(logs.recentCount(user)).thenReturn(3L);
        assertThatThrownBy(() -> service.inspect(user,MessageType.CONTENT_CHAT,target,"꺼져"))
            .isInstanceOf(ModerationException.class);
        verify(logs).record(user,MessageType.CONTENT_CHAT,target,"꺼져",DetectionSource.LLM,ModerationCategory.HARMFUL,ModerationAction.BLOCK);
        verify(reviews).submit(user,MessageType.CONTENT_CHAT,target,"꺼져");
    }
    @Test void dispatchFailureKeepsConfirmedMask() {
        when(logs.recentCount(user)).thenReturn(3L);
        doThrow(new IllegalStateException("unavailable")).when(reviews).submit(user,MessageType.DM,target,"씨발");
        assertThat(service.inspect(user,MessageType.DM,target,"씨발")).isEqualTo("**");
        assertThat(meters.get("moderation.reviews").tag("outcome","dispatch_error").counter().count()).isEqualTo(1);
    }
    @Test void restrictionBlocksBeforeRuleOrLogWork() {
        when(restrictions.restricted(user)).thenReturn(true);
        assertThatThrownBy(() -> service.assertCanSend(user)).isInstanceOf(ModerationException.class)
            .extracting(e -> ((ModerationException)e).getErrorCode()).isEqualTo(ModerationException.ErrorCode.CHAT_RESTRICTED);
        verifyNoInteractions(logs,reviews,messageReviews);
    }
}
