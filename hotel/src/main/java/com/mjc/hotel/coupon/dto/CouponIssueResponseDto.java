package com.mjc.hotel.coupon.dto;

import com.mjc.hotel.coupon.entity.CouponDiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CouponIssueResponseDto {
    private Long sid;
    private Long couponId;
    private String couponName;
    private CouponDiscountType discountType;
    private Double discountValue;
    private Integer minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isUsed;
    private LocalDateTime usedAt;
}
