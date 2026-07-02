package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.ReviewPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
    ReviewPhoto findBySidAndDeletedFalse(Long sid);
    List<ReviewPhoto> findAllByReviewSidAndDeletedFalse(Long reviewId);
    Page<ReviewPhoto> findAllByReviewSidAndDeletedFalse(Long reviewId, Pageable pageable);
    Boolean existsByReviewSid(Long sid);
}
