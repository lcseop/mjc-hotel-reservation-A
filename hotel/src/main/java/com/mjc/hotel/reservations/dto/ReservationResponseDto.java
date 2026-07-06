package com.mjc.hotel.reservations.dto;

import com.mjc.hotel.reservations.entity.ReservationChannel;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private String guestName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Integer totalNights;
    private Integer adults;
    private Integer children;
    private Integer originalAmount;
    private Integer discountAmount;
    private Integer couponDiscount;
    private Integer pointDiscount;
    private Integer totalAmount;
    private Integer earnedPoint;
    private ReservationStatus reservationStatus;
    private ReservationChannel reservationChannel;
    private String specialRequests;
    private String checkInQr;
    private List<CancellationPolicyDto> cancellationPolicyDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
