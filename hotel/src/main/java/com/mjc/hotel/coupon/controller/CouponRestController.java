package com.mjc.hotel.coupon.controller;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.service.CouponService;
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
public class CouponRestController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponDto>> insert(@RequestBody CouponDto dto) {
        CouponDto insert = couponService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon insert success", insert)
        );
    }
}
