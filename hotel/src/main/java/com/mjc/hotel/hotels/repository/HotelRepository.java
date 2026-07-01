package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, HotelRepositorySub {

}
