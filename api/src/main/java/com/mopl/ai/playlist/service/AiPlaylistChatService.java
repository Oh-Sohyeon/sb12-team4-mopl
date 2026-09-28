package com.mopl.ai.playlist.service;

import java.util.UUID;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPlaylistChatService {

	private final ChatClient aiPlaylistChatClient;

	public String chat(
		UUID currentUserId,
		String conversationId,
		String message
	) {
		return aiPlaylistChatClient.prompt()
			.system("""
				당신은 MOPL의 AI Playlist 챗봇입니다.
				사용자가 원하는 콘텐츠를 찾고, 대화를 통해 플레이리스트를 구성할 수 있도록 돕습니다.
			
				콘텐츠를 새로 찾거나 추천해야 할 때는 searchContents 도구를 사용하세요.
				searchContents가 반환한 MOPL의 실제 콘텐츠만 사용자에게 제안하세요.
				검색 결과에 없는 콘텐츠를 임의로 만들어 추천하면 안 됩니다.
			
				이전 대화에서 제안한 콘텐츠에 대해 사용자가 제외, 추가, 조건 변경을 요청하면
				대화 맥락을 반영하여 플레이리스트 구성을 조정하세요.
			
				사용자가 플레이리스트 생성을 명확하게 요청한 경우에만 createPlaylist 도구를 사용하세요.
				사용자가 단순히 콘텐츠 추천이나 검색만 요청한 경우에는 플레이리스트를 생성하지 마세요.
			
				createPlaylist를 호출할 때는 현재 대화에서 최종적으로 구성된 실제 콘텐츠의 ID만 사용하세요.
				""")
			.advisors(advisor -> advisor.param(
				ChatMemory.CONVERSATION_ID,
				conversationId
			))
			.toolContext(Map.of(
				"currentUserId", currentUserId
			))
			.user(message)
			.call()
			.content();
	}
}