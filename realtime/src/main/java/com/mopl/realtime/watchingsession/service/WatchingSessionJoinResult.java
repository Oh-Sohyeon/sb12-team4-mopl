package com.mopl.realtime.watchingsession.service;

import com.mopl.core.domain.watchingsession.model.WatchingSessionState;

import java.util.Optional;

public record WatchingSessionJoinResult(
	WatchingSessionState joinedSession,
	Optional<WatchingSessionState> leftSession
) {
}