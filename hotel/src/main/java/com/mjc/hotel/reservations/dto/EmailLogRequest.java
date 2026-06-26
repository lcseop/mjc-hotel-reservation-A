package com.mjc.hotel.reservations.dto;

import lombok.Data;

@Data
public class EmailLogRequest {
    private Long reservationSid;
    private String recipientEmail;
}
