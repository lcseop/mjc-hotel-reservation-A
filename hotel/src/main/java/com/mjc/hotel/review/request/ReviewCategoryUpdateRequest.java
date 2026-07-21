package com.mjc.hotel.review.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewCategoryUpdateRequest {
    private Long sid;
    private Long categoryId;
    private Integer rating;
}
