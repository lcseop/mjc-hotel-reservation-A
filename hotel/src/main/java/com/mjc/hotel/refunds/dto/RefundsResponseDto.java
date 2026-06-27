package com.mjc.hotel.refunds.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class RefundsResponseDto {
    private Long refundId;
    private Long paymentId;
    private Long memberId;
    private String pgTransactionKey;
    private String idempotencyKey;
    private BigDecimal refundAmount;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String failureReason;
}
