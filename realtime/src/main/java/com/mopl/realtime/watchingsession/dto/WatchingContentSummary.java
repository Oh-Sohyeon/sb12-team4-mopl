package com.mopl.realtime.watchingsession.dto;

import com.mopl.core.common.enums.ContentType;

import java.util.List;
import java.util.UUID;

public record WatchingContentSummary(
	UUID id,
	ContentType type,
	String title,
	String description,
	String thumbnailUrl,
	List<String> tags,
	double averageRating,
	int reviewCount
) {
	// TODO: API와 Realtime의 ContentSummary 데이터 조립 로직 공통화 검토
	// (tags, averageRating, reviewCount 중복 조회/집계 방지)
}