package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewCategoryMasterResponse {
    private Long sid;
    private String reviewCategoryName;
}
