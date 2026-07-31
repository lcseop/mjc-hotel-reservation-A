package com.mjc.hotel.payments.dto;

import com.mjc.hotel.payments.entity.PaymentMethod;
import com.mjc.hotel.payments.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class PaymentsResponseDto {
    private Long sid;
    private Long reservationId;
    private Long memberSid;
    private BigDecimal paymentAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionNo;
    private String orderId;
    private String paymentKey;
    private String provider;
    private String receiptUrl;
    private String failCode;
    private String failMessage;
    private LocalDateTime paidAt;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private Integer point;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
