package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.dto.RoomTypeDto;
import com.mjc.hotel.room.service.RoomTypeService;
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
@RequestMapping("/api/roomtype")
@RequiredArgsConstructor
@Tag(name = "객실 타입", description = "객실 타입 데이터를 관리합니다.")
public class RoomTypeRestController {
    private final RoomTypeService roomTypeService;

    @Operation(
            summary = "객실 타입 생성",
            description = "객실 타입을 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoomTypeDto>> insert(@RequestBody RoomTypeDto dto) {
        RoomTypeDto insert = roomTypeService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room type insert success", insert)
        );
    }

    @Operation(
            summary = "객실 타입 수정",
            description = "객실 타입을 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<RoomTypeDto>> update(@RequestBody RoomTypeDto dto) {
        RoomTypeDto update = roomTypeService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room type update success", update)
        );
    }

    @Operation(
            summary = "객실 타입 삭제",
            description = "객실 타입을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTypeDto>> delete(@PathVariable Long id) {
        RoomTypeDto delete = roomTypeService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room type delete success", delete)
        );
    }

    @Operation(
            summary = "객실 타입 조회",
            description = "객실 타입을 가져옵니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomTypeDto>>> findAll() {
        List<RoomTypeDto> search = roomTypeService.findAll();
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room type search success", search)
        );
    }

    @Operation(
            summary = "객실 타입 단일 조회",
            description = "객실 타입 한 개를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTypeDto>> findById(@PathVariable(name = "id") Long hotelId) {
        RoomTypeDto search = roomTypeService.findById(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room type search success", search)
        );
    }
}
