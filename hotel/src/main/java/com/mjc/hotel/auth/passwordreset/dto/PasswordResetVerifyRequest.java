package com.mjc.hotel.auth.passwordreset.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyRequest {

    private String email;
    private String code;
}
