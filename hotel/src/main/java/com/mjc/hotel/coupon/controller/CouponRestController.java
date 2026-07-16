package com.mjc.hotel.coupon.controller;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.dto.CouponIssueRequestDto;
import com.mjc.hotel.coupon.dto.CouponIssueResponseDto;
import com.mjc.hotel.coupon.service.CouponService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cou")
@RequiredArgsConstructor
@Tag(name = "쿠폰 관리", description = "보유 중인 쿠폰 관리")
public class CouponRestController {

    @Autowired
    private CouponService couponService;

    @Operation(
            summary = "쿠폰 전체 조회",
            description = "관리자 화면에서 발급할 쿠폰 목록을 가져옵니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponDto>>> findAll() {
        List<CouponDto> coupons = couponService.findAll();
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon search success", coupons)
        );
    }

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

    @Operation(
            summary = "회원 보유 사용 가능 쿠폰 조회",
            description = "예약 화면에서 사용할 수 있는 회원의 미사용 쿠폰을 가져옵니다."
    )
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<CouponIssueResponseDto>>> findUsableByMember(@PathVariable Long memberId) {
        List<CouponIssueResponseDto> coupons = couponService.findUsableByMember(memberId);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon issue search success", coupons)
        );
    }

    @Operation(
            summary = "회원 쿠폰 발급",
            description = "선택한 회원에게 쿠폰을 발급합니다."
    )
    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<CouponIssueResponseDto>> issue(@RequestBody CouponIssueRequestDto dto) {
        CouponIssueResponseDto issue = couponService.issue(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "coupon issue success", issue)
        );
    }
}
