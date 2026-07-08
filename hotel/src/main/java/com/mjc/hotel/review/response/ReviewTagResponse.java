package com.mjc.hotel.review.response;

import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewTagResponse {
    private Long reviewId;
    private Long reviewTagId;
    private String reviewTagName;
    private ReviewTagCategory reviewTagCategory;
}
