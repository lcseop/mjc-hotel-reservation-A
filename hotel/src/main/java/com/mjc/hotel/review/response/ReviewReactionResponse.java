package com.mjc.hotel.review.response;

import com.mjc.hotel.review.entity.enums.ReactionType;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ReviewReactionResponse {
    private Long reviewId;
    private Long memberId;
    private ReactionType reactionType;
}
