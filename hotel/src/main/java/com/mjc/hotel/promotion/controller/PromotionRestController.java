package com.mjc.hotel.promotion.controller;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.dto.PromotionSearchRequestDto;
import com.mjc.hotel.promotion.service.PromotionService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/prom")
@RequiredArgsConstructor
public class PromotionRestController {

    @Autowired
    private PromotionService promotionService;

    @Operation(
            summary = "프로모션 데이터 생성",
            description = "프로모션 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionDto>> addPromotion(@RequestBody PromotionDto dto) {
        PromotionDto insert = promotionService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "promotion insert success", insert)
        );
    }

    @Operation(
            summary = "프로모션 데이터 수정",
            description = "프로모션 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<PromotionDto>> update(@RequestBody PromotionDto dto) {
        PromotionDto update = promotionService.update(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "promotion update success", update)
        );
    }

    @Operation(
            summary = "프로모션 데이터 삭제",
            description = "프로모션 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionDto>> delete(@PathVariable Long id) {
        PromotionDto delete = promotionService.delete(id);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "promotion delete success", delete)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PromotionDto>> searchPromotions(PromotionSearchRequestDto req, Pageable pageable) {
        return ResponseEntity.ok(promotionService.search(req, pageable));
    }
}
