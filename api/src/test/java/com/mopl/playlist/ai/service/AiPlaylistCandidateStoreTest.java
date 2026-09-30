package com.mopl.playlist.ai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

@Testcontainers
class AiPlaylistCandidateStoreTest {

	@Container
	static final GenericContainer<?> REDIS =
		new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	private static LettuceConnectionFactory connectionFactory;
	private static StringRedisTemplate redisTemplate;

	@BeforeAll
	static void setUpRedis() {
		connectionFactory = new LettuceConnectionFactory(
			REDIS.getHost(),
			REDIS.getMappedPort(6379)
		);
		connectionFactory.afterPropertiesSet();

		redisTemplate = new StringRedisTemplate(connectionFactory);
		redisTemplate.afterPropertiesSet();
	}

	@BeforeEach
	void clearRedis() {
		connectionFactory
			.getConnection()
			.serverCommands()
			.flushDb();
	}

	@AfterAll
	static void tearDownRedis() {
		connectionFactory.destroy();
	}

	@Test
	@DisplayName("검색 후보는 대화별로 분리하여 저장한다")
	void candidatesAreSeparatedByConversation() {
		AiPlaylistCandidateStore candidateStore =
			new AiPlaylistCandidateStore(redisTemplate);

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
			new AiPlaylistCandidateStore(redisTemplate);

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
			new AiPlaylistCandidateStore(redisTemplate);

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

	@Test
	@DisplayName("서버 인스턴스가 변경되어도 검색 후보가 유지되어야 한다")
	void candidatesShouldRemainAcrossStoreInstances() {
		UUID userId = UUID.randomUUID();
		String sessionId = "conversation-reproduction";
		UUID contentId = UUID.randomUUID();

		AiPlaylistCandidateStore firstInstance =
			new AiPlaylistCandidateStore(redisTemplate);

		firstInstance.saveCandidates(
			userId,
			sessionId,
			Set.of(contentId)
		);

		AiPlaylistCandidateStore secondInstance =
			new AiPlaylistCandidateStore(redisTemplate);

		assertTrue(secondInstance.containsAll(
			userId,
			sessionId,
			Set.of(contentId)
		));
	}
}