package com.mjc.hotel.coupon.dto;

import com.mjc.hotel.coupon.entity.CouponDiscountType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CouponDto {
    private Long sid;
    private String couponName;
    private CouponDiscountType discountType;
    private Double discountValue;
    private Integer minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalQuantity;
}
