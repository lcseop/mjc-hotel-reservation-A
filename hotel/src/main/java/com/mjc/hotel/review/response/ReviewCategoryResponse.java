package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewCategoryResponse {
    private Long sid;

    private Long reviewId;

    private Long reviewCategoryId;

    private int rating;
}
