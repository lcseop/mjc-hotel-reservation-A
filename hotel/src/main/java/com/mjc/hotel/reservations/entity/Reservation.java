package com.mjc.hotel.reservations.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reservation {
    private Long reservationId;
    private Long memberId;
    private Long roomId;
    private Long refundId;

    private String reservationNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer adults;
    private Integer children;

    private ReservationStatus reservationStatus;

    private Integer totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String checkInQr;
    private Long totalNights;
    private String guestName;

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CHECKED_IN,
        CHECKED_OUT,
        UPCOMING,
        COMPLETED,
        CANCELED,
        NO_SHOW
    }

}
