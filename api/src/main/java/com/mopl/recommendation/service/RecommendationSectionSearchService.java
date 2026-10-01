package com.mopl.recommendation.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.mopl.content.repository.ContentRepository;
import com.mopl.content.search.document.ContentSearchDocument;
import com.mopl.core.common.enums.ContentType;
import com.mopl.core.domain.content.entity.Content;
import com.mopl.recommendation.dto.RecommendationPreferredTag;
import com.mopl.recommendation.dto.RecommendationSectionItem;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationSectionSearchService {

    private static final int MAX_SECTION_SIZE = 10;

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ElasticsearchOperations elasticsearchOperations;
    private final ContentRepository contentRepository;

    // 기존 AI 추천 결과의 contentId 순서를 유지하면서 Section 카드 DTO로 변환
    public List<RecommendationSectionItem> findByContentIds(
            List<UUID> contentIds
    ) {
        if (contentIds == null || contentIds.isEmpty()) {
            return List.of();
        }

        // AI 추천 결과의 중복 ID를 제거하고 Section 최대 크기인 10개까지만 조회
        List<UUID> limitedContentIds = contentIds.stream()
                .distinct()
                .limit(MAX_SECTION_SIZE)
                .toList();

        List<String> elasticsearchIds = limitedContentIds.stream()
                .map(UUID::toString)
                .toList();

        // 기존 추천 결과의 콘텐츠 ID들에 해당하는 Elasticsearch 문서 조회
        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.ids(ids -> ids
                        .values(elasticsearchIds)
                )))
                .withPageable(PageRequest.of(
                        0,
                        limitedContentIds.size()
                ))
                .build();

        SearchHits<ContentSearchDocument> searchHits =
                elasticsearchOperations.search(
                        query,
                        ContentSearchDocument.class
                );

        if (searchHits.isEmpty()) {
            return List.of();
        }

        // Elasticsearch 문서를 contentId 기준으로 빠르게 조회할 수 있도록 Map 구성
        Map<UUID, ContentSearchDocument> documentById =
                searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toMap(
                                document -> UUID.fromString(document.getId()),
                                document -> document
                        ));

        // MySQL에서 실제 존재하며 삭제되지 않은 콘텐츠만 조회
        Map<UUID, Content> contentById = contentRepository
                .findAllByIdInAndDeletedAtIsNull(limitedContentIds)
                .stream()
                .collect(Collectors.toMap(
                        Content::getId,
                        content -> content
                ));

        // Elasticsearch 반환 순서가 아니라 기존 AI 추천 순서를 그대로 유지
        return limitedContentIds.stream()
                .map(contentId -> {
                    Content content = contentById.get(contentId);
                    ContentSearchDocument document = documentById.get(contentId);

                    if (content == null || document == null) {
                        return null;
                    }

                    return RecommendationSectionItem.from(
                            content,
                            document
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 전체 인기 콘텐츠 조회
    public List<RecommendationSectionItem> findPopular(int size) {
        return search(
                null,
                null,
                null,
                null,
                size,
                buildPopularitySort()
        );
    }

    // 특정 콘텐츠 타입의 인기 콘텐츠 조회
    public List<RecommendationSectionItem> findPopularByType(
            ContentType contentType,
            int size
    ) {
        return search(
                contentType,
                null,
                null,
                null,
                size,
                buildPopularitySort()
        );
    }

    // 특정 콘텐츠 타입 + 선호 태그 기준 TOP 콘텐츠 조회
    public List<RecommendationSectionItem> findPopularByPreferenceTag(
            ContentType contentType,
            RecommendationPreferredTag preferredTag,
            int size
    ) {
        return search(
                contentType,
                preferredTag,
                null,
                null,
                size,
                buildPopularitySort()
        );
    }

    // 최근 MOPL DB에 추가된 전체 콘텐츠 조회
    public List<RecommendationSectionItem> findNew(
            LocalDateTime createdAfter,
            int size
    ) {
        return search(
                null,
                null,
                createdAfter,
                null,
                size,
                buildNewestSort()
        );
    }

    // 최근 MOPL DB에 등록된 특정 타입 콘텐츠 조회
    public List<RecommendationSectionItem> findNewByType(
            ContentType contentType,
            LocalDateTime createdAfter,
            int size
    ) {
        return search(
                contentType,
                null,
                createdAfter,
                null,
                size,
                buildNewestSort()
        );
    }

    // 신규 콘텐츠 중 특정 타입 + 선호 태그 조건 조회
    public List<RecommendationSectionItem> findNewByPreferenceTag(
            ContentType contentType,
            RecommendationPreferredTag preferredTag,
            LocalDateTime createdAfter,
            int size
    ) {
        return search(
                contentType,
                preferredTag,
                createdAfter,
                null,
                size,
                buildPopularitySort()
        );
    }

    // 공통 Section 검색 진입점
    private List<RecommendationSectionItem> search(
            ContentType contentType,
            RecommendationPreferredTag preferredTag,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            int requestedSize,
            Sort sort
    ) {
        int size = normalizeSize(requestedSize);

        List<Query> filters = buildFilters(
                contentType,
                preferredTag,
                createdAfter,
                createdBefore
        );

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(Query.of(query -> query.matchAll(matchAll -> matchAll)))
                .withPageable(PageRequest.of(
                        0,
                        size,
                        sort
                ));

        if (!filters.isEmpty()) {
            queryBuilder.withFilter(
                    Query.of(query -> query.bool(bool -> bool.filter(filters)))
            );
        }

        SearchHits<ContentSearchDocument> searchHits =
                elasticsearchOperations.search(
                        queryBuilder.build(),
                        ContentSearchDocument.class
                );

        return toSectionItems(
                searchHits,
                size
        );
    }

    // type / tag / createdAt 조건을 Elasticsearch filter로 변환
    private List<Query> buildFilters(
            ContentType contentType,
            RecommendationPreferredTag preferredTag,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        List<Query> filters = new ArrayList<>();

        if (contentType != null) {
            filters.add(
                    Query.of(query -> query.term(term -> term
                            .field("type")
                            .value(contentType.name())
                    ))
            );
        }

        if (preferredTag != null) {
            filters.add(
                    buildTagFilter(
                            preferredTag.tag(),
                            preferredTag.value()
                    )
            );
        }

        if (createdAfter != null || createdBefore != null) {
            filters.add(
                    buildCreatedAtFilter(
                            createdAfter,
                            createdBefore
                    )
            );
        }

        return filters;
    }

    // nested tags 내부의 tag/value가 같은 객체에서 일치하도록 검색
    private Query buildTagFilter(
            String tag,
            String value
    ) {
        Query tagFilter = Query.of(query -> query.term(term -> term
                .field("tags.tag")
                .value(tag)
        ));

        Query valueFilter = Query.of(query -> query.term(term -> term
                .field("tags.value")
                .value(value)
        ));

        Query tagAndValueFilter = Query.of(query -> query.bool(bool -> bool
                .filter(List.of(
                        tagFilter,
                        valueFilter
                ))
        ));

        return Query.of(query -> query.nested(nested -> nested
                .path("tags")
                .query(tagAndValueFilter)
        ));
    }

    // releaseDate가 아니라 MOPL DB 등록 시각인 createdAt 기준
    private Query buildCreatedAtFilter(
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        return Query.of(query -> query.range(range -> range.date(date -> {
            date.field("createdAt");

            if (createdAfter != null) {
                date.gte(
                        createdAfter.format(CREATED_AT_FORMATTER)
                );
            }

            if (createdBefore != null) {
                date.lte(
                        createdBefore.format(CREATED_AT_FORMATTER)
                );
            }

            return date;
        })));
    }

    // [#90 추가]
    // Elasticsearch 결과 순서를 유지하면서 MySQL에서 삭제된 콘텐츠를 제거
    private List<RecommendationSectionItem> toSectionItems(
            SearchHits<ContentSearchDocument> searchHits,
            int size
    ) {
        if (searchHits.isEmpty()) {
            return List.of();
        }

        List<UUID> contentIds = searchHits.getSearchHits().stream()
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

        return searchHits.getSearchHits().stream()
                .map(searchHit -> toSectionItem(
                        searchHit,
                        contentById
                ))
                .filter(Objects::nonNull)
                .limit(size)
                .toList();
    }

    private RecommendationSectionItem toSectionItem(
            SearchHit<ContentSearchDocument> searchHit,
            Map<UUID, Content> contentById
    ) {
        ContentSearchDocument searchDocument =
                searchHit.getContent();

        UUID contentId =
                UUID.fromString(searchDocument.getId());

        Content content =
                contentById.get(contentId);

        if (content == null) {
            return null;
        }

        return RecommendationSectionItem.from(
                content,
                searchDocument
        );
    }

    // Section 응답은 어떤 호출에서도 10개를 넘지 않도록 서비스 레벨에서 보장
    private int normalizeSize(int requestedSize) {
        if (requestedSize <= 0) {
            return MAX_SECTION_SIZE;
        }

        return Math.min(
                requestedSize,
                MAX_SECTION_SIZE
        );
    }

    // 기존 ColdStartRecommendationService와 동일한 인기 정렬 기준 사용
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

    // 신규 콘텐츠는 MOPL DB 등록일 내림차순 + id tie-breaker
    private Sort buildNewestSort() {
        return Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("id")
        );
    }
}
