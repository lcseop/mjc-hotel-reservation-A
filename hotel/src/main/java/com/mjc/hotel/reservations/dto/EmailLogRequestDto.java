package com.mjc.hotel.reservations.dto;

import lombok.Data;

@Data
public class EmailLogRequestDto {
    private Long reservationSid;
    private String recipientEmail;
}
