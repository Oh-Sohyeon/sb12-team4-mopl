package com.mopl.playlist.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AiPlaylistCandidateStore {

	private static final String KEY_PREFIX =
		"ai:playlist:candidates:";

	private final StringRedisTemplate redisTemplate;

	public void saveCandidates(
		UUID userId,
		String sessionId,
		Set<UUID> contentIds
	) {
		if (contentIds.isEmpty()) {
			return;
		}

		String key = buildKey(userId, sessionId);

		String[] values = contentIds.stream()
			.map(UUID::toString)
			.toArray(String[]::new);

		redisTemplate.opsForSet().add(key, values);
	}

	public boolean containsAll(
		UUID userId,
		String sessionId,
		Set<UUID> contentIds
	) {
		if (contentIds.isEmpty()) {
			return false;
		}

		String key = buildKey(userId, sessionId);

		Object[] values = contentIds.stream()
			.map(UUID::toString)
			.toArray();

		Map<Object, Boolean> membership =
			redisTemplate.opsForSet().isMember(key, values);

		return membership != null
			&& membership.size() == contentIds.size()
			&& membership.values().stream()
			.allMatch(Boolean.TRUE::equals);
	}

	private String buildKey(
		UUID userId,
		String sessionId
	) {
		return KEY_PREFIX
			+ userId
			+ ":"
			+ sessionId;
	}
}