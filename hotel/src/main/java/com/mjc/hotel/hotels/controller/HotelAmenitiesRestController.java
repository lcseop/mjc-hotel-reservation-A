package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelAmenitiesDto;
import com.mjc.hotel.hotels.service.HotelAmenitiesService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotelame")
@RequiredArgsConstructor
public class HotelAmenitiesRestController {
    @Autowired
    private HotelAmenitiesService hotelAmenitiesService;

    @Operation(
            summary = "호텔 편의시설 생성",
            description = "호텔의 편의시설 데이터를 추가합니다.\n편의시설의 title에는 제목이, description에는 간단한 설명이 제공됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelAmenitiesDto>> insert(@RequestBody HotelAmenitiesDto dto) {
        HotelAmenitiesDto insert = hotelAmenitiesService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "amenities insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 편의시설 수정",
            description = "호텔의 편의시설 데이터를 수정합니다.\n편의시설의 title에는 제목이, description에는 간단한 설명이 제공됩니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelAmenitiesDto>> update(@RequestBody HotelAmenitiesDto dto) {
        HotelAmenitiesDto update = hotelAmenitiesService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "amenities update success", update)
        );
    }

    @Operation(
            summary = "호텔 편의시설 삭제",
            description = "호텔의 편의시설 데이터를 완전히 삭제하고, HotelInAmenities의 매핑 정보도 같이 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelAmenitiesDto>> delete(@PathVariable Long id) {
        HotelAmenitiesDto delete = hotelAmenitiesService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "amenities delete success", delete)
        );
    }

    @Operation(
            summary = "호텔 편의시설 가져오기",
            description = "알맞은 id의 호텔의 편의시설 데이터를 가져옵니다.\n편의시설의 title에는 제목이, description에는 간단한 설명이 제공됩니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelAmenitiesDto>> findById(@PathVariable Long id) {
        HotelAmenitiesDto dto = hotelAmenitiesService.findById(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "amenities get success", dto)
        );
    }
}
