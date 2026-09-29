package com.mopl.playlist.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiPlaylistChatRequest(
	@NotBlank(message = "메시지는 필수입니다.")
	String message
) {
}