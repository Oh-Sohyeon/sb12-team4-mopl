package com.mopl.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class RecommendationSectionServiceTest {

    private static final int SECTION_SIZE = 10;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private RecommendationPreferenceService recommendationPreferenceService;

    @Mock
    private RecommendationSectionSearchService recommendationSectionSearchService;

    @Mock
    private UserRepository userRepository;

    private RecommendationSectionService recommendationSectionService;

    private UUID userId;
    private User user;

    private RecommendationPreferredTag movieTag;
    private RecommendationPreferredTag tvSeriesTag;
    private RecommendationPreferredTag sportTag;

    @BeforeEach
    void setUp() {
        recommendationSectionService =
                new RecommendationSectionService(
                        recommendationService,
                        recommendationPreferenceService,
                        recommendationSectionSearchService,
                        userRepository
                );

        userId = UUID.randomUUID();

        user = mock(User.class);

        when(user.getName())
                .thenReturn("소현");

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        movieTag = new RecommendationPreferredTag(
                "GENRE",
                "SF",
                10L
        );

        tvSeriesTag = new RecommendationPreferredTag(
                "GENRE",
                "미스터리",
                8L
        );

        sportTag = new RecommendationPreferredTag(
                "SPORT",
                "F1",
                7L
        );
    }

    @Test
    void returnsPersonalizedHomeSectionsInExpectedOrder() {
        RecommendationPreference preference =
                createPersonalizedPreference();

        RecommendationItem aiRecommendation =
                createRecommendation(
                        ContentType.MOVIE,
                        "AI 추천"
                );

        RecommendationSectionItem aiItem =
                createSectionItem(
                        aiRecommendation.contentId(),
                        ContentType.MOVIE,
                        "AI 추천"
                );

        RecommendationSectionItem newItem =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "신규 취향"
                );

        RecommendationSectionItem movieItem =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "SF 영화"
                );

        RecommendationSectionItem sportItem =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.SPORT,
                        "F1 콘텐츠"
                );

        RecommendationSectionItem tvItem =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.TV_SERIES,
                        "미스터리 TV"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(preference);

        when(recommendationService.getRecommendations(userId))
                .thenReturn(List.of(aiRecommendation));

        when(recommendationSectionSearchService.findByContentIds(
                List.of(aiRecommendation.contentId())
        )).thenReturn(List.of(aiItem));

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.MOVIE),
                eq(movieTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newItem));

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.TV_SERIES),
                eq(tvSeriesTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of());

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.SPORT),
                eq(sportTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of());

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.MOVIE,
                movieTag,
                SECTION_SIZE
        )).thenReturn(List.of(movieItem));

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.SPORT,
                sportTag,
                SECTION_SIZE
        )).thenReturn(List.of(sportItem));

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.TV_SERIES,
                tvSeriesTag,
                SECTION_SIZE
        )).thenReturn(List.of(tvItem));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.HOME
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "AI_PERSONALIZED",
                        "NEW_FOR_YOU",
                        "PREFERENCE_TOP_MOVIE",
                        "PREFERENCE_TOP_SPORT",
                        "PREFERENCE_TOP_TV_SERIES"
                );

        assertThat(response.sections().getFirst().title())
                .isEqualTo("소현님을 위한 AI 추천");
    }

    @Test
    void returnsNewTabSectionsInExpectedOrder() {
        RecommendationPreference preference =
                createPersonalizedPreference();

        RecommendationSectionItem personalizedNew =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "취향 신규"
                );

        RecommendationSectionItem newMovie =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "새 영화"
                );

        RecommendationSectionItem newTv =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.TV_SERIES,
                        "새 TV"
                );

        RecommendationSectionItem newSport =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.SPORT,
                        "새 스포츠"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(preference);

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.MOVIE),
                eq(movieTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(personalizedNew));

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.TV_SERIES),
                eq(tvSeriesTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of());

        when(recommendationSectionSearchService.findNewByPreferenceTag(
                eq(ContentType.SPORT),
                eq(sportTag),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of());

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.MOVIE),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newMovie));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.TV_SERIES),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newTv));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.SPORT),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newSport));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.NEW
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "NEW_FOR_YOU",
                        "NEW_MOVIE",
                        "NEW_TV_SERIES",
                        "NEW_SPORT"
                );
    }

    @Test
    void returnsMovieTabSectionsForPersonalizedUser() {
        RecommendationPreference preference =
                createPersonalizedPreference();

        RecommendationItem recommendation =
                createRecommendation(
                        ContentType.MOVIE,
                        "개인화 영화"
                );

        RecommendationSectionItem personalizedMovie =
                createSectionItem(
                        recommendation.contentId(),
                        ContentType.MOVIE,
                        "개인화 영화"
                );

        RecommendationSectionItem newMovie =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "새 영화"
                );

        RecommendationSectionItem preferenceMovie =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "SF 영화"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(preference);

        when(recommendationService.getRecommendations(userId))
                .thenReturn(List.of(recommendation));

        when(recommendationSectionSearchService.findByContentIds(
                List.of(recommendation.contentId())
        )).thenReturn(List.of(personalizedMovie));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.MOVIE),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newMovie));

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.MOVIE,
                movieTag,
                SECTION_SIZE
        )).thenReturn(List.of(preferenceMovie));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.MOVIE
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "PERSONALIZED_MOVIE",
                        "NEW_MOVIE",
                        "PREFERENCE_TOP_MOVIE"
                );
    }

    @Test
    void returnsTvSeriesTabSectionsForPersonalizedUser() {
        RecommendationPreference preference =
                createPersonalizedPreference();

        RecommendationItem recommendation =
                createRecommendation(
                        ContentType.TV_SERIES,
                        "개인화 TV"
                );

        RecommendationSectionItem personalizedTv =
                createSectionItem(
                        recommendation.contentId(),
                        ContentType.TV_SERIES,
                        "개인화 TV"
                );

        RecommendationSectionItem newTv =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.TV_SERIES,
                        "새 TV"
                );

        RecommendationSectionItem preferenceTv =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.TV_SERIES,
                        "미스터리 TV"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(preference);

        when(recommendationService.getRecommendations(userId))
                .thenReturn(List.of(recommendation));

        when(recommendationSectionSearchService.findByContentIds(
                List.of(recommendation.contentId())
        )).thenReturn(List.of(personalizedTv));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.TV_SERIES),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newTv));

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.TV_SERIES,
                tvSeriesTag,
                SECTION_SIZE
        )).thenReturn(List.of(preferenceTv));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.TV_SERIES
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "PERSONALIZED_TV_SERIES",
                        "NEW_TV_SERIES",
                        "PREFERENCE_TOP_TV_SERIES"
                );
    }

    @Test
    void returnsSportTabSectionsForPersonalizedUser() {
        RecommendationPreference preference =
                createPersonalizedPreference();

        RecommendationItem recommendation =
                createRecommendation(
                        ContentType.SPORT,
                        "개인화 스포츠"
                );

        RecommendationSectionItem personalizedSport =
                createSectionItem(
                        recommendation.contentId(),
                        ContentType.SPORT,
                        "개인화 스포츠"
                );

        RecommendationSectionItem newSport =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.SPORT,
                        "새 스포츠"
                );

        RecommendationSectionItem preferenceSport =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.SPORT,
                        "F1"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(preference);

        when(recommendationService.getRecommendations(userId))
                .thenReturn(List.of(recommendation));

        when(recommendationSectionSearchService.findByContentIds(
                List.of(recommendation.contentId())
        )).thenReturn(List.of(personalizedSport));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.SPORT),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newSport));

        when(recommendationSectionSearchService.findPopularByPreferenceTag(
                ContentType.SPORT,
                sportTag,
                SECTION_SIZE
        )).thenReturn(List.of(preferenceSport));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.SPORT
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "PERSONALIZED_SPORT",
                        "NEW_SPORT",
                        "PREFERENCE_TOP_SPORT"
                );
    }

    @Test
    void usesPopularTypeSectionForColdStartUser() {
        RecommendationSectionItem popularMovie =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "인기 영화"
                );

        RecommendationSectionItem newMovie =
                createSectionItem(
                        UUID.randomUUID(),
                        ContentType.MOVIE,
                        "새 영화"
                );

        when(recommendationPreferenceService.createPreference(userId))
                .thenReturn(
                        RecommendationPreference.forColdStart()
                );

        when(recommendationSectionSearchService.findPopularByType(
                ContentType.MOVIE,
                SECTION_SIZE
        )).thenReturn(List.of(popularMovie));

        when(recommendationSectionSearchService.findNewByType(
                eq(ContentType.MOVIE),
                any(LocalDateTime.class),
                eq(SECTION_SIZE)
        )).thenReturn(List.of(newMovie));

        RecommendationSectionsResponse response =
                recommendationSectionService.getSections(
                        userId,
                        RecommendationTab.MOVIE
                );

        assertThat(response.sections())
                .extracting(RecommendationSection::key)
                .containsExactly(
                        "POPULAR_MOVIE",
                        "NEW_MOVIE"
                );

        verify(recommendationService, never())
                .getRecommendations(userId);

        verify(
                recommendationSectionSearchService,
                never()
        ).findPopularByPreferenceTag(
                eq(ContentType.MOVIE),
                any(RecommendationPreferredTag.class),
                eq(SECTION_SIZE)
        );
    }

    private RecommendationPreference createPersonalizedPreference() {
        return RecommendationPreference.personalized(
                "개인화 취향 정보",
                Set.of(),
                Map.of(
                        ContentType.MOVIE,
                        List.of(movieTag),
                        ContentType.TV_SERIES,
                        List.of(tvSeriesTag),
                        ContentType.SPORT,
                        List.of(sportTag)
                )
        );
    }

    private RecommendationItem createRecommendation(
            ContentType contentType,
            String title
    ) {
        return new RecommendationItem(
                UUID.randomUUID(),
                title,
                "https://example.com/" + UUID.randomUUID() + ".jpg",
                contentType,
                List.of(),
                0.95,
                8.5,
                100.0,
                1000L,
                "추천 이유"
        );
    }

    private RecommendationSectionItem createSectionItem(
            UUID contentId,
            ContentType contentType,
            String title
    ) {
        return new RecommendationSectionItem(
                contentId,
                title,
                "https://example.com/" + contentId + ".jpg",
                contentType,
                List.of(),
                4.5,
                100L,
                1000L,
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        12,
                        0
                )
        );
    }
}