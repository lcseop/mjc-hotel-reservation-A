package com.mjc.hotel.room.controller;

import com.mjc.hotel.room.dto.RoomPhotoDto;
import com.mjc.hotel.room.dto.RoomRequestDto;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.dto.RoomTagDto;
import com.mjc.hotel.room.service.RoomService;
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
@RequestMapping("/api/room")
@RequiredArgsConstructor
@Tag(name = "Room", description = "객실 API")
public class RoomRestController {

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
            summary = "객실 데이터 조회",
            description = "객실 데이터를 가져옵니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> findById(@PathVariable(name = "id") Long hotelId) {
        RoomResponseDto search = roomService.findById(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room search success", search)
        );
    }

    @Operation(
            summary = "객실 타입별 조회",
            description = "객실 데이터를 타입 ID를 통해 가져옵니다."
    )
    @GetMapping("/type/{id}")
    public ResponseEntity<ApiResponse<List<RoomResponseDto>>> findByRoomType(@PathVariable(name = "id") Long typeId) {
        List<RoomResponseDto> search = roomService.findByRoomType(typeId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "room search success", search)
        );
    }

    @Operation(
            summary = "객실의 태그들 조회",
            description = "객실의 태그들을 가져옵니다."
    )
    @GetMapping("/intag/{id}")
    public ResponseEntity<ApiResponse<List<RoomTagDto>>> findRoomInTags(@PathVariable(name = "id") Long hotelId) {
        List<RoomTagDto> search = roomService.findRoomInTags(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in tags search success", search)
        );
    }

    @Operation(
            summary = "객실의 사진들 조회",
            description = "객실의 사진 경로들을 가져옵니다."
    )
    @GetMapping("/inimage/{id}")
    public ResponseEntity<ApiResponse<List<RoomPhotoDto>>> findRoomInImages(@PathVariable(name = "id") Long hotelId) {
        List<RoomPhotoDto> search = roomService.findRoomInImages(hotelId);
        return ResponseEntity.status(200).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "hotel in images search success", search)
        );
    }
}
