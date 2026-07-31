package com.mjc.hotel.payments.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TossPaymentConfirmRequestDto {
    private Long reservationId;
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
}
