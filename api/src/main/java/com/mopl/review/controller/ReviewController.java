package com.mopl.review.controller;

import com.mopl.review.dto.ReviewListResponse;
import com.mopl.review.dto.ReviewResponse;
import com.mopl.review.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;
import java.util.UUID;

@RestController
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/contents/{contentId}/reviews")
    public ResponseEntity<ReviewListResponse> getReviews(@PathVariable UUID contentId,
                                                         @PageableDefault(
                                                                 size = 20,
                                                                 sort = "createdAt",
                                                                 direction = DESC
                                                         )Pageable pageable) {
        ReviewListResponse response = reviewService.getReviews(contentId, pageable);

        return ResponseEntity.ok(response);
    }
}
