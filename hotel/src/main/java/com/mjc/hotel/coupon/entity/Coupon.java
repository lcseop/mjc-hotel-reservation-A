package com.mjc.hotel.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "coupon")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(length = 100, name="coupon_name", nullable = false)
    private String couponName;

    @Enumerated(EnumType.STRING)
    @Column(name="discount_type")
    private CouponDiscountType discountType;

    @Column(name="discount_value")
    private Double discountValue;

    @Column(name="min_order_amount")
    private Integer minOrderAmount;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="end_date")
    private LocalDateTime endDate;

    @Column(name="total_quantity")
    private Integer totalQuantity;

    public void update(String couponName, CouponDiscountType discountType, Double discountValue, Integer minOrderAmount, LocalDateTime startDate, LocalDateTime endDate, Integer totalQuantity) {
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalQuantity = totalQuantity;
    }
}
