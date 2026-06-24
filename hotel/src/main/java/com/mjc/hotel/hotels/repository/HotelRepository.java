package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

}
