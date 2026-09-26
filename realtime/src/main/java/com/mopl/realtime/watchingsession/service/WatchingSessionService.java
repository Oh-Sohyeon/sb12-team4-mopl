package com.mopl.realtime.watchingsession.service;

import com.mopl.core.domain.content.entity.Content;
import com.mopl.core.domain.user.entity.User;
import com.mopl.core.domain.watchingsession.model.WatchingSessionState;
import com.mopl.infrastructure.content.repository.ContentSummaryQueryResult;
import com.mopl.infrastructure.content.repository.ContentSummaryRepository;
import com.mopl.infrastructure.watchingsession.repository.WatchingSessionRedisRepository;
import com.mopl.realtime.common.dto.UserSummary;
import com.mopl.realtime.contentchat.repository.ContentRepository;
import com.mopl.realtime.contentchat.repository.UserRepository;
import com.mopl.realtime.watchingsession.dto.WatchingContentSummary;
import com.mopl.realtime.watchingsession.dto.WatchingSessionDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchingSessionService {

	private final WatchingSessionRedisRepository watchingSessionRepository;
	private final UserRepository userRepository;
	private final ContentRepository contentRepository;
	private final ContentSummaryRepository contentSummaryRepository;

	public WatchingSessionJoinResult join(
		UUID userId,
		UUID contentId,
		String webSocketSessionId,
		String subscriptionId
	) {
		Optional<WatchingSessionState> previousSession =
			watchingSessionRepository.findByUserId(userId);

		previousSession.ifPresent(watchingSessionRepository::delete);

		WatchingSessionState session = new WatchingSessionState(
			UUID.randomUUID(),
			userId,
			contentId,
			webSocketSessionId,
			subscriptionId,
			Instant.now()
		);

		watchingSessionRepository.save(session);

		return new WatchingSessionJoinResult(
			session,
			previousSession
		);
	}

	public Optional<WatchingSessionState> leave(String webSocketSessionId) {
		Optional<WatchingSessionState> session =
			watchingSessionRepository.findByWebSocketSessionId(webSocketSessionId);

		session.ifPresent(watchingSessionRepository::delete);

		return session;
	}

	public Optional<WatchingSessionState> leaveSubscription(
		String webSocketSessionId,
		String subscriptionId
	) {
		Optional<WatchingSessionState> session =
			watchingSessionRepository.findByWebSocketSessionId(webSocketSessionId);

		if (session.isEmpty()) {
			return Optional.empty();
		}

		WatchingSessionState currentSession = session.get();

		if (!currentSession.subscriptionId().equals(subscriptionId)) {
			return Optional.empty();
		}

		watchingSessionRepository.delete(currentSession);

		return Optional.of(currentSession);
	}

	public WatchingSessionDto toDto(WatchingSessionState session) {
		User watcher = userRepository
			.findByIdAndDeletedAtIsNull(session.watcherId())
			.orElseThrow();

		Content content = contentRepository
			.findByIdAndDeletedAtIsNull(session.contentId())
			.orElseThrow();

		ContentSummaryQueryResult summary =
			contentSummaryRepository.findByContentId(content.getId());

		return new WatchingSessionDto(
			session.id(),
			session.createdAt(),
			UserSummary.from(watcher),
			new WatchingContentSummary(
				content.getId(),
				content.getType(),
				content.getTitle(),
				content.getDescription(),
				content.getThumbnailUrl(),
				summary.tags(),
				summary.averageRating(),
				summary.reviewCount()
			)
		);
	}

	public long count(UUID contentId) {
		return watchingSessionRepository.countByContentId(contentId);
	}
}