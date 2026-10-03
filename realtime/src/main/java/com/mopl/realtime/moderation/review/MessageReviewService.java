package com.mopl.realtime.moderation.review;

import com.mopl.core.common.enums.MessageType;
import com.mopl.core.common.enums.ModerationAction;
import com.mopl.realtime.moderation.config.MessageReviewProperties;
import com.mopl.realtime.moderation.dto.MessageReviewOutcome;
import com.mopl.realtime.moderation.dto.MessageReviewResult;
import com.mopl.realtime.moderation.exception.InvalidMessageReviewResponseException;
import com.mopl.realtime.moderation.rule.MessageMasker;
import com.mopl.realtime.moderation.service.ModerationContextService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MessageReviewService {
    private final ModerationContextService contexts;
    private final MessageReviewer reviewer;
    private final MessageMasker masker;
    private final MessageReviewProperties properties;
    private final MeterRegistry meters;
    private final ExecutorService executor;

    public MessageReviewService(ModerationContextService contexts, MessageReviewer reviewer, MessageMasker masker,
        MessageReviewProperties properties, MeterRegistry meters,
        @Qualifier("moderationMessageExecutor") ExecutorService executor) {
        this.contexts = contexts; this.reviewer = reviewer; this.masker = masker;
        this.properties = properties; this.meters = meters; this.executor = executor;
    }

    private static boolean isTimeout(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof TimeoutException || cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) {
                return true;
            }
            if (cause.getCause() == cause) break;
        }
        return false;
    }

    public MessageReviewResult review(UUID senderId, MessageType type, UUID targetId, String text) {
        long deadlineNanos = System.nanoTime() + properties.timeout().toNanos();
        var observation = new ReviewObservation(Instant.now().plus(properties.timeout()));
        observation.event("message_review_started", "");
        Timer.Sample timer = Timer.start(meters);
        MessageReviewOutcome outcome = MessageReviewOutcome.FAIL_OPEN_ERROR;
        Future<MessageReviewResult> task = null;
        try {
            task = executor.submit(() -> {
                var context = contexts.loadMessage(senderId, type, targetId, text);
                if (System.nanoTime() - deadlineNanos >= 0) throw new TimeoutException();
                meters.counter("moderation.message.llm.calls").increment();
                observation.event("message_llm_started", "contextSize=" + context.recentConversation().size());
                var sample = Timer.start(meters);
                try {
                    var decision = reviewer.review(context);
                    if (decision == null) throw new InvalidMessageReviewResponseException();
                    if (decision.action() == ModerationAction.MASK) {
                        return new MessageReviewResult(MessageReviewOutcome.MASK, masker.mask(text, decision.maskRanges()));
                    }
                    if (!decision.maskRanges().isEmpty()) throw new InvalidMessageReviewResponseException();
                    return new MessageReviewResult(decision.action() == ModerationAction.ALLOW
                        ? MessageReviewOutcome.ALLOW : MessageReviewOutcome.BLOCK, text);
                } finally { sample.stop(meters.timer("moderation.message.llm")); }
            });
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) throw new TimeoutException();
            MessageReviewResult result = task.get(remaining, TimeUnit.NANOSECONDS);
            outcome = result.outcome();
            return result;
        } catch (TimeoutException exception) {
            outcome = MessageReviewOutcome.FAIL_OPEN_TIMEOUT;
        } catch (ExecutionException exception) {
            if (isTimeout(exception.getCause())) outcome = MessageReviewOutcome.FAIL_OPEN_TIMEOUT;
            else if (exception.getCause() instanceof InvalidMessageReviewResponseException) {
                outcome = MessageReviewOutcome.FAIL_OPEN_INVALID_RESPONSE;
            }
        } catch (RejectedExecutionException exception) {
            outcome = MessageReviewOutcome.FAIL_OPEN_QUEUE_FULL;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException exception) {
            outcome = MessageReviewOutcome.FAIL_OPEN_ERROR;
        } finally {
            // baseline의 fail-open은 확정 위반이 아니며, 늦은 worker 결과에는 저장 등의 부수 효과가 없다.
            if (task != null && !task.isDone()) task.cancel(true);
            String label = outcome.name().toLowerCase(Locale.ROOT);
            meters.counter("moderation.message.reviews", "outcome", label).increment();
            timer.stop(meters.timer("moderation.message.review", "outcome", label));
            observation.event("message_review_finished", "outcome=" + label);
        }
        return new MessageReviewResult(outcome, text);
    }
}
