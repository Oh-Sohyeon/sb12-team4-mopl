package com.mopl.ai.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.ai.playlist.dto.AiPlaylistChatRequest;
import com.mopl.ai.playlist.service.AiPlaylistChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiPlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiPlaylistControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AiPlaylistChatService aiPlaylistChatService;

	@Test
	@DisplayName("메시지가 비어 있으면 400을 반환한다")
	void chatFailsWhenMessageIsBlank() throws Exception {
		AiPlaylistChatRequest request =
			new AiPlaylistChatRequest(
				"conversation-1",
				""
			);

		mockMvc.perform(post("/api/ai/playlists/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(aiPlaylistChatService);
	}

	@Test
	@DisplayName("대화 ID가 비어 있으면 400을 반환한다")
	void chatFailsWhenConversationIdIsBlank() throws Exception {
		AiPlaylistChatRequest request =
			new AiPlaylistChatRequest(
				"",
				"비 오는 날 보기 좋은 영화 추천해줘"
			);

		mockMvc.perform(post("/api/ai/playlists/chat")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(aiPlaylistChatService);
	}
}