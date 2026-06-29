package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long>, HotelRepositorySub {

}
