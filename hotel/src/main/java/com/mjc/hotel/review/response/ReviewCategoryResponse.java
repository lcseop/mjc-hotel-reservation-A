package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewCategoryResponse {
    private Long reviewCategoryId;

    private Long reviewId;

    private Long reviewCategoryMasterId;

    private int rating;
}
