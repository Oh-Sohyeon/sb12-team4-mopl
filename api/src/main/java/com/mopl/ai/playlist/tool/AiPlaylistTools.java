package com.mopl.ai.playlist.tool;

import com.mopl.content.search.service.SemanticCandidateSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import com.mopl.content.search.condition.SemanticCandidateCondition;
import com.mopl.content.search.dto.ContentCandidate;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPlaylistTools {

	private final SemanticCandidateSearchService semanticCandidateSearchService;

	@Tool(description = """
            사용자가 새로운 콘텐츠를 추천받거나 찾고 싶을 때 사용합니다.
            사용자의 자연어 요청을 기반으로 MOPL에 실제 존재하는 콘텐츠를 검색합니다.
            """)
	public List<ContentCandidate> searchContents(String query) {
		return semanticCandidateSearchService.search(
			query,
			new SemanticCandidateCondition(null, List.of()),
			20
		);
	}
}