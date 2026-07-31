package com.mjc.hotel.promotion.dto;

public record PromotionDashboardDto(
    Long activePromotionCount,
    Long totalReservations,
    Long totalDiscountAmount,
    Double conversionRate
){}

