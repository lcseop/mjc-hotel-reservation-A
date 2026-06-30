package com.mjc.hotel.room.controller;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.dto.RoomRequestDto;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.mapper.RoomMapper;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.service.RoomService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    @Autowired
    private final RoomService roomService;

    @Operation(
            summary = "객실 데이터 생성",
            description = "객실 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> insert(@RequestBody RoomRequestDto dto) {
        RoomResponseDto insert = roomService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room insert success", insert)
        );
    }

    @Operation(
            summary = "객실 데이터 수정",
            description = "객실 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> update(@RequestBody RoomRequestDto dto) {
        RoomResponseDto update = roomService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room update success", update)
        );
    }

    @Operation(
            summary = "객실 데이터 삭제",
            description = "객실 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> delete(@PathVariable Long id) {
        RoomResponseDto delete = roomService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room delete success", delete)
        );
    }

    @Operation(
            summary = "호텔의 모든 객실 조회",
            description = "호텔 ID를 받아 모든 객실을 가져옵니다."
    )
    @GetMapping("/in/{id}")
    public ResponseEntity<ApiResponse<List<RoomResponseDto>>> search(@PathVariable(name = "id") Long hotelId) {
        List<RoomResponseDto> search = roomService.findByHotelId(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in room search success", search)
        );
    }
}
