package com.mopl.realtime.moderation.dto;

import java.util.Objects;

public record MessageReviewResult(MessageReviewOutcome outcome, String content) {
    public MessageReviewResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(content, "content");
    }
    @Override public String toString() { return "MessageReviewResult[outcome=" + outcome + "]"; }
}
