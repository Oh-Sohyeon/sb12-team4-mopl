package com.mopl.ai.playlist.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPlaylistCandidateStoreTest {

	@Test
	@DisplayName("검색 후보는 대화별로 분리하여 저장한다")
	void candidatesAreSeparatedByConversation() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		UUID firstContentId = UUID.randomUUID();
		UUID secondContentId = UUID.randomUUID();

		candidateStore.saveCandidates(
			"conversation-1",
			Set.of(firstContentId)
		);

		candidateStore.saveCandidates(
			"conversation-2",
			Set.of(secondContentId)
		);

		assertTrue(candidateStore.containsAll(
			"conversation-1",
			Set.of(firstContentId)
		));

		assertFalse(candidateStore.containsAll(
			"conversation-1",
			Set.of(secondContentId)
		));

		assertTrue(candidateStore.containsAll(
			"conversation-2",
			Set.of(secondContentId)
		));
	}

	@Test
	@DisplayName("같은 대화에서 검색한 콘텐츠 후보를 누적하여 저장한다")
	void candidatesAccumulateInSameConversation() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore();

		String conversationId = "conversation-1";
		UUID firstContentId = UUID.randomUUID();
		UUID secondContentId = UUID.randomUUID();

		candidateStore.saveCandidates(
			conversationId,
			Set.of(firstContentId)
		);

		candidateStore.saveCandidates(
			conversationId,
			Set.of(secondContentId)
		);

		assertTrue(candidateStore.containsAll(
			conversationId,
			Set.of(firstContentId, secondContentId)
		));
	}
}