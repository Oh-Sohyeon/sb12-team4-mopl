package com.mopl.ai.playlist.dto;

public record AiPlaylistChatRequest(
	String conversationId,
	String message
) {
}