package com.mopl.ai.playlist.tool;

import com.mopl.ai.playlist.service.AiPlaylistCandidateStore;
import com.mopl.content.search.condition.SemanticCandidateCondition;
import com.mopl.content.search.dto.ContentCandidate;
import com.mopl.content.search.service.SemanticCandidateSearchService;
import com.mopl.core.common.enums.ContentType;
import com.mopl.playlist.dto.PlaylistCreateRequest;
import com.mopl.playlist.dto.PlaylistResponse;
import com.mopl.playlist.service.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiPlaylistToolsTest {

	private PlaylistService playlistService;
	private AiPlaylistTools aiPlaylistTools;
	private AiPlaylistCandidateStore candidateStore;

	@BeforeEach
	void setUp() {
		playlistService = mock(PlaylistService.class);

		SemanticCandidateSearchService semanticCandidateSearchService =
			mock(SemanticCandidateSearchService.class);

		candidateStore = new AiPlaylistCandidateStore();

		aiPlaylistTools = new AiPlaylistTools(
			playlistService,
			semanticCandidateSearchService,
			candidateStore
		);
	}

	@Test
	@DisplayName("검색 후보에 없는 콘텐츠로 플레이리스트를 생성하면 예외가 발생한다")
	void createPlaylistWithUnknownContentFails() {
		UUID currentUserId = UUID.randomUUID();
		UUID unknownContentId = UUID.randomUUID();
		String conversationId = "test-conversation";

		ToolContext toolContext = new ToolContext(Map.of(
			"currentUserId", currentUserId,
			"conversationId", conversationId
		));

		assertThrows(
			IllegalArgumentException.class,
			() -> aiPlaylistTools.createPlaylist(
				"테스트 플레이리스트",
				"테스트 설명",
				List.of(unknownContentId),
				toolContext
			)
		);

		verifyNoInteractions(playlistService);
	}

	@Test
	@DisplayName("검색 후보에 포함된 콘텐츠로 플레이리스트를 생성하면 서비스가 호출된다")
	void createPlaylistWithCandidateContentSucceeds() {
		UUID currentUserId = UUID.randomUUID();
		UUID contentId = UUID.randomUUID();
		UUID playlistId = UUID.randomUUID();
		String conversationId = "test-conversation";

		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		candidateStore.saveCandidates(
			conversationId,
			Set.of(contentId)
		);

		AiPlaylistTools tools = new AiPlaylistTools(
			playlistService,
			mock(SemanticCandidateSearchService.class),
			candidateStore
		);

		ToolContext toolContext = new ToolContext(Map.of(
			"currentUserId", currentUserId,
			"conversationId", conversationId
		));

		PlaylistResponse playlistResponse = mock(PlaylistResponse.class);

		when(playlistResponse.id()).thenReturn(playlistId);
		when(playlistService.createPlaylist(
			eq(currentUserId),
			any(PlaylistCreateRequest.class)
		)).thenReturn(playlistResponse);

		when(playlistService.getPlaylist(
			playlistId,
			currentUserId
		)).thenReturn(playlistResponse);

		tools.createPlaylist(
			"테스트 플레이리스트",
			"테스트 설명",
			List.of(contentId),
			toolContext
		);

		verify(playlistService).createPlaylist(
			eq(currentUserId),
			any(PlaylistCreateRequest.class)
		);

		verify(playlistService).addContentToPlaylist(
			currentUserId,
			playlistId,
			contentId
		);

		verify(playlistService).getPlaylist(
			playlistId,
			currentUserId
		);
	}

	@Test
	@DisplayName("콘텐츠를 검색하면 검색 결과의 ID를 대화별 후보로 저장한다")
	void searchContentsSavesCandidatesByConversation() {
		UUID contentId = UUID.randomUUID();
		String conversationId = "test-conversation";

		ContentCandidate candidate = mock(ContentCandidate.class);
		when(candidate.contentId()).thenReturn(contentId);

		SemanticCandidateSearchService searchService =
			mock(SemanticCandidateSearchService.class);

		when(searchService.search(
			eq("주말에 볼 영화"),
			any(SemanticCandidateCondition.class),
			eq(10)
		)).thenReturn(List.of(candidate));

		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		AiPlaylistTools tools = new AiPlaylistTools(
			playlistService,
			searchService,
			candidateStore
		);

		ToolContext toolContext = new ToolContext(Map.of(
			"conversationId", conversationId
		));

		List<ContentCandidate> result = tools.searchContents(
			"주말에 볼 영화",
			ContentType.MOVIE,
			toolContext
		);

		assertEquals(List.of(candidate), result);
		assertTrue(candidateStore.containsAll(
			conversationId,
			Set.of(contentId)
		));

		verify(searchService).search(
			eq("주말에 볼 영화"),
			any(SemanticCandidateCondition.class),
			eq(10)
		);
	}

	@Test
	@DisplayName("콘텐츠가 없으면 플레이리스트를 생성하지 않는다")
	void createPlaylistWithEmptyContentsFails() {
		UUID currentUserId = UUID.randomUUID();
		String conversationId = "test-conversation";

		// 검색을 한 상태를 먼저 만든다.
		candidateStore.saveCandidates(
			conversationId,
			Set.of(UUID.randomUUID())
		);

		ToolContext toolContext = new ToolContext(Map.of(
			"currentUserId", currentUserId,
			"conversationId", conversationId
		));

		assertThrows(
			IllegalArgumentException.class,
			() -> aiPlaylistTools.createPlaylist(
				"테스트 플레이리스트",
				"테스트 설명",
				List.of(),
				toolContext
			)
		);

		verifyNoInteractions(playlistService);
	}
}