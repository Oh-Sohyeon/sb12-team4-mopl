package com.mopl.review.dto;

import com.mopl.core.domain.review.entity.Review;
import java.math.BigDecimal;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID contentId,
        ReviewAuthorResponse author,
        String text,
        BigDecimal rating
) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getContent().getId(),
                ReviewAuthorResponse.from(review.getUser()),
                review.getText(),
                review.getRating()
        );
    }
}
