package com.mopl.review.service;

import com.mopl.common.exception.content.ContentNotFoundException;
import com.mopl.common.exception.review.ReviewNotFoundException;
import com.mopl.content.repository.ContentRepository;
import com.mopl.core.domain.content.entity.Content;
import com.mopl.core.domain.review.entity.Review;
import com.mopl.core.domain.user.entity.User;
import com.mopl.review.dto.ReviewListResponse;
import com.mopl.review.dto.ReviewResponse;
import com.mopl.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContentRepository contentRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, contentRepository);
    }

    @Test
    @DisplayName("리뷰 단건 조회에 성공한다.")
    void getReviewSuccess() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();

        Review review = createReview(
                reviewId,
                userId,
                contentId,
                "리뷰 내용"
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReview(reviewId);

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.contentId()).isEqualTo(contentId);
        assertThat(response.author().id()).isEqualTo(userId);
        assertThat(response.rating()).isEqualByComparingTo("4.5");
        assertThat(response.text()).isEqualTo("리뷰 내용");
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회 시 예외가 발생한다")
    void getReviewNotFound() {
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("콘텐츠별 리뷰 목록을 조회한다")
    void getReviewsSuccess() {
        UUID contentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        Review firstReview = createReview(
                UUID.randomUUID(),
                UUID.randomUUID(),
                contentId,
                "첫 번째 리뷰"
        );

        Review secondReview = createReview(
                UUID.randomUUID(),
                UUID.randomUUID(),
                contentId,
                "두 번째 리뷰"
        );

        Page<Review> reviewPage = new PageImpl<>(
                List.of(firstReview, secondReview),
                pageable,
                2
        );

        when(contentRepository.existsByIdAndDeletedAtIsNull(contentId))
                .thenReturn(true);

        when(reviewRepository.findAllByContentId(contentId, pageable))
                .thenReturn(reviewPage);

        ReviewListResponse response = reviewService.getReviews(
                contentId,
                pageable
        );

        assertThat(response.reviews()).hasSize(2);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 콘텐츠의 리뷰 목록 조회 시 예외가 발생한다")
    void getReviewsContentNotFound() {
        UUID contentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(contentRepository.existsByIdAndDeletedAtIsNull(contentId))
                .thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviews(contentId, pageable))
                .isInstanceOf(ContentNotFoundException.class);
    }

    private Review createReview(UUID reviewId, UUID userId, UUID contentId, String text) {
        User user = mock(User.class);
        Content content = mock(Content.class);
        Review review = mock(Review.class);

        when(user.getId()).thenReturn(userId);
        when(user.getDeletedAt()).thenReturn(null);
        when(user.getName()).thenReturn("리뷰작성자");
        when(user.getProfileImageUrl()).thenReturn(null);

        when(content.getId()).thenReturn(contentId);

        when(review.getId()).thenReturn(reviewId);
        when(review.getUser()).thenReturn(user);
        when(review.getContent()).thenReturn(content);
        when(review.getRating()).thenReturn(new BigDecimal(4.5));
        when(review.getText()).thenReturn(text);
        when(review.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 9, 9, 12, 0));
        when(review.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 9, 9, 12, 0));

        return review;
    }
}
