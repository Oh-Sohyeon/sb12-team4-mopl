package com.mopl.playlist.ai.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiPlaylistCandidateStore {

	private final ConcurrentHashMap<CandidateKey, Set<UUID>> candidatesBySession =
		new ConcurrentHashMap<>();

	public void saveCandidates(
		UUID userId,
		String sessionId,
		Set<UUID> contentIds
	) {
		CandidateKey key = new CandidateKey(userId, sessionId);

		candidatesBySession
			.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet())
			.addAll(contentIds);
	}

	public boolean containsAll(
		UUID userId,
		String sessionId,
		Set<UUID> contentIds
	) {
		CandidateKey key = new CandidateKey(userId, sessionId);
		Set<UUID> candidates = candidatesBySession.get(key);

		return candidates != null && candidates.containsAll(contentIds);
	}

	private record CandidateKey(
		UUID userId,
		String sessionId
	) {
	}
}