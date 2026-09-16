package com.mopl.message.controller;

import com.mopl.message.dto.ConversationResponse;
import com.mopl.message.dto.ConversationSortBy;
import com.mopl.message.dto.CursorResponse;
import com.mopl.message.dto.DirectMessageResponse;
import com.mopl.message.dto.DirectMessageSortBy;
import com.mopl.message.dto.SortDirection;
import com.mopl.message.service.ConversationService;
import com.mopl.message.service.DirectMessageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

	private final ConversationService conversationService;
	private final DirectMessageService directMessageService;

	public ConversationController(
		ConversationService conversationService,
		DirectMessageService directMessageService
	) {
		this.conversationService = conversationService;
		this.directMessageService = directMessageService;
	}

	@GetMapping
	public ResponseEntity<CursorResponse<ConversationResponse>> getConversations(
		@RequestParam UUID requesterId, // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
		@RequestParam(required = false) String keywordLike,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam String sortDirection,
		@RequestParam String sortBy
	) {
		CursorResponse<ConversationResponse> response = conversationService.getConversations(
			requesterId,
			keywordLike,
			cursor,
			idAfter,
			limit,
			SortDirection.from(sortDirection),
			ConversationSortBy.from(sortBy)
		);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{conversationId}")
	public ResponseEntity<ConversationResponse> getConversation(
		@PathVariable UUID conversationId,
		@RequestParam UUID requesterId // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
	) {
		ConversationResponse response = conversationService.getConversation(conversationId, requesterId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/with")
	public ResponseEntity<ConversationResponse> getConversationWith(
		@RequestParam UUID requesterId, // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
		@RequestParam UUID userId
	) {
		ConversationResponse response = conversationService.getConversationWith(requesterId, userId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{conversationId}/direct-messages")
	public ResponseEntity<CursorResponse<DirectMessageResponse>> getDirectMessages(
		@PathVariable UUID conversationId,
		@RequestParam UUID requesterId, // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam int limit,
		@RequestParam String sortDirection,
		@RequestParam String sortBy
	) {
		CursorResponse<DirectMessageResponse> response = directMessageService.getDirectMessages(
			conversationId,
			requesterId,
			cursor,
			idAfter,
			limit,
			SortDirection.from(sortDirection),
			DirectMessageSortBy.from(sortBy)
		);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
	public ResponseEntity<Void> markAsRead(
		@PathVariable UUID conversationId,
		@PathVariable UUID directMessageId,
		@RequestParam UUID requesterId // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
	) {
		directMessageService.markAsRead(conversationId, directMessageId, requesterId);

		return ResponseEntity.ok().build();
	}
}