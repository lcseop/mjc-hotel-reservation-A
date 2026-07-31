package com.mjc.hotel.promotion.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionStatsDto {
    private Long promotionSid;
    private Long reservationCount;
    private Long totalDiscountAmount;

    public Long getReservationCount() { return reservationCount; }
    public Long getTotalDiscountAmount() { return totalDiscountAmount; }
}