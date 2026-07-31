package com.mjc.hotel.hotels.controller;

import com.mjc.hotel.hotels.dto.HotelWishlistRequestDto;
import com.mjc.hotel.hotels.dto.HotelWishlistResponseDto;
import com.mjc.hotel.hotels.service.HotelWishlistService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wish")
@RequiredArgsConstructor
@Tag(name = "위시리스트", description = "회원이 호텔에 대해 찜한 목록이 나열됩니다.")
public class HotelWishlistRestController {
    private final HotelWishlistService hotelWishlistService;

    @Operation(
            summary = "회원 아이디로 위시리스트 찾기",
            description = "회원이 찜한 호텔 목록을 나열합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelWishlistResponseDto>>> findByMember(@RequestParam Long memberId) {
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "wishlist search success",
                hotelWishlistService.findByMember(memberId)
        ));
    }

    @Operation(
            summary = "회원이 호텔에 대해 찜했는지 확인",
            description = "회원이 호텔 아이디를 받아 그 호텔에 대해 찜했는지 확인합니다."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<HotelWishlistResponseDto>> status(@RequestParam Long memberId,
                                                                        @RequestParam Long hotelId) {
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "wishlist status success",
                hotelWishlistService.status(memberId, hotelId)
        ));
    }

    @Operation(
            summary = "위시리스트 등록",
            description = "위시리스트를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<HotelWishlistResponseDto>> add(@RequestBody HotelWishlistRequestDto dto) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "wishlist insert success",
                hotelWishlistService.add(dto)
        ));
    }

    @Operation(
            summary = "위시리스트 삭제 (회원 ID/호텔 ID)",
            description = "회원과 호텔 아이디를 받아 두 아이디가 일치하는 위시리스트를 삭제합니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<HotelWishlistResponseDto>> deleteByMemberAndHotel(@RequestParam Long memberId,
                                                                                        @RequestParam Long hotelId) {
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "wishlist delete success",
                hotelWishlistService.deleteByMemberAndHotel(memberId, hotelId)
        ));
    }

    @Operation(
            summary = "위시리스트 삭제",
            description = "위시리스트 ID를 바탕으로 위시리스트를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelWishlistResponseDto>> deleteById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "wishlist delete success",
                hotelWishlistService.deleteById(id)
        ));
    }
}
