package com.mjc.hotel.review.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewCategoryMasterUpdateRequest {
    private Long sid;
    private String reviewCategoryName;
}
