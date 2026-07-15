package com.mjc.hotel.payments.dto;

import lombok.Data;

@Data
public class TossPaymentFailRequestDto {
    private String orderId;
    private String code;
    private String message;
}
