package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotelController {

    private final HotelMapper hotelMapper;
    private final HotelRepository hotelRepository;

    @GetMapping("/mapperHotels")
    public List<Hotel> getHotels() {
        return hotelMapper.getHotels();
    }

    @GetMapping("/repositoryHotels")
    public List<Hotel> getHotelRepository() {
        return hotelRepository.findAll();
    }
}
