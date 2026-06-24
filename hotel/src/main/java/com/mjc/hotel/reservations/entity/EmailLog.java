package com.mjc.hotel.reservations.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmailLog {

    private Long emailLogId;
    private Long reservationId;
    private String recipientEmail;
    private EmailStatus status;
    private LocalDateTime sentAt;

    public enum EmailStatus {
        PENDING,
        SEND,
        FAILED
    }
}
