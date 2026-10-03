package com.mopl.realtime.moderation.review;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.mopl.core.common.enums.*;
import com.mopl.realtime.moderation.dto.*;
import com.mopl.realtime.moderation.exception.InvalidMessageReviewResponseException;
import com.mopl.realtime.moderation.rule.MessageMasker;
import com.mopl.realtime.moderation.service.ModerationContextService;
import com.mopl.realtime.moderation.support.MessageReviewTestSettings;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
class MessageReviewServiceTest {
    private final ModerationContextService contexts = mock(ModerationContextService.class);
    private final UUID user = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();
    private final String text = "너는 멍청이!";
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @BeforeEach void setup() {
        when(contexts.loadMessage(user,MessageType.DM,target,text)).thenReturn(new MessageReviewContext(text,List.of()));
    }
    @AfterEach void close() throws InterruptedException {
        executor.shutdownNow();
        assertThat(executor.awaitTermination(2,TimeUnit.SECONDS)).isTrue();
        meters.close();
    }
    private MessageReviewService service(MessageReviewer reviewer,Duration timeout) {
        return new MessageReviewService(contexts,reviewer,new MessageMasker(),
            MessageReviewTestSettings.withTimeout(timeout),meters,executor);
    }
    @ParameterizedTest @EnumSource(ModerationAction.class)
    void explicitDecisionsAreValidatedAndMeasured(ModerationAction action) {
        var ranges = action == ModerationAction.MASK ? List.of(new MaskRange(3,6)) : List.<MaskRange>of();
        var result = service(context -> new MessageReviewDecision(action,ranges),Duration.ofSeconds(1))
            .review(user,MessageType.DM,target,text);
        assertThat(result.outcome().name()).isEqualTo(action.name());
        assertThat(result.content()).isEqualTo(action==ModerationAction.MASK ? "너는 *!" : text);
        assertThat(meters.get("moderation.message.llm.calls").counter().count()).isEqualTo(1);
        assertThat(meters.get("moderation.message.reviews").tag("outcome",action.name().toLowerCase(java.util.Locale.ROOT))
            .counter().count()).isEqualTo(1);
    }
    @Test void timeoutReturnsOriginalAndDiscardsLateDecision() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var done = new CountDownLatch(1);
        MessageReviewer reviewer = context -> {
            started.countDown();
            try { while (!release.await(5,TimeUnit.MILLISECONDS)) { } }
            catch (InterruptedException exception) {
                try { release.await(); } catch (InterruptedException repeated) { Thread.currentThread().interrupt(); }
            }
            done.countDown();
            return new MessageReviewDecision(ModerationAction.BLOCK,List.of());
        };
        try {
            var result = service(reviewer,Duration.ofMillis(100)).review(user,MessageType.DM,target,text);
            assertThat(started.await(1,TimeUnit.SECONDS)).isTrue();
            assertFailOpen(result,MessageReviewOutcome.FAIL_OPEN_TIMEOUT);
        } finally { release.countDown(); }
        assertThat(done.await(1,TimeUnit.SECONDS)).isTrue();
        assertThat(meters.find("moderation.message.reviews").tag("outcome","block").counter()).isNull();
    }
    @Test void modelErrorIsDistinctFromTimeoutAndAllow() {
        assertFailOpen(service(context -> { throw new IllegalStateException("model error"); },Duration.ofSeconds(1))
            .review(user,MessageType.DM,target,text),MessageReviewOutcome.FAIL_OPEN_ERROR);
    }
    @Test void transportTimeoutIsObservedSeparatelyFromModelError() {
        assertFailOpen(service(context -> { throw new IllegalStateException(new java.net.SocketTimeoutException()); },
            Duration.ofSeconds(1)).review(user,MessageType.DM,target,text),MessageReviewOutcome.FAIL_OPEN_TIMEOUT);
    }
    @Test void parsingFailureFailsOpenAsInvalidResponse() {
        assertFailOpen(service(context -> { throw new InvalidMessageReviewResponseException(); },Duration.ofSeconds(1))
            .review(user,MessageType.DM,target,text),MessageReviewOutcome.FAIL_OPEN_INVALID_RESPONSE);
    }
    @Test void invalidMaskDoesNotBecomeWholeMaskOrBlock() {
        assertFailOpen(service(context -> new MessageReviewDecision(ModerationAction.MASK,List.of(new MaskRange(-1,100))),
            Duration.ofSeconds(1)).review(user,MessageType.DM,target,text),MessageReviewOutcome.FAIL_OPEN_INVALID_RESPONSE);
    }
    @Test void emptyMaskAndUnexpectedRangesAreInvalid() {
        for (ModerationAction action : ModerationAction.values()) {
            List<MaskRange> ranges = action==ModerationAction.MASK ? List.of() : List.of(new MaskRange(0,1));
            assertFailOpen(service(context -> new MessageReviewDecision(action,ranges),Duration.ofSeconds(1))
                .review(user,MessageType.DM,target,text),MessageReviewOutcome.FAIL_OPEN_INVALID_RESPONSE);
        }
    }
    @Test void nullDecisionFailsOpen() {
        assertFailOpen(service(context -> null,Duration.ofSeconds(1)).review(user,MessageType.DM,target,text),
            MessageReviewOutcome.FAIL_OPEN_INVALID_RESPONSE);
    }
    @Test void exhaustedExecutorFailsOpenWithoutCallingModel() {
        executor.shutdown();
        MessageReviewer reviewer = mock(MessageReviewer.class);
        assertFailOpen(service(reviewer,Duration.ofSeconds(1)).review(user,MessageType.DM,target,text),
            MessageReviewOutcome.FAIL_OPEN_QUEUE_FULL);
        verifyNoInteractions(reviewer);
    }
    @Test void queueWaitAndContextTimeConsumeMessageBudget() throws Exception {
        var release = new CountDownLatch(1);
        executor.submit(() -> { release.await(); return null; });
        MessageReviewer reviewer = mock(MessageReviewer.class);
        try {
            assertFailOpen(service(reviewer,Duration.ofMillis(30)).review(user,MessageType.DM,target,text),
                MessageReviewOutcome.FAIL_OPEN_TIMEOUT);
        } finally { release.countDown(); }
        verifyNoInteractions(reviewer);
        verify(contexts,never()).loadMessage(any(),any(),any(),any());
    }
    private void assertFailOpen(MessageReviewResult result,MessageReviewOutcome outcome) {
        assertThat(result.outcome()).isEqualTo(outcome);
        assertThat(result.content()).isEqualTo(text);
        assertThat(meters.get("moderation.message.reviews").tag("outcome",outcome.name().toLowerCase(java.util.Locale.ROOT))
            .counter().count()).isGreaterThanOrEqualTo(1);
        assertThat(meters.find("moderation.message.reviews").tag("outcome","allow").counter()).isNull();
    }
}
