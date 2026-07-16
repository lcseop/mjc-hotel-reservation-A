package com.mjc.hotel.coupon.dto;

import lombok.Data;

@Data
public class CouponIssueRequestDto {
    private Long couponId;
    private Long memberId;
}
