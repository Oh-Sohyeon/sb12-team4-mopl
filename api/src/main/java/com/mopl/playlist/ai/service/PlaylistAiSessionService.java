package com.mopl.playlist.ai.service;

import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.domain.playlist.entity.PlaylistAiSession;
import com.mopl.core.domain.user.entity.User;
import com.mopl.playlist.ai.repository.PlaylistAiSessionRepository;
import com.mopl.user.repository.UserRepository;
import com.mopl.common.exception.playlist.PlaylistAiSessionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlaylistAiSessionService {

	private final PlaylistAiSessionRepository sessionRepository;
	private final UserRepository userRepository;

	public PlaylistAiSessionService(
		PlaylistAiSessionRepository sessionRepository,
		UserRepository userRepository
	) {
		this.sessionRepository = sessionRepository;
		this.userRepository = userRepository;
	}

	public PlaylistAiSession getSession(
		UUID sessionId,
		UUID userId
	) {
		return sessionRepository.findByIdAndUser_Id(sessionId, userId)
			.orElseThrow(PlaylistAiSessionNotFoundException::new);
	}

	public List<PlaylistAiSession> getSessions(UUID userId) {
		return sessionRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
	}

	@Transactional
	public PlaylistAiSession createSession(UUID userId, String title) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new MoplException(
				UserErrorCode.USER_NOT_FOUND,
				"존재하지 않는 사용자입니다. userId=" + userId
			));

		return sessionRepository.save(
			new PlaylistAiSession(user, title)
		);
	}

	@Transactional
	public void updateTitle(
		PlaylistAiSession session,
		String title
	) {
		session.updateTitle(title);
		sessionRepository.save(session);
	}

	@Transactional
	public void touchSession(UUID sessionId) {
		sessionRepository.updateUpdatedAt(
			sessionId,
			LocalDateTime.now()
		);
	}
}