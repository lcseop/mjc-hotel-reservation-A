package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewTagResponse {
    private Long reviewId;
    private Long reviewTagId;
}
