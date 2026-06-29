package com.mjc.hotel.review.request;

import com.mjc.hotel.review.entity.enums.TravelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewUpdateRequest {
    private Long reviewId;

    private int rating;

    private TravelType travelType;

    private String content;

    private List<ReviewCategoryRequest> categories;

    private List<ReviewTagRequest> tags;
}
