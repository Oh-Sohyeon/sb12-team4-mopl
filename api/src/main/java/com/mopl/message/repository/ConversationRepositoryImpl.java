package com.mopl.message.repository;

import com.mopl.core.domain.message.entity.QConversation;
import com.mopl.core.domain.message.entity.QDirectMessage;
import com.mopl.message.dto.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	private static final QConversation conversation = QConversation.conversation;
	private static final QDirectMessage lastDm = new QDirectMessage("lastDm");

	@Override
	public List<ConversationListRow> findConversationsByCursor(
		UUID requesterId,
		String keywordLike,
		String cursor,
		UUID idAfter,
		int limit,
		SortDirection sortDirection
	) {
		BooleanBuilder where = new BooleanBuilder()
			.and(participantCondition(requesterId))
			.and(keywordCondition(requesterId, keywordLike))
			.and(cursorCondition(cursor, idAfter, sortDirection));

		boolean asc = sortDirection == SortDirection.ASCENDING;
		OrderSpecifier<?> primaryOrder = asc ? lastDm.createdAt.asc() : lastDm.createdAt.desc();
		OrderSpecifier<?> secondaryOrder = asc ? conversation.id.asc() : conversation.id.desc();

		return queryFactory
			.select(Projections.constructor(ConversationListRow.class, conversation, lastDm))
			.from(conversation)
			.join(lastDm).on(lastMessageJoinCondition())
			.where(where)
			.orderBy(primaryOrder, secondaryOrder)
			.limit(limit)
			.fetch();
	}

	@Override
	public long countConversations(UUID requesterId, String keywordLike) {
		BooleanBuilder where = new BooleanBuilder()
			.and(participantCondition(requesterId))
			.and(keywordCondition(requesterId, keywordLike));

		Long count = queryFactory
			.select(conversation.count())
			.from(conversation)
			.join(lastDm).on(lastMessageJoinCondition())
			.where(where)
			.fetchOne();

		return count == null ? 0L : count;
	}

	@Override
	public List<UUID> findConversationIdsWithUnread(UUID requesterId, List<UUID> conversationIds) {
		if (conversationIds.isEmpty()) {
			return List.of();
		}

		QDirectMessage dm = QDirectMessage.directMessage;
		return queryFactory
			.select(dm.conversation.id)
			.distinct()
			.from(dm)
			.where(
				dm.conversation.id.in(conversationIds),
				dm.receiver.id.eq(requesterId),
				dm.readAt.isNull()
			)
			.fetch();
	}

	private BooleanExpression lastMessageJoinCondition() {
		QDirectMessage subDm = QDirectMessage.directMessage;
		return lastDm.conversation.eq(conversation)
			.and(lastDm.createdAt.eq(
				JPAExpressions.select(subDm.createdAt.max())
					.from(subDm)
					.where(subDm.conversation.eq(conversation))
			));
	}

	private BooleanExpression participantCondition(UUID requesterId) {
		return conversation.user1.id.eq(requesterId).or(conversation.user2.id.eq(requesterId));
	}

	private BooleanExpression keywordCondition(UUID requesterId, String keywordLike) {
		if (keywordLike == null || keywordLike.isBlank()) {
			return null;
		}

		StringExpression opponentName = new CaseBuilder()
			.when(conversation.user1.id.eq(requesterId)).then(conversation.user2.name)
			.otherwise(conversation.user1.name);

		return opponentName.containsIgnoreCase(keywordLike)
			.or(lastDm.content.containsIgnoreCase(keywordLike));
	}

	private BooleanExpression cursorCondition(String cursor, UUID idAfter, SortDirection sortDirection) {
		if (cursor == null || idAfter == null) {
			return null;
		}

		LocalDateTime cursorTime = LocalDateTime.parse(cursor);
		boolean asc = sortDirection == SortDirection.ASCENDING;

		return asc
			? lastDm.createdAt.gt(cursorTime)
			.or(lastDm.createdAt.eq(cursorTime).and(conversation.id.gt(idAfter)))
			: lastDm.createdAt.lt(cursorTime)
			.or(lastDm.createdAt.eq(cursorTime).and(conversation.id.lt(idAfter)));
	}
}