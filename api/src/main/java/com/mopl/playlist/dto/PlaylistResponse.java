package com.mopl.playlist.dto;

import com.mopl.core.domain.playlist.entity.Playlist;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
	UUID id,
	UUID ownerId,
	String ownerName,
	String ownerProfileImageUrl,
	String title,
	String description,
	LocalDateTime updatedAt,
	long subscriberCount,
	boolean subscribedByMe,
	List<Object> contents // TODO: 콘텐츠 연결 이슈에서 ContentSummary로 교체
) {
	public static PlaylistResponse from(Playlist playlist) {
		return new PlaylistResponse(
			playlist.getId(),
			playlist.getOwner().getId(),
			playlist.getOwner().getName(),
			playlist.getOwner().getProfileImageUrl(),
			playlist.getTitle(),
			playlist.getDescription(),
			playlist.getUpdatedAt(),
			0L,     // TODO: 구독 이슈에서 실제 집계로 교체
			false,  // TODO: 구독 이슈에서 실제 값으로 교체
			Collections.emptyList() // TODO: 콘텐츠 연결 이슈에서 실제 값으로 교체
		);
	}
}