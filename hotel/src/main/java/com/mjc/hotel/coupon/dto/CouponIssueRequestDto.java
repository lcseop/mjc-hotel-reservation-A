package com.mjc.hotel.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueRequestDto {
    private Long couponId;
    private Long memberId;
}
