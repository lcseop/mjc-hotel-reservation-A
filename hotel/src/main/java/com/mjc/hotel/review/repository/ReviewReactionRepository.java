package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewReaction;
import com.mjc.hotel.review.entity.composite_key.ReviewReactionId;
import com.mjc.hotel.review.entity.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, ReviewReactionId> {
    List<ReviewReaction> findByReviewSidAndReactionType(Long reviewId, ReactionType reactionType);


}
