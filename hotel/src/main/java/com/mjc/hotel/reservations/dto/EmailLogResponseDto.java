package com.mjc.hotel.reservations.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailLogResponseDto {
    private Long sid;
    private Long reservationSid;
    private String recipientEmail;
    private String emailStatus;
    private LocalDateTime sentAt;

}
