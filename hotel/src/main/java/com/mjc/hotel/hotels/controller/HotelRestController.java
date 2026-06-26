package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.service.HotelService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
public class HotelRestController {

    @Autowired
    private final HotelService hotelService;

    @Operation(
            summary = "호텔 데이터 생성",
            description = "호텔 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelResponseDto>> insert(@RequestBody HotelRequestDto dto) {
        HotelResponseDto insert = hotelService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 데이터 수정",
            description = "호텔 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelResponseDto>> update(@RequestBody HotelRequestDto dto) {
        HotelResponseDto update = hotelService.update(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel update success", update)
        );
    }

    @Operation(
            summary = "호텔 데이터 삭제",
            description = "호텔 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponseDto>> delete(@PathVariable Long id) {
        HotelResponseDto delete = hotelService.delete(id);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel delete success", delete)
        );
    }
}
