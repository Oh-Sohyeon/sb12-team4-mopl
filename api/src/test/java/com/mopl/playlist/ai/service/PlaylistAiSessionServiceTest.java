package com.mopl.playlist.ai.service;

import com.mopl.common.exception.playlist.PlaylistAiSessionNotFoundException;
import com.mopl.playlist.ai.repository.PlaylistAiSessionRepository;
import com.mopl.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaylistAiSessionServiceTest {

	@Test
	@DisplayName("AI 플레이리스트 대화 세션을 찾을 수 없으면 예외가 발생한다")
	void getSessionFailsWhenSessionNotFound() {
		UUID sessionId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		PlaylistAiSessionRepository sessionRepository =
			mock(PlaylistAiSessionRepository.class);
		UserRepository userRepository =
			mock(UserRepository.class);

		when(sessionRepository.findByIdAndUser_Id(sessionId, userId))
			.thenReturn(Optional.empty());

		PlaylistAiSessionService service =
			new PlaylistAiSessionService(
				sessionRepository,
				userRepository
			);

		assertThrows(
			PlaylistAiSessionNotFoundException.class,
			() -> service.getSession(sessionId, userId)
		);
	}
}