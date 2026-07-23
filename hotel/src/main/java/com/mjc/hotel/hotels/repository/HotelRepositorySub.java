package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelRepositorySub {
    Page<HotelResponseDto> search(HotelSearchRequestDto dto, Pageable pageable);
}
