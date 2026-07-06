package com.mjc.hotel.reservations.dto;

import com.mjc.hotel.reservations.entity.EmailType;
import lombok.Data;

@Data
public class EmailLogRequestDto {
    private Long sid;
    private String recipientEmail;
    private EmailType emailType;
}
