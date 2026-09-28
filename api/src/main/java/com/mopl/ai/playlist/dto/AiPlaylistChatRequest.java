package com.mopl.ai.playlist.dto;

import jakarta.validation.constraints.NotBlank;

public record AiPlaylistChatRequest(

	@NotBlank(message = "대화 ID는 필수입니다.")
	String conversationId,

	@NotBlank(message = "메시지는 필수입니다.")
	String message
) {
}