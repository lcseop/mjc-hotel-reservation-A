package com.mjc.hotel.payments.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TossPaymentReadyResponseDto {
    private Long paymentId;
    private Long reservationId;
    private String orderId;
    private String orderName;
    private BigDecimal amount;
}
