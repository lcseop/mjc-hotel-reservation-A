package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.dto.RoomTagDto;
import com.mjc.hotel.room.service.RoomTagService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roomtag")
@RequiredArgsConstructor
@Tag(name = "RoomTag", description = "객실 태그 API")
public class RoomTagRestController {

    @Autowired
    private final RoomTagService roomTagService;

    @Operation(
            summary = "객실 태그 삭제",
            description = "객실 태그를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoomTagDto>> insert(@RequestBody RoomTagDto dto) {
        RoomTagDto insert = roomTagService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room tag insert success", insert)
        );
    }

    @Operation(
            summary = "객실 태그 수정",
            description = "객실 태그를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<RoomTagDto>> update(@RequestBody RoomTagDto dto) {
        RoomTagDto update = roomTagService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room tag update success", update)
        );
    }

    @Operation(
            summary = "객실 태그 삭제",
            description = "객실 태그를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTagDto>> delete(@PathVariable Long id) {
        RoomTagDto delete = roomTagService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room tag delete success", delete)
        );
    }

    @Operation(
            summary = "객실 태그 조회",
            description = "객실 태그를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomTagDto>> findById(@PathVariable(name = "id") Long hotelId) {
        RoomTagDto search = roomTagService.findById(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room tag search success", search)
        );
    }
}
