package com.mopl.recommendation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mopl.auth.dto.AuthUser;
import com.mopl.content.search.dto.ContentTagDto;
import com.mopl.core.common.enums.ContentType;
import com.mopl.recommendation.dto.RecommendationItem;
import com.mopl.recommendation.dto.RecommendationSectionsResponse;
import com.mopl.recommendation.dto.RecommendationTab;
import com.mopl.recommendation.service.RecommendationSectionService;
import com.mopl.recommendation.service.RecommendationService;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    // Section API 추가에 따라 RecommendationController의 신규 의존성 Mock 추가
    @Mock
    private RecommendationSectionService recommendationSectionService;

    private RecommendationController recommendationController;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        // RecommendationController가 RecommendationSectionService도 주입받도록 변경됨
        recommendationController = new RecommendationController(
                recommendationService,
                recommendationSectionService
        );

        currentUserId = UUID.randomUUID();

        AuthUser authUser = org.mockito.Mockito.mock(AuthUser.class);

        when(authUser.userId())
                .thenReturn(currentUserId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsRecommendationsForCurrentUser() {
        RecommendationItem recommendation =
                createRecommendationItem();

        when(recommendationService.getRecommendations(currentUserId))
                .thenReturn(
                        List.of(recommendation)
                );

        ResponseEntity<List<RecommendationItem>> response =
                recommendationController.getRecommendations();

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsExactly(recommendation);

        verify(recommendationService)
                .getRecommendations(currentUserId);
    }

    // HOME 탭 Section API가 현재 로그인한 사용자 기준으로 조회되는지 검증
    @Test
    void returnsHomeRecommendationSectionsForCurrentUser() {
        RecommendationSectionsResponse sectionsResponse =
                RecommendationSectionsResponse.empty();

        when(recommendationSectionService.getSections(
                currentUserId,
                RecommendationTab.HOME
        )).thenReturn(sectionsResponse);

        ResponseEntity<RecommendationSectionsResponse> response =
                recommendationController.getRecommendationSections(
                        RecommendationTab.HOME
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isEqualTo(sectionsResponse);

        verify(recommendationSectionService)
                .getSections(
                        currentUserId,
                        RecommendationTab.HOME
                );
    }

    private RecommendationItem createRecommendationItem() {
        return new RecommendationItem(
                UUID.randomUUID(),
                "Interstellar",
                "https://example.com/interstellar.jpg",
                ContentType.MOVIE,
                List.of(
                        new ContentTagDto(
                                "GENRE",
                                "SF"
                        )
                ),
                0.95,
                8.7,
                120.0,
                5000L,
                "SF 콘텐츠를 선호하는 취향과 잘 맞습니다."
        );
    }
}