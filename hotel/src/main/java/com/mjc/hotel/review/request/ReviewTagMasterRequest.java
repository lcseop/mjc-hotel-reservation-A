package com.mjc.hotel.review.request;

import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewTagMasterRequest {
    private Long sid;
    private String reviewTagName;
    private ReviewTagCategory reviewTagCategory;

}
