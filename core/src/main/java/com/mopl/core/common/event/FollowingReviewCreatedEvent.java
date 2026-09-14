package com.mopl.core.common.event;

import java.util.UUID;

public record FollowingReviewCreatedEvent(
    UUID reviewerId,
    String contentTitle
) {
}