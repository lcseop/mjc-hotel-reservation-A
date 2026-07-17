package com.mjc.hotel.review.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewWriteStatusResponse {
    private Boolean checked;
    //회원이 같은 객실로 날짜를 다르게 해서 여러번 예약을 할 수 있으므로 List로 받음
    private List<Long> reservationIds;
    private List<Boolean> existsReviews;
    private List<Long> reviewIds;
}
