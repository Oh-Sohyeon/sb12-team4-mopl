package com.mopl.core.common.event;

import java.util.UUID;

public record FollowingPlaylistCreatedEvent(
    UUID creatorId,
    String playlistTitle
) {
}