package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.HotelWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelWishlistRepository extends JpaRepository<HotelWishlist, Long> {

    Optional<HotelWishlist> findByMemberSidAndHotelSid(Long memberId, Long hotelId);

    @Query("""
            select w
            from HotelWishlist w
            join fetch w.hotel h
            left join fetch h.type
            where w.member.sid = :memberId
              and (w.deleted = false or w.deleted is null)
            order by w.createdAt desc
            """)
    List<HotelWishlist> findActiveByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberSidAndHotelSidAndDeletedFalse(Long memberId, Long hotelId);
}
