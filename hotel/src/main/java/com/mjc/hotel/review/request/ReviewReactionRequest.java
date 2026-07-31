package com.mjc.hotel.review.request;

import com.mjc.hotel.review.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewReactionRequest {
    private Long reviewId;
    private Long memberId;
    private ReactionType reactionType;
}
