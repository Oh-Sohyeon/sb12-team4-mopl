package com.mopl.ai.playlist.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiPlaylistCandidateStore {

	private final ConcurrentHashMap<String, Set<UUID>> candidatesByConversation =
		new ConcurrentHashMap<>();

	public void saveCandidates(String conversationId, Set<UUID> contentIds) {
		candidatesByConversation
			.computeIfAbsent(conversationId, key -> ConcurrentHashMap.newKeySet())
			.addAll(contentIds);
	}

	public boolean containsAll(String conversationId, Set<UUID> contentIds) {
		Set<UUID> candidates = candidatesByConversation.get(conversationId);

		return candidates != null && candidates.containsAll(contentIds);
	}
}