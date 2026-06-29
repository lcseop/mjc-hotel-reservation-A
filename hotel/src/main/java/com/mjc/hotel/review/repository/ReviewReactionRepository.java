package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewReaction;
import com.mjc.hotel.review.entity.composite_key.ReviewReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, ReviewReactionId> {
}
