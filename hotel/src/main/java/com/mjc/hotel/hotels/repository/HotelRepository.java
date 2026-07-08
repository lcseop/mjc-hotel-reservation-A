package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, HotelRepositorySub {
    boolean existsByTypeSid(Long sid);

    boolean existsByHotelNameAndLocation(String hotelName, String location);

    @Query("SELECT h FROM hotel h ORDER BY RAND() LIMIT 4")
    List<Hotel> findTop4Popular();
}
