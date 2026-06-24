package com.mjc.hotel.reservations.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReservationCancel {

    private Long cancelId;
    private Long reservationId;
    private String cancelReason;
    private Long refundAmount;
    private LocalDateTime cancelledAt;
}
