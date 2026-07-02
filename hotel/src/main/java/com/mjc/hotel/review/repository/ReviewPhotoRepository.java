package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewAnswer;
import com.mjc.hotel.review.entity.ReviewPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
    ReviewPhoto findBySidAndDeletedFalse(Long sid);
    List<ReviewPhoto> findByReviewSidAndDeletedFalse(Long reviewId);
}
