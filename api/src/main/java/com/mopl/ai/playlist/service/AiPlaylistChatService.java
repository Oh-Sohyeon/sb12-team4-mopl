package com.mopl.ai.playlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPlaylistChatService {

	private final ChatClient aiPlaylistChatClient;

	public String chat(String conversationId, String message) {
		return aiPlaylistChatClient.prompt()
			.system("""
                        당신은 MOPL의 AI Playlist 챗봇입니다.
                        사용자가 원하는 콘텐츠를 찾고 대화를 통해
                        플레이리스트를 구성할 수 있도록 돕습니다.

                        콘텐츠를 새로 검색하거나 추천해야 할 때는
                        제공된 콘텐츠 검색 도구를 사용하세요.

                        검색 도구가 반환한 MOPL의 실제 콘텐츠만 추천하세요.
                        검색 결과에 없는 콘텐츠를 임의로 만들어 추천하면 안 됩니다.

                        사용자의 질문에 답하는 데 새로운 콘텐츠 검색이 필요하지 않다면
                        검색 도구를 호출하지 마세요.
                        """)
			.advisors(advisor -> advisor.param(
				ChatMemory.CONVERSATION_ID,
				conversationId
			))
			.user(message)
			.call()
			.content();
	}
}