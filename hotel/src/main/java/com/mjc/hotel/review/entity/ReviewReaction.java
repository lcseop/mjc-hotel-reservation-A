package com.mjc.hotel.review.entity;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.review.entity.composite_key.ReviewReactionId;
import com.mjc.hotel.review.entity.enums.ReactionType;
import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_reaction")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@IdClass(ReviewReactionId.class)
public class ReviewReaction extends  BaseEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;
}
