package com.mjc.hotel.review.request;

import com.mjc.hotel.review.entity.enums.TravelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewRequest {

    private Long hotelId;

    private Long memberId;

    private Long reservationId;

    private int rating;

    private TravelType travelType;

    private String content;

    private int likeCount = 0;

    private int dislikeCount = 0;
}
