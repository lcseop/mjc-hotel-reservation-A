package com.mjc.hotel.review.response;

import com.mjc.hotel.review.entity.enums.ReactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewReactionResponse {
    private Long reviewId;
    private Long memberId;
    private ReactionType reactionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
