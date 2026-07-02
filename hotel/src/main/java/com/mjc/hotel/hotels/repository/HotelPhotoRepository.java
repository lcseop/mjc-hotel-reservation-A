package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.HotelPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelPhotoRepository extends JpaRepository<HotelPhoto, Long> {
    List<HotelPhoto> findByHotelSid(Long sid);
}
