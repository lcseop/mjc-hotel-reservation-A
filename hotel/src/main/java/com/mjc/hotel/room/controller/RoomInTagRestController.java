package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.dto.RoomInTagDto;
import com.mjc.hotel.room.service.RoomInTagService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roomintag")
@RequiredArgsConstructor
@Tag(name = "RoomTag", description = "객실과 태그 매핑 API")
public class RoomInTagRestController {

    @Autowired
    private final RoomInTagService roomInTagService;

    @Operation(
            summary = "객실 태그 매핑 데이터 삭제",
            description = "객실과 태그의 매핑 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoomInTagDto>> insert(@RequestBody RoomInTagDto dto) {
        RoomInTagDto insert = roomInTagService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room in tag insert success", insert)
        );
    }

    @Operation(
            summary = "객실 태그 매핑 데이터 수정",
            description = "객실과 태그의 매핑 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<RoomInTagDto>> Update(@RequestBody RoomInTagDto dto) {
        RoomInTagDto update = roomInTagService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room in tag update success", update)
        );
    }

    @Operation(
            summary = "객실 태그 매핑 데이터 삭제",
            description = "객실과 태그의 매핑 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomInTagDto>> delete(@PathVariable Long id) {
        RoomInTagDto delete = roomInTagService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room in tag delete success", delete)
        );
    }

    @Operation(
            summary = "객실 태그 매핑 데이터 조회",
            description = "객실과 태그의 매핑 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomInTagDto>> findById(@PathVariable(name = "id") Long hotelId) {
        RoomInTagDto search = roomInTagService.findById(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room in tag search success", search)
        );
    }
}
