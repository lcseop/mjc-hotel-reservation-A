package com.mjc.hotel.reservations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatsDto {
    private Long todayNewReservations;
    private Long todayCheckIns;
    private Long todayCheckOuts;
    private Long monthlyReservations;
    private Long pendingCount;
}
