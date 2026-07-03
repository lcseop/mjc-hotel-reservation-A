package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.dto.RoomPhotoDto;
import com.mjc.hotel.room.service.RoomPhotoService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roomphoto")
@RequiredArgsConstructor
@Tag(name = "RoomPhoto", description = "객실 이미지 API")
public class RoomPhotoRestController {

    @Autowired
    private final RoomPhotoService roomPhotoService;

    @Operation(
            summary = "객실 이미지 삭제",
            description = "객실 이미지를 만듭니다.\nroomId에 객실 아이디를, imagePath에 이미지 경로 주소를 기입하세요."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoomPhotoDto>> insert(@RequestBody RoomPhotoDto dto) {
        RoomPhotoDto insert = roomPhotoService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room photo insert success", insert)
        );
    }

    @Operation(
            summary = "객실 이미지 수정",
            description = "객실 이미지를 수정합니다.\nroomId에 객실 아이디를, imagePath에 이미지 경로 주소를 기입하세요."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<RoomPhotoDto>> update(@RequestBody RoomPhotoDto dto) {
        RoomPhotoDto update = roomPhotoService.update(dto);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room photo update success", update)
        );
    }

    @Operation(
            summary = "객실 이미지 삭제",
            description = "객실 이미지를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomPhotoDto>> delete(@PathVariable Long id) {
        RoomPhotoDto delete = roomPhotoService.delete(id);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room photo delete success", delete)
        );
    }

    @Operation(
            summary = "객실 이미지 조회",
            description = "객실 이미지를 가져옵니다.\nroomId는 객실 아이디를, imagePath는 이미지 경로 주소를 나타냅니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomPhotoDto>> findById(@PathVariable(name = "id") Long hotelId) {
        RoomPhotoDto search = roomPhotoService.findById(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room photo search success", search)
        );
    }
}
