package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCategoryRepository extends JpaRepository<ReviewCategory, Long> {
    public void deleteByReviewReviewId(Long reviewId);

    List<ReviewCategory> findByReviewReviewId(Long reviewId);
}
