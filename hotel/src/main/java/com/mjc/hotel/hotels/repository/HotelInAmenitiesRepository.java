package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.HotelInAmenities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelInAmenitiesRepository extends JpaRepository<HotelInAmenities, Long> {
    List<HotelInAmenities> findByHotelSid(Long hotelId);
}
