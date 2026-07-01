package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelRepositorySub {
    Page<Hotel> search(HotelSearchRequestDto dto, Pageable pageable);
}
