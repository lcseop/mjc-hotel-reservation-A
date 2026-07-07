package com.mjc.hotel.reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPolicyDto {
    private String periodDescription;
    private Integer refundPercentage;
    private Integer expectedRefundAmount;
    private boolean applicable;
}
