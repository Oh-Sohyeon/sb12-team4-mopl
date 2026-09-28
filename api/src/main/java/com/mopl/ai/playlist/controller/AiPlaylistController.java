package com.mopl.ai.playlist.controller;

import com.mopl.ai.playlist.dto.AiPlaylistChatRequest;
import com.mopl.ai.playlist.dto.AiPlaylistChatResponse;
import com.mopl.ai.playlist.service.AiPlaylistChatService;
import com.mopl.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/playlists")
public class AiPlaylistController {

	private final AiPlaylistChatService aiPlaylistChatService;

	@PostMapping("/chat")
	public AiPlaylistChatResponse chat(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid AiPlaylistChatRequest request
	) {
		String message = aiPlaylistChatService.chat(
			authUser.userId(),
			request.conversationId(),
			request.message()
		);

		return new AiPlaylistChatResponse(
			request.conversationId(),
			message
		);
	}
}