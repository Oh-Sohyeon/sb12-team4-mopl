package com.mopl.playlist.ai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mopl.playlist.ai.service.AiPlaylistCandidateStore;

class AiPlaylistCandidateStoreTest {

	@Test
	@DisplayName("검색 후보는 대화별로 분리하여 저장한다")
	void candidatesAreSeparatedByConversation() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		UUID userId = UUID.randomUUID();
		UUID firstContentId = UUID.randomUUID();
		UUID secondContentId = UUID.randomUUID();

		candidateStore.saveCandidates(
			userId,
			"conversation-1",
			Set.of(firstContentId)
		);

		candidateStore.saveCandidates(
			userId,
			"conversation-2",
			Set.of(secondContentId)
		);

		assertTrue(candidateStore.containsAll(
			userId,
			"conversation-1",
			Set.of(firstContentId)
		));

		assertFalse(candidateStore.containsAll(
			userId,
			"conversation-1",
			Set.of(secondContentId)
		));

		assertTrue(candidateStore.containsAll(
			userId,
			"conversation-2",
			Set.of(secondContentId)
		));
	}

	@Test
	@DisplayName("같은 대화에서 검색한 콘텐츠 후보를 누적하여 저장한다")
	void candidatesAccumulateInSameConversation() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		UUID userId = UUID.randomUUID();
		String conversationId = "conversation-1";
		UUID firstContentId = UUID.randomUUID();
		UUID secondContentId = UUID.randomUUID();

		candidateStore.saveCandidates(
			userId,
			conversationId,
			Set.of(firstContentId)
		);

		candidateStore.saveCandidates(
			userId,
			conversationId,
			Set.of(secondContentId)
		);

		assertTrue(candidateStore.containsAll(
			userId,
			conversationId,
			Set.of(firstContentId, secondContentId)
		));
	}

	@Test
	@DisplayName("같은 대화 ID를 사용해도 사용자별로 검색 후보를 분리한다")
	void candidatesAreSeparatedByUser() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		UUID firstUserId = UUID.randomUUID();
		UUID secondUserId = UUID.randomUUID();
		UUID contentId = UUID.randomUUID();
		String conversationId = "conversation-1";

		candidateStore.saveCandidates(
			firstUserId,
			conversationId,
			Set.of(contentId)
		);

		assertTrue(candidateStore.containsAll(
			firstUserId,
			conversationId,
			Set.of(contentId)
		));

		assertFalse(candidateStore.containsAll(
			secondUserId,
			conversationId,
			Set.of(contentId)
		));
	}
}