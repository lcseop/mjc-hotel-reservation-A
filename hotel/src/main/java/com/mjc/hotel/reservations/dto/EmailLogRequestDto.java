package com.mjc.hotel.reservations.dto;

import lombok.Data;

@Data
public class EmailLogRequestDto {
    private Long sid;
    private String recipientEmail;
}
