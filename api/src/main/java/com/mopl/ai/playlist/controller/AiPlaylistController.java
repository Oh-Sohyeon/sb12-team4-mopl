package com.mopl.ai.playlist.controller;

import com.mopl.ai.playlist.dto.AiPlaylistChatRequest;
import com.mopl.ai.playlist.dto.AiPlaylistChatResponse;
import com.mopl.ai.playlist.service.AiPlaylistChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/playlists")
public class AiPlaylistController {

	private final AiPlaylistChatService aiPlaylistChatService;

	@PostMapping("/chat")
	public AiPlaylistChatResponse chat(
		@RequestBody AiPlaylistChatRequest request
	) {
		String message = aiPlaylistChatService.chat(
			request.conversationId(),
			request.message()
		);

		return new AiPlaylistChatResponse(
			request.conversationId(),
			message
		);
	}
}