package com.mjc.hotel.mail.dto;

import lombok.Data;

@Data
public class EmailVerificationConfirmRequest {
    private String email;
    private String code;
}
