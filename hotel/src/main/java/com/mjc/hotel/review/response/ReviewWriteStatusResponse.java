package com.mjc.hotel.review.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewWriteStatusResponse {
    private Boolean checked;
    private Long reservationId;

    private Boolean existsReview;
    private Long reviewId;
}
