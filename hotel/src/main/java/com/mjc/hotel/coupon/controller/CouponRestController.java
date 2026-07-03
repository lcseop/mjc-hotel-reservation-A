package com.mjc.hotel.coupon.controller;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.service.CouponService;
import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cou")
@RequiredArgsConstructor
public class CouponRestController {

    @Autowired
    private CouponService couponService;

    @Operation(
            summary = "쿠폰 생성",
            description = "쿠폰 데이터를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CouponDto>> insert(@RequestBody CouponDto dto) {
        CouponDto insert = couponService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon insert success", insert)
        );
    }

    @Operation(
            summary = "쿠폰 삭제",
            description = "쿠폰 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponDto>> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon delete success", null)
        );
    }

    @Operation(
            summary = "쿠폰 수정",
            description = "기존 쿠폰 정보를 수정합니다."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponDto>> update(
            @PathVariable Long id,
            @RequestBody CouponDto dto) {

        couponService.update(id, dto);

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon update success", null)
        );
    }
}
