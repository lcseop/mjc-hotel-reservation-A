package com.mjc.hotel.promotion.controller;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.service.PromotionService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
public class PromotionRestController {

    @Autowired
    private PromotionService promotionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionDto>> addPromotion(@RequestBody PromotionDto dto) {
        PromotionDto insert = promotionService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "promotion insert success", insert)
        );
    }
}
