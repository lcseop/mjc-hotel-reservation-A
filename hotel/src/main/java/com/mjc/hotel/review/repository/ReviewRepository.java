package com.mjc.hotel.review.repository;

import com.mjc.hotel.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findBySidAndDeletedFalse(Long sid);

    Page<Review> findByHotelSid(Long hotelId, Pageable pageable);
    Page<Review> findByMemberSid(Long memberId, Pageable pageable);

    Boolean existsByReservationSid(Long reservationId);

    Page<Review> findByHotelSidAndDeletedFalse(Long hotelId, Pageable pageable);

    Page<Review> findByHotelSidAndRatingGreaterThanEqualAndDeletedFalse(Long hotelId, Integer rating,Pageable pageable);

    @Query("select distinct r from review r inner join review_photo p on r.sid = p.review.sid where r.hotel.sid = :hotelId and r.deleted = false")
    Page<Review> findByHotelSidAndExistsPhotoAndDeletedFalse(Long hotelId, Pageable pageable);

    @Modifying
    @Query("UPDATE review r SET r.deleted = true, r.deletedAt = local_datetime WHERE r.sid = :sid")
    void softDeleteById(@Param("sid") Long sid); // 논리삭제 쿼리

    Review findByReservationSidAndDeletedFalse(Long sid);
}
