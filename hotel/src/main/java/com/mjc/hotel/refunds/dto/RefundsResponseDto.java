package com.mjc.hotel.refunds.dto;

import com.mjc.hotel.refunds.entity.RefundStatus;
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
    private Long sid;
    private String pgTransactionKey;
    private String idempotencyKey;
    private BigDecimal refundAmount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
