package com.mopl.realtime.moderation.dto;

public enum MessageReviewOutcome {
    ALLOW, MASK, BLOCK, FAIL_OPEN_TIMEOUT, FAIL_OPEN_ERROR, FAIL_OPEN_INVALID_RESPONSE, FAIL_OPEN_QUEUE_FULL;

    public boolean failOpen() {
        return switch (this) {
            case ALLOW, MASK, BLOCK -> false;
            default -> true;
        };
    }
}
