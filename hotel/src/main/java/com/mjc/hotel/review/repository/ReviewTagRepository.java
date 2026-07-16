package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewTag;
import com.mjc.hotel.review.entity.composite_key.ReviewTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTag, ReviewTagId> {
    void deleteByReviewSid(Long reviewReviewId);
    List<ReviewTag> findByReviewSid(Long reviewReviewId);
}
