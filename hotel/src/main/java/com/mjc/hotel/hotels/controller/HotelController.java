package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotelController {

    private final HotelMapper hotelMapper;
    private final HotelRepository hotelRepository;

    @Operation(
            summary = "호텔 맵퍼 조회",
            description = "맵퍼를 이용해 호텔을 조회합니다."
    )
    @GetMapping("/mapperHotels")
    public List<Hotel> getHotels() {
        return hotelMapper.getHotels();
    }

    @Operation(
            summary = "호텔 레포지토리 조회",
            description = "레포지토리를 이용해 호텔을 조회합니다."
    )
    @GetMapping("/repositoryHotels")
    public List<Hotel> getHotelRepository() {
        return hotelRepository.findAll();
    }
}
