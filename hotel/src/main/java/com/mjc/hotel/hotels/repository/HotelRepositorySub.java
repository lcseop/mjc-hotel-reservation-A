package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;

import java.util.List;

public interface HotelRepositorySub {
    List<Hotel> search(HotelSearchRequestDto dto);
}
