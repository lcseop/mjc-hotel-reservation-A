package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelInAmenitiesDto;
import com.mjc.hotel.hotels.dto.HotelInAmenitiesRequestDto;
import com.mjc.hotel.hotels.service.HotelInAmenitiesService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoteliname")
@RequiredArgsConstructor
@Tag(name = "호텔 편의 시설 매핑", description = "호텔과 편의 시설 매핑 데이터를 관리합니다.")
public class HotelInAmenitiesRestController {
    private final HotelInAmenitiesService hotelInAmenitiesService;

    @Operation(
            summary = "호텔 안의 편의시설 생성",
            description = "호텔에 편의시설을 추가시킵니다.\nhotelId에는 호텔 아이디, amenitiesId에는 편의 시설 ID를 기입하세요."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelInAmenitiesDto>> insert(@RequestBody HotelInAmenitiesRequestDto dto) {
        HotelInAmenitiesDto insert = hotelInAmenitiesService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities insert success", insert)
        );
    }

    @Operation(
            summary = "호텔 안의 편의시설 수정",
            description = "호텔의 편의시설 매핑 정보를 수정합니다.\nhotelId에는 호텔 아이디, amenitiesId에는 편의 시설 ID를 기입하세요."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<HotelInAmenitiesDto>> update(@RequestBody HotelInAmenitiesRequestDto dto) {
        HotelInAmenitiesDto update = hotelInAmenitiesService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities update success", update)
        );
    }

    @Operation(
            summary = "호텔 안의 편의시설 삭제",
            description = "호텔의 편의시설 매핑 데이터를 완전히 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelInAmenitiesDto>> delete(@PathVariable Long id) {
        HotelInAmenitiesDto delete = hotelInAmenitiesService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities delete success", delete)
        );
    }

    @Operation(
            summary = "호텔 안의 편의시설 가져오기",
            description = "알맞은 id의 호텔과 편의시설 매핑 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelInAmenitiesDto>> findById(@PathVariable Long id) {
        HotelInAmenitiesDto dto = hotelInAmenitiesService.findById(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities get success", dto)
        );
    }

    @Operation(
            summary = "호텔별 편의시설 매핑 목록 조회",
            description = "호텔 ID에 연결된 편의시설 매핑 목록을 가져옵니다."
    )
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<HotelInAmenitiesDto>>> findByHotelId(@PathVariable Long hotelId) {
        List<HotelInAmenitiesDto> dto = hotelInAmenitiesService.findByHotelId(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in amenities list success", dto)
        );
    }
}
