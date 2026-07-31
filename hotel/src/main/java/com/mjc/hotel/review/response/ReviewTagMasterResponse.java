package com.mjc.hotel.review.response;

import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewTagMasterResponse {
    private Long sid;
    private String reviewTagName;
    private ReviewTagCategory reviewTagCategory;
}
