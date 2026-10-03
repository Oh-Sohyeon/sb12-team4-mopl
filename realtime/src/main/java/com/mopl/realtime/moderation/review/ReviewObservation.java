package com.mopl.realtime.moderation.review;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 로그 연결에만 사용하며, 기한 판정이나 메트릭 라벨에는 사용하지 않는다. */
final class ReviewObservation {
    private static final Logger log = LoggerFactory.getLogger(ReviewObservation.class);
    private final String reviewId = UUID.randomUUID().toString();
    private final Instant deadline;
    private final long startedNanos = System.nanoTime();

    ReviewObservation(Instant deadline) { this.deadline = deadline; }

    static double elapsedMs(long start) { return (System.nanoTime() - start) / 1_000_000.0; }

    void event(String stage, String details) {
        // 관측 오류가 모더레이션 처리 결과에 영향을 주지 않도록 한다.
        try {
            Instant at = Instant.now();
            log.info("Moderation observation reviewId={} stage={} at={} deadline={} elapsedMs={} remainingMs={} {}",
                reviewId, stage, at, deadline, elapsedMs(startedNanos),
                Duration.between(at, deadline).toNanos() / 1_000_000.0, details);
        } catch (RuntimeException ignored) {
            // 로그 기록 실패는 무시하며, 메시지 내용이나 사용자 식별자는 포함하지 않는다.
        }
    }
}
