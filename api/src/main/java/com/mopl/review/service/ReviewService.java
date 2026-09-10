package com.mopl.review.service;

import com.mopl.common.exception.content.ContentNotFoundException;
import com.mopl.common.exception.review.ReviewNotFoundException;
import com.mopl.content.repository.ContentRepository;
import com.mopl.core.domain.review.entity.Review;
import com.mopl.review.dto.ReviewListResponse;
import com.mopl.review.dto.ReviewResponse;
import com.mopl.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;

    public ReviewService(ReviewRepository reviewRepository, ContentRepository contentRepository) {
        this.reviewRepository = reviewRepository;
        this.contentRepository = contentRepository;
    }

    // 리뷰 단건 조회
    public ReviewResponse getReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        return ReviewResponse.from(review);
    }

    // 리뷰 목록 조회
    public ReviewListResponse getReviews(UUID contentId, Pageable pageable) {
        if (!contentRepository.existsByIdAndDeletedAtIsNull(contentId)) {
            throw new ContentNotFoundException();
        }

        Page<ReviewResponse> reviewPage = reviewRepository
                .findAllByContentId(contentId, pageable)
                .map(ReviewResponse::from);

        return ReviewListResponse.from(reviewPage);
    }
}
