package com.mopl.recommendation.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mopl.recommendation.config.RecommendationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.mopl.content.repository.ContentRepository;
import com.mopl.content.search.document.ContentSearchDocument;
import com.mopl.content.search.dto.ContentCandidate;
import com.mopl.core.domain.content.entity.Content;
import com.mopl.recommendation.dto.RecommendationItem;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColdStartRecommendationService {

    private static final String COLD_START_REASON = "현재 MOPL에서 인기 있는 콘텐츠입니다.";

    private final ElasticsearchOperations elasticsearchOperations;
    private final ContentRepository contentRepository;
    private final RecommendationProperties recommendationProperties;

    public List<RecommendationItem> recommend() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.matchAll(
                        matchAll -> matchAll
                )))
                .withPageable(PageRequest.of(
                        0,
                        recommendationProperties.candidateSize(),
                        buildPopularitySort()
                ))
                .build();

        SearchHits<ContentSearchDocument> searchHits = elasticsearchOperations.search(
                query,
                ContentSearchDocument.class
        );

        if (searchHits.isEmpty()) {
            return List.of();
        }

        List<UUID> contentIds = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .map(ContentSearchDocument::getId)
                .map(UUID::fromString)
                .toList();

        Map<UUID, Content> contentById = contentRepository
                .findAllByIdInAndDeletedAtIsNull(contentIds)
                .stream()
                .collect(Collectors.toMap(
                        Content::getId,
                        content -> content
                ));

        return searchHits.getSearchHits()
                .stream()
                .map(searchHit -> createRecommendation(
                        searchHit,
                        contentById
                ))
                .filter(Objects::nonNull)
                .limit(recommendationProperties.resultSize())
                .toList();
    }

    private Sort buildPopularitySort() {
        return Sort.by(
                Sort.Order.desc("watcherCount"),
                Sort.Order.desc("averageRating"),
                Sort.Order.desc("reviewCount"),
                Sort.Order.desc("externalPopularity"),
                Sort.Order.desc("externalRating"),
                Sort.Order.desc("externalVoteCount"),
                Sort.Order.desc("id")
        );
    }

    private RecommendationItem createRecommendation(
            SearchHit<ContentSearchDocument> searchHit,
            Map<UUID, Content> contentById
    ) {
        ContentSearchDocument document = searchHit.getContent();

        UUID contentId = UUID.fromString(document.getId());

        Content content = contentById.get(contentId);

        // Elasticsearch에는 남아 있지만 MySQL에서 삭제된 콘텐츠는 제외
        if (content == null) {
            return null;
        }

        ContentCandidate candidate = ContentCandidate.from(
                content,
                document,
                0.0
        );

        return RecommendationItem.from(
                candidate,
                COLD_START_REASON
        );
    }
}
