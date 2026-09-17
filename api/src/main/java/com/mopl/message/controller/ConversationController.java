package com.mopl.message.controller;

import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.domain.message.entity.Conversation;
import com.mopl.core.domain.user.entity.User;
import com.mopl.message.dto.ConversationCreateRequest;
import com.mopl.message.dto.ConversationResponse;
import com.mopl.message.dto.ConversationSortBy;
import com.mopl.message.dto.CursorResponse;
import com.mopl.message.dto.DirectMessageResponse;
import com.mopl.message.dto.DirectMessageSortBy;
import com.mopl.message.dto.SortDirection;
import com.mopl.message.service.ConversationService;
import com.mopl.message.service.DirectMessageService;
import com.mopl.user.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

	private final ConversationService conversationService;
	private final DirectMessageService directMessageService;
	private final UserRepository userRepository;

	public ConversationController(
		ConversationService conversationService,
		DirectMessageService directMessageService,
		UserRepository userRepository
	) {
		this.conversationService = conversationService;
		this.directMessageService = directMessageService;
		this.userRepository = userRepository;
	}

	@PostMapping
	public ResponseEntity<ConversationResponse> createConversation(
		@RequestParam UUID requesterId, // TODO: Security 완료 후 @AuthenticationPrincipal로 교체
		@Valid @RequestBody ConversationCreateRequest request
	) {
		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> new MoplException(UserErrorCode.USER_NOT_FOUND));
		User target = userRepository.findById(request.withUserId())
			.orElseThrow(() -> new MoplException(UserErrorCode.USER_NOT_FOUND));

		Conversation conversation = conversationService.getOrCreateConversation(requester, target);
		ConversationResponse response = ConversationResponse.from(conversation, null, requesterId, false);

		return ResponseEntity.ok(response);
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