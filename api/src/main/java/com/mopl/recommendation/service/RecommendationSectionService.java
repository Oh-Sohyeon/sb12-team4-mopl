package com.mopl.recommendation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.common.exception.CommonErrorCode;
import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.common.enums.ContentType;
import com.mopl.core.domain.user.entity.User;
import com.mopl.recommendation.dto.RecommendationItem;
import com.mopl.recommendation.dto.RecommendationPreference;
import com.mopl.recommendation.dto.RecommendationPreferredTag;
import com.mopl.recommendation.dto.RecommendationSection;
import com.mopl.recommendation.dto.RecommendationSectionItem;
import com.mopl.recommendation.dto.RecommendationSectionsResponse;
import com.mopl.recommendation.dto.RecommendationTab;
import com.mopl.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationSectionService {

    private static final int SECTION_SIZE = 10;
    private static final int NEW_CONTENT_DAYS = 14;

    private static final List<ContentType> RECOMMENDATION_CONTENT_TYPES = List.of(
            ContentType.MOVIE,
            ContentType.TV_SERIES,
            ContentType.SPORT
    );

    private final RecommendationService recommendationService;
    private final RecommendationPreferenceService recommendationPreferenceService;
    private final RecommendationSectionSearchService recommendationSectionSearchService;
    private final UserRepository userRepository;

    public RecommendationSectionsResponse getSections(
            UUID userId,
            RecommendationTab tab
    ) {
        if (tab == null) {
            throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        User user = findActiveUser(userId);

        RecommendationPreference preference =
                recommendationPreferenceService.createPreference(userId);

        return switch (tab) {
            case HOME -> getHomeSections(
                    userId,
                    user,
                    preference
            );
            case NEW -> getNewSections(
                    user,
                    preference
            );
            case MOVIE -> getTypeSections(
                    userId,
                    user,
                    preference,
                    ContentType.MOVIE
            );
            case TV_SERIES -> getTypeSections(
                    userId,
                    user,
                    preference,
                    ContentType.TV_SERIES
            );
            case SPORT -> getTypeSections(
                    userId,
                    user,
                    preference,
                    ContentType.SPORT
            );
        };
    }

    private RecommendationSectionsResponse getHomeSections(
            UUID userId,
            User user,
            RecommendationPreference preference
    ) {
        Set<UUID> usedContentIds = new HashSet<>();
        List<RecommendationSection> sections = new ArrayList<>();

        addAiRecommendationSection(
                sections,
                usedContentIds,
                userId,
                user.getName(),
                preference.coldStart()
        );

        addNewPreferenceSection(
                sections,
                usedContentIds,
                user.getName(),
                preference
        );

        if (!preference.coldStart()) {
            addPreferenceTopSection(
                    sections,
                    usedContentIds,
                    user.getName(),
                    preference,
                    ContentType.MOVIE
            );

            addPreferenceTopSection(
                    sections,
                    usedContentIds,
                    user.getName(),
                    preference,
                    ContentType.SPORT
            );

            addPreferenceTopSection(
                    sections,
                    usedContentIds,
                    user.getName(),
                    preference,
                    ContentType.TV_SERIES
            );
        }

        return RecommendationSectionsResponse.of(sections);
    }

    private RecommendationSectionsResponse getNewSections(
            User user,
            RecommendationPreference preference
    ) {
        Set<UUID> usedContentIds = new HashSet<>();
        List<RecommendationSection> sections = new ArrayList<>();
        LocalDateTime createdAfter = newContentThreshold();

        addNewPreferenceSection(
                sections,
                usedContentIds,
                user.getName(),
                preference
        );

        addNewTypeSection(
                sections,
                usedContentIds,
                ContentType.MOVIE,
                createdAfter
        );

        addNewTypeSection(
                sections,
                usedContentIds,
                ContentType.TV_SERIES,
                createdAfter
        );

        addNewTypeSection(
                sections,
                usedContentIds,
                ContentType.SPORT,
                createdAfter
        );

        return RecommendationSectionsResponse.of(sections);
    }

    private RecommendationSectionsResponse getTypeSections(
            UUID userId,
            User user,
            RecommendationPreference preference,
            ContentType contentType
    ) {
        Set<UUID> usedContentIds = new HashSet<>();
        List<RecommendationSection> sections = new ArrayList<>();
        LocalDateTime createdAfter = newContentThreshold();

        addPersonalizedTypeSection(
                sections,
                usedContentIds,
                userId,
                user.getName(),
                preference,
                contentType
        );

        addNewTypeSection(
                sections,
                usedContentIds,
                contentType,
                createdAfter
        );

        if (!preference.coldStart()) {
            addPreferenceTopSection(
                    sections,
                    usedContentIds,
                    user.getName(),
                    preference,
                    contentType
            );
        }

        return RecommendationSectionsResponse.of(sections);
    }

    private void addAiRecommendationSection(
            List<RecommendationSection> sections,
            Set<UUID> usedContentIds,
            UUID userId,
            String userName,
            boolean coldStart
    ) {
        List<UUID> contentIds = recommendationService
                .getRecommendations(userId)
                .stream()
                .map(RecommendationItem::contentId)
                .toList();

        List<RecommendationSectionItem> items =
                recommendationSectionSearchService.findByContentIds(
                        contentIds
                );

        items = takeUnused(
                items,
                usedContentIds
        );

        if (items.isEmpty()) {
            return;
        }

        sections.add(
                new RecommendationSection(
                        coldStart
                                ? "POPULAR"
                                : "AI_PERSONALIZED",
                        coldStart
                                ? "지금 MOPL에서 인기 있는 콘텐츠"
                                : userName + "님을 위한 AI 추천",
                        coldStart
                                ? "현재 MOPL에서 인기 있는 콘텐츠예요."
                                : "취향과 이용 기록을 바탕으로 추천한 콘텐츠예요.",
                        null,
                        null,
                        items
                )
        );
    }

    private void addNewPreferenceSection(
            List<RecommendationSection> sections,
            Set<UUID> usedContentIds,
            String userName,
            RecommendationPreference preference
    ) {
        LocalDateTime createdAfter = newContentThreshold();

        boolean personalized =
                !preference.coldStart()
                        && !preference.preferredTagsByType().isEmpty();

        List<RecommendationSectionItem> items =
                personalized
                        ? findNewPreferenceItems(
                        preference,
                        createdAfter
                )
                        : recommendationSectionSearchService.findNew(
                        createdAfter,
                        SECTION_SIZE
                );

        if (personalized && items.isEmpty()) {
            personalized = false;

            items = recommendationSectionSearchService.findNew(
                    createdAfter,
                    SECTION_SIZE
            );
        }

        items = takeUnused(
                items,
                usedContentIds
        );

        if (personalized && items.isEmpty()) {
            personalized = false;

            items = takeUnused(
                    recommendationSectionSearchService.findNew(
                            createdAfter,
                            SECTION_SIZE
                    ),
                    usedContentIds
            );
        }

        if (items.isEmpty()) {
            return;
        }

        sections.add(
                new RecommendationSection(
                        personalized
                                ? "NEW_FOR_YOU"
                                : "NEW_CONTENT",
                        personalized
                                ? "새로 들어온 콘텐츠 중 " + userName + "님 취향"
                                : "새로 들어온 콘텐츠",
                        personalized
                                ? "최근 추가된 콘텐츠 중 취향에 맞는 콘텐츠예요."
                                : "최근 MOPL에 추가된 콘텐츠예요.",
                        null,
                        null,
                        items
                )
        );
    }

    private void addPersonalizedTypeSection(
            List<RecommendationSection> sections,
            Set<UUID> usedContentIds,
            UUID userId,
            String userName,
            RecommendationPreference preference,
            ContentType contentType
    ) {
        boolean personalized = !preference.coldStart();

        List<RecommendationSectionItem> items;

        if (personalized) {
            List<UUID> contentIds = recommendationService
                    .getRecommendations(userId)
                    .stream()
                    .filter(recommendation ->
                            recommendation.type() == contentType
                    )
                    .map(RecommendationItem::contentId)
                    .toList();

            items = recommendationSectionSearchService.findByContentIds(
                    contentIds
            );

            if (items.isEmpty()) {
                personalized = false;

                items = recommendationSectionSearchService
                        .findPopularByType(
                                contentType,
                                SECTION_SIZE
                        );
            }
        } else {
            items = recommendationSectionSearchService
                    .findPopularByType(
                            contentType,
                            SECTION_SIZE
                    );
        }

        items = takeUnused(
                items,
                usedContentIds
        );

        if (items.isEmpty()) {
            return;
        }

        sections.add(
                new RecommendationSection(
                        personalized
                                ? "PERSONALIZED_" + contentType.name()
                                : "POPULAR_" + contentType.name(),
                        personalized
                                ? personalizedTypeTitle(
                                userName,
                                contentType
                        )
                                : popularTypeTitle(contentType),
                        personalized
                                ? "취향과 이용 기록을 바탕으로 추천한 콘텐츠예요."
                                : "현재 MOPL에서 인기 있는 콘텐츠예요.",
                        contentType,
                        null,
                        items
                )
        );
    }

    private void addNewTypeSection(
            List<RecommendationSection> sections,
            Set<UUID> usedContentIds,
            ContentType contentType,
            LocalDateTime createdAfter
    ) {
        List<RecommendationSectionItem> items =
                recommendationSectionSearchService.findNewByType(
                        contentType,
                        createdAfter,
                        SECTION_SIZE
                );

        items = takeUnused(
                items,
                usedContentIds
        );

        if (items.isEmpty()) {
            return;
        }

        sections.add(
                new RecommendationSection(
                        "NEW_" + contentType.name(),
                        newTypeTitle(contentType),
                        "최근 MOPL에 추가된 "
                                + contentTypeLabel(contentType)
                                + " 콘텐츠예요.",
                        contentType,
                        null,
                        items
                )
        );
    }

    private void addPreferenceTopSection(
            List<RecommendationSection> sections,
            Set<UUID> usedContentIds,
            String userName,
            RecommendationPreference preference,
            ContentType contentType
    ) {
        RecommendationPreferredTag preferredTag =
                findTopPreferredTag(
                        preference,
                        contentType
                );

        if (preferredTag == null) {
            return;
        }

        List<RecommendationSectionItem> items =
                recommendationSectionSearchService
                        .findPopularByPreferenceTag(
                                contentType,
                                preferredTag,
                                SECTION_SIZE
                        );

        items = takeUnused(
                items,
                usedContentIds
        );

        if (items.isEmpty()) {
            return;
        }

        sections.add(
                new RecommendationSection(
                        "PREFERENCE_TOP_" + contentType.name(),
                        preferenceTitle(
                                userName,
                                contentType,
                                preferredTag.value()
                        ),
                        preferenceDescription(contentType),
                        contentType,
                        preferredTag.value(),
                        items
                )
        );
    }

    private List<RecommendationSectionItem> findNewPreferenceItems(
            RecommendationPreference preference,
            LocalDateTime createdAfter
    ) {
        Map<UUID, RecommendationSectionItem> itemsById =
                new LinkedHashMap<>();

        for (ContentType contentType : RECOMMENDATION_CONTENT_TYPES) {
            RecommendationPreferredTag preferredTag =
                    findTopPreferredTag(
                            preference,
                            contentType
                    );

            if (preferredTag == null) {
                continue;
            }

            recommendationSectionSearchService
                    .findNewByPreferenceTag(
                            contentType,
                            preferredTag,
                            createdAfter,
                            SECTION_SIZE
                    )
                    .forEach(item ->
                            itemsById.putIfAbsent(
                                    item.contentId(),
                                    item
                            )
                    );
        }

        return itemsById.values()
                .stream()
                .sorted(
                        Comparator.comparingLong(
                                        RecommendationSectionItem::watcherCount
                                )
                                .reversed()
                                .thenComparing(
                                        Comparator.comparing(
                                                RecommendationSectionItem::averageRating,
                                                Comparator.nullsLast(
                                                        Comparator.reverseOrder()
                                                )
                                        )
                                )
                                .thenComparing(
                                        Comparator.comparingLong(
                                                        RecommendationSectionItem::reviewCount
                                                )
                                                .reversed()
                                )
                                .thenComparing(
                                        Comparator.comparing(
                                                RecommendationSectionItem::createdAt,
                                                Comparator.nullsLast(
                                                        Comparator.reverseOrder()
                                                )
                                        )
                                )
                )
                .limit(SECTION_SIZE)
                .toList();
    }

    private RecommendationPreferredTag findTopPreferredTag(
            RecommendationPreference preference,
            ContentType contentType
    ) {
        List<RecommendationPreferredTag> preferredTags =
                preference.preferredTagsByType()
                        .getOrDefault(
                                contentType,
                                List.of()
                        );

        return preferredTags.isEmpty()
                ? null
                : preferredTags.getFirst();
    }

    private List<RecommendationSectionItem> takeUnused(
            List<RecommendationSectionItem> items,
            Set<UUID> usedContentIds
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<RecommendationSectionItem> result = items.stream()
                .filter(item ->
                        !usedContentIds.contains(
                                item.contentId()
                        )
                )
                .limit(SECTION_SIZE)
                .toList();

        result.stream()
                .map(RecommendationSectionItem::contentId)
                .forEach(usedContentIds::add);

        return result;
    }

    private LocalDateTime newContentThreshold() {
        return LocalDateTime.now()
                .minusDays(NEW_CONTENT_DAYS);
    }

    private String personalizedTypeTitle(
            String userName,
            ContentType contentType
    ) {
        return switch (contentType) {
            case MOVIE ->
                    userName + "님을 위한 영화 추천";
            case TV_SERIES ->
                    userName + "님을 위한 TV 시리즈 추천";
            case SPORT ->
                    userName + "님을 위한 스포츠 추천";
        };
    }

    private String popularTypeTitle(ContentType contentType) {
        return switch (contentType) {
            case MOVIE ->
                    "지금 인기 있는 영화";
            case TV_SERIES ->
                    "지금 인기 있는 TV 시리즈";
            case SPORT ->
                    "지금 인기 있는 스포츠 콘텐츠";
        };
    }

    private String newTypeTitle(ContentType contentType) {
        return switch (contentType) {
            case MOVIE ->
                    "새로 추가된 영화";
            case TV_SERIES ->
                    "새로 추가된 TV 시리즈";
            case SPORT ->
                    "새로 추가된 스포츠";
        };
    }

    private String preferenceTitle(
            String userName,
            ContentType contentType,
            String tag
    ) {
        return switch (contentType) {
            case MOVIE ->
                    userName
                            + "님이 좋아하는 "
                            + tag
                            + " 영화 TOP 10";
            case TV_SERIES ->
                    userName
                            + "님 취향의 "
                            + tag
                            + " TV 시리즈 TOP 10";
            case SPORT ->
                    userName
                            + "님이 자주 보는 "
                            + tag
                            + " 콘텐츠 TOP 10";
        };
    }

    private String preferenceDescription(ContentType contentType) {
        return switch (contentType) {
            case MOVIE ->
                    "선호 태그를 바탕으로 추천한 영화예요.";
            case TV_SERIES ->
                    "선호 태그를 바탕으로 추천한 TV 시리즈예요.";
            case SPORT ->
                    "선호 스포츠 태그를 바탕으로 추천한 콘텐츠예요.";
        };
    }

    private String contentTypeLabel(ContentType contentType) {
        return switch (contentType) {
            case MOVIE -> "영화";
            case TV_SERIES -> "TV 시리즈";
            case SPORT -> "스포츠";
        };
    }

    private User findActiveUser(UUID userId) {
        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new MoplException(
                                UserErrorCode.USER_NOT_FOUND,
                                "존재하지 않는 사용자입니다. userId="
                                        + userId
                        )
                );
    }
}