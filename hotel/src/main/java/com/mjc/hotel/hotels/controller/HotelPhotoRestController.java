package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelPhotoDto;
import com.mjc.hotel.hotels.service.HotelPhotoService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotelphoto")
@RequiredArgsConstructor
@Tag(name = "호텔 이미지", description = "호텔의 이미지 데이터를 관리합니다.")
public class HotelPhotoRestController {
    @Autowired
    private HotelPhotoService hotelPhotoService;

    @Operation(
            summary = "호텔 이미지 생성",
            description = "호텔 이미지를 생성합니다.\nimagePath에 이미지 경로 주소를 기입하세요."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelPhotoDto>> insert(@RequestBody HotelPhotoDto dto) {
        HotelPhotoDto insert = hotelPhotoService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel photo insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 이미지 수정",
            description = "호텔 이미지를 수정합니다.\nimagePath에 이미지 경로 주소를 기입하세요."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelPhotoDto>> update(@RequestBody HotelPhotoDto dto) {
        HotelPhotoDto update = hotelPhotoService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel photo update success", update)
        );
    }

    @Operation(
            summary = "호텔 이미지 삭제",
            description = "호텔 이미지 데이터를 삭제합니다.\n이미 연결된 호텔이 있다면 삭제가 불가합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelPhotoDto>> delete(@PathVariable Long id) {
        HotelPhotoDto delete = hotelPhotoService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel photo delete success", delete)
        );
    }

    @Operation(
            summary = "호텔 이미지 가져오기",
            description = "알맞은 id의 호텔 이미지 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelPhotoDto>> findById(@PathVariable Long id) {
        HotelPhotoDto dto = hotelPhotoService.findById(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel photo get success", dto)
        );
    }
}
