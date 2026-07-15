package com.mjc.hotel.reservations.dto;

import com.mjc.hotel.reservations.entity.PointStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponseDto {
    private Long sid;
    private Long reservationId;
    private String reservationNumber;
    private Long hotelId;
    private String hotelName;
    private String roomName;
    private Integer amount;
    private PointStatus pointStatus;
    private LocalDateTime createdAt;
}
