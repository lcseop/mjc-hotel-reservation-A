package com.mjc.hotel.mail.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailVerificationResponse {
    private String email;
    private Boolean verified;
    private Long expiresInSeconds;
}
