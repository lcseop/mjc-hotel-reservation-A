package com.mjc.hotel.review.response;

import com.mjc.hotel.review.entity.enums.TravelType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewResponse {
    private Long sid;

    private Long hotelId;

    private String hotelName;

    private String hotelLocation;

    private Integer hotelStarRating;

    private Long memberId;

    private Long reservationId;

    private Integer rating;

    private TravelType travelType;

    private String content;

    private Integer likeCount;

    private Integer dislikeCount;

    private String roomName;

    private Integer totalNights;

    private List<ReviewCategoryResponse> categories;

    private List<ReviewTagResponse> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Boolean deleted;
}
