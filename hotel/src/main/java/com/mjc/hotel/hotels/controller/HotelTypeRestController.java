package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.service.HotelTypeService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hoteltype")
@RequiredArgsConstructor
public class HotelTypeRestController {
    @Autowired
    private HotelTypeService hotelTypeService;

    @Operation(
            summary = "호텔 타입 생성",
            description = "호텔 타입을 생성합니다.\ntitle에 호텔 타입명을 기입하세요. (호텔, 모텔, 펜션, 리조트 등)"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelTypeDto>> insert(@RequestBody HotelTypeDto dto) {
        HotelTypeDto insert = hotelTypeService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel type insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 타입 수정",
            description = "호텔 타입을 수정합니다.\ntitle에 호텔 타입명을 기입하세요. (호텔, 모텔, 펜션, 리조트 등)"
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelTypeDto>> update(@RequestBody HotelTypeDto dto) {
        HotelTypeDto update = hotelTypeService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel type update success", update)
        );
    }

    @Operation(
            summary = "호텔 타입 삭제",
            description = "호텔 타입 데이터를 완전히 삭제합니다.\n이미 연결된 호텔이 있다면 삭제가 불가합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelTypeDto>> delete(@PathVariable Long id) {
        HotelTypeDto delete = hotelTypeService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel type delete success", delete)
        );
    }

    @Operation(
            summary = "호텔 타입 가져오기",
            description = "알맞은 id의 호텔 타입 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelTypeDto>> findById(@PathVariable Long id) {
        HotelTypeDto dto = hotelTypeService.findById(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel type get success", dto)
        );
    }
}
