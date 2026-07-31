package com.mjc.hotel.review.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ReviewAnswerResponse {
    private Long sid;

    private Long reviewId;

    private String reviewAnswer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Boolean deleted;
}
