package com.mjc.hotel.reservations.dto;

import com.mjc.hotel.reservations.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {
    private Long sid;
    private String reservationNumber;
    private Long memberId;
    private String memberName;
    private Long roomId;
    private Integer roomNumber;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Integer adults;
    private Integer children;
    private ReservationStatus reservationStatus;
    private Integer totalAmount;
    private Integer totalNights;
    private String specialRequests;
    private String guestName;
    private String checkInQr;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
