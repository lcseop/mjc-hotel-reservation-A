package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findBySidAndDeletedFalse(Long sid, Pageable pageable);
    Review findBySidAndDeletedFalse(Long sid);
    Page<Review> findByHotelSid(Long hotelId, Pageable pageable);
    Page<Review> findByMemberSid(Long memberId, Pageable pageable);
}
