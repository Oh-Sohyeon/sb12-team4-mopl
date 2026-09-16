package com.mopl.message.repository;

import com.mopl.message.dto.SortDirection;

import java.util.List;
import java.util.UUID;

public interface ConversationRepositoryCustom {

	List<ConversationListRow> findConversationsByCursor(
		UUID requesterId,
		String keywordLike,
		String cursor,
		UUID idAfter,
		int limit,
		SortDirection sortDirection
	);

	long countConversations(UUID requesterId, String keywordLike);

	List<UUID> findConversationIdsWithUnread(UUID requesterId, List<UUID> conversationIds);
}