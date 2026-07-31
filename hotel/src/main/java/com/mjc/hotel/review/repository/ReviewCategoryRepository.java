package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCategoryRepository extends JpaRepository<ReviewCategory, Long> {
    void deleteByReviewSid(Long reviewId);

    List<ReviewCategory> findByReviewSid(Long reviewId);
}
