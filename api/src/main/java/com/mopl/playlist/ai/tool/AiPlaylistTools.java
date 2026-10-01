package com.mopl.playlist.ai.tool;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import com.mopl.common.exception.CommonErrorCode;
import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.playlist.PlaylistAiContentNotSearchedException;
import com.mopl.playlist.ai.service.AiPlaylistCandidateStore;
import com.mopl.playlist.dto.PlaylistCreateRequest;
import com.mopl.playlist.dto.PlaylistResponse;
import com.mopl.playlist.service.PlaylistService;
import org.springframework.ai.chat.model.ToolContext;
import com.mopl.content.search.service.SemanticCandidateSearchService;
import com.mopl.content.search.condition.SemanticCandidateCondition;
import com.mopl.content.search.dto.ContentCandidate;
import com.mopl.core.common.enums.ContentType;
import com.mopl.recommendation.dto.RecommendationItem;
import com.mopl.recommendation.service.RecommendationService;


import java.util.UUID;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AiPlaylistTools {

	private final PlaylistService playlistService;
	private final SemanticCandidateSearchService semanticCandidateSearchService;
	private final AiPlaylistCandidateStore aiPlaylistCandidateStore;
	private final RecommendationService recommendationService;

	@Tool(description = """
    사용자가 원하는 조건에 맞는 MOPL의 실제 콘텐츠를 찾을 때 사용합니다.
    자연어 요청을 queryText로 전달하세요.
    영화나 TV 등 콘텐츠 종류를 명확하게 요청한 경우에만 type을 지정하고,
    종류를 지정하지 않았다면 type은 null로 전달하세요.
    """)
	public List<ContentCandidate> searchContents(
		String queryText,
		ContentType type,
		ToolContext toolContext
	) {
		SemanticCandidateCondition condition =
			new SemanticCandidateCondition(type, List.of());

		List<ContentCandidate> candidates = semanticCandidateSearchService.search(
			queryText,
			condition,
			10
		);

		UUID currentUserId =
			(UUID) toolContext.getContext().get("currentUserId");

		String sessionId =
			(String) toolContext.getContext().get("sessionId");

		aiPlaylistCandidateStore.saveCandidates(
			currentUserId,
			sessionId,
			candidates.stream()
				.map(ContentCandidate::contentId)
				.collect(java.util.stream.Collectors.toSet())
		);

		return candidates;
	}

	@Tool(description = """
        사용자가 자신의 취향, 과거 시청 이력, 평가 기록을 기반으로
        콘텐츠를 추천해달라고 요청할 때 사용합니다.
        일반적인 조건이나 분위기를 기준으로 콘텐츠를 찾는 경우에는
        searchContents를 사용하세요.
        """)
	public List<RecommendationItem> recommendContentsForUser(
		ToolContext toolContext
	) {
		UUID currentUserId =
			(UUID) toolContext.getContext().get("currentUserId");

		String sessionId =
			(String) toolContext.getContext().get("sessionId");

		List<RecommendationItem> recommendations =
			recommendationService.getRecommendations(currentUserId);

		aiPlaylistCandidateStore.saveCandidates(
			currentUserId,
			sessionId,
			recommendations.stream()
				.map(RecommendationItem::contentId)
				.collect(java.util.stream.Collectors.toSet())
		);

		return recommendations;
	}

	@Tool(description = """
	사용자가 현재 대화에서 구성한 콘텐츠들로
	플레이리스트를 실제로 생성해 달라고 명확하게 요청했을 때 사용합니다.
	사용자의 명확한 생성 요청이 있기 전에는 호출하지 마세요.
	
	플레이리스트를 생성하기 전에 사용할 콘텐츠가
	searchContents 또는 recommendContentsForUser를 통해
	실제 MOPL 콘텐츠인지 확인되어 있어야 합니다.
	현재 후보가 확인되지 않은 경우에는
	사용자의 요청에 맞는 도구를 다시 호출하여 콘텐츠를 확인한 후 이 도구를 호출하세요.
	
	contentIds에는 콘텐츠 제목이 아니라
	검색 또는 추천 결과의 contentId UUID 값을 반드시 전달하세요.
	예: "E2E Content 2"가 아니라 "22222222-2222-2222-2222-222222222222"
	""")
	public PlaylistResponse createPlaylist(
		String title,
		String description,
		List<UUID> contentIds,
		ToolContext toolContext
	) {
		if (contentIds == null || contentIds.isEmpty()) {
			throw new MoplException(
				CommonErrorCode.INVALID_INPUT_VALUE,
				"플레이리스트에는 하나 이상의 콘텐츠가 필요합니다."
			);
		}

		UUID currentUserId =
			(UUID) toolContext.getContext().get("currentUserId");

		String sessionId =
			(String) toolContext.getContext().get("sessionId");

		if (!aiPlaylistCandidateStore.containsAll(
			currentUserId,
			sessionId,
			Set.copyOf(contentIds)
		)) {
			throw new PlaylistAiContentNotSearchedException();
		}

		return playlistService.createPlaylistWithContents(
			currentUserId,
			new PlaylistCreateRequest(title, description),
			contentIds
		);
	}
}