package com.mopl.core.common.event;

import java.util.UUID;

public record FollowingWatchStartedEvent(
    UUID watcherId,
    String contentTitle
) {
}