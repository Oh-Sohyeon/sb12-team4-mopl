package com.mopl.ai.playlist.controller;

import com.mopl.ai.playlist.dto.AiPlaylistChatRequest;
import com.mopl.ai.playlist.dto.AiPlaylistChatResponse;
import com.mopl.ai.playlist.service.AiPlaylistChatService;
import com.mopl.auth.dto.AuthUser;
import com.mopl.core.common.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiPlaylistControllerTest {

	private final AiPlaylistChatService aiPlaylistChatService =
		mock(AiPlaylistChatService.class);

	private final AiPlaylistController aiPlaylistController =
		new AiPlaylistController(aiPlaylistChatService);

	@Test
	@DisplayName("AI 플레이리스트 채팅 요청 시 인증 사용자와 대화 정보를 서비스에 전달한다")
	void chatPassesAuthenticatedUserAndConversationToService() {
		UUID userId = UUID.randomUUID();
		String conversationId = "conversation-1";
		String message = "비 오는 날 보기 좋은 영화 추천해줘";
		String answer = "추천 콘텐츠입니다.";

		AuthUser authUser = new AuthUser(
			userId,
			"test@mopl.com",
			UserRole.USER
		);

		AiPlaylistChatRequest request =
			new AiPlaylistChatRequest(conversationId, message);

		when(aiPlaylistChatService.chat(
			userId,
			conversationId,
			message
		)).thenReturn(answer);

		AiPlaylistChatResponse response =
			aiPlaylistController.chat(authUser, request);

		assertEquals(conversationId, response.conversationId());
		assertEquals(answer, response.message());

		verify(aiPlaylistChatService).chat(
			userId,
			conversationId,
			message
		);
	}
}