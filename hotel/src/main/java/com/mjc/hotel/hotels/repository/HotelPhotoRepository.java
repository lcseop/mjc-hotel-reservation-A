package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.HotelPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelPhotoRepository extends JpaRepository<HotelPhoto, Long> {
    List<HotelPhoto> findByHotelSid(Long sid);

    @Query(value = """
        SELECT *
        FROM hotel_photo
        WHERE hotel_id = :id
        ORDER BY RAND()
        LIMIT 1
    """, nativeQuery = true)
    HotelPhoto findRandomPhoto(@Param("id") Long id);
}
