package com.mopl.ai.playlist.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mopl.ai.playlist.tool.AiPlaylistTools;

@Configuration
public class AiPlaylistConfig {

	@Bean
	public ChatMemory aiPlaylistChatMemory() {
		return MessageWindowChatMemory.builder()
			.maxMessages(10)
			.build();
	}

	@Bean
	public ChatClient aiPlaylistChatClient(
		ChatClient.Builder chatClientBuilder,
		ChatMemory aiPlaylistChatMemory,
		AiPlaylistTools aiPlaylistTools
	) {
		return chatClientBuilder
			.defaultAdvisors(
				MessageChatMemoryAdvisor.builder(aiPlaylistChatMemory)
					.build()
			)
			.defaultTools(aiPlaylistTools)
			.build();
	}
}