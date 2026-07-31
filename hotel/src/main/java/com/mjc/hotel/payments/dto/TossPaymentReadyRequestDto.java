package com.mjc.hotel.payments.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TossPaymentReadyRequestDto {
    private Long memberId;
    private Long reservationId;
    private BigDecimal amount;
    private String orderName;
}
