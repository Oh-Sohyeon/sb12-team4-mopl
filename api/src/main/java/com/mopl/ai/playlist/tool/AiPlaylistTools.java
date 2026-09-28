package com.mopl.ai.playlist.tool;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;

import com.mopl.ai.playlist.service.AiPlaylistCandidateStore;
import com.mopl.playlist.dto.PlaylistCreateRequest;
import com.mopl.playlist.dto.PlaylistResponse;
import com.mopl.playlist.service.PlaylistService;
import org.springframework.ai.chat.model.ToolContext;
import com.mopl.content.search.service.SemanticCandidateSearchService;
import com.mopl.content.search.condition.SemanticCandidateCondition;
import com.mopl.content.search.dto.ContentCandidate;
import com.mopl.core.common.enums.ContentType;


import java.util.UUID;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AiPlaylistTools {

	private final PlaylistService playlistService;
	private final SemanticCandidateSearchService semanticCandidateSearchService;
	private final AiPlaylistCandidateStore aiPlaylistCandidateStore;

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

		String conversationId =
			(String) toolContext.getContext().get("conversationId");

		aiPlaylistCandidateStore.saveCandidates(
			conversationId,
			candidates.stream()
				.map(ContentCandidate::contentId)
				.collect(java.util.stream.Collectors.toSet())
		);

		return candidates;
	}

	@Tool(description = """
	사용자가 현재 대화에서 구성한 콘텐츠들로
	플레이리스트를 실제로 생성해 달라고 명확하게 요청했을 때 사용합니다.
	사용자의 명확한 생성 요청이 있기 전에는 호출하지 마세요.
	
	contentIds에는 콘텐츠 제목이 아니라
	searchContents 결과의 contentId UUID 값을 반드시 전달하세요.
	예: "E2E Content 2"가 아니라 "22222222-2222-2222-2222-222222222222"
	""")
	public PlaylistResponse createPlaylist(
		String title,
		String description,
		List<UUID> contentIds,
		ToolContext toolContext
	) {
		if (contentIds == null || contentIds.isEmpty()) {
			throw new IllegalArgumentException(
				"플레이리스트에는 하나 이상의 콘텐츠가 필요합니다."
			);
		}

		UUID currentUserId =
			(UUID) toolContext.getContext().get("currentUserId");

		String conversationId =
			(String) toolContext.getContext().get("conversationId");


		if (!aiPlaylistCandidateStore.containsAll(
			conversationId,
			Set.copyOf(contentIds)
		)) {
			throw new IllegalArgumentException(
				"검색 결과에 포함되지 않은 콘텐츠는 플레이리스트에 추가할 수 없습니다."
			);
		}

		PlaylistResponse playlist = playlistService.createPlaylist(
			currentUserId,
			new PlaylistCreateRequest(title, description)
		);

		for (UUID contentId : contentIds) {
			playlistService.addContentToPlaylist(
				currentUserId,
				playlist.id(),
				contentId
			);
		}

		return playlistService.getPlaylist(
			playlist.id(),
			currentUserId
		);
	}
}