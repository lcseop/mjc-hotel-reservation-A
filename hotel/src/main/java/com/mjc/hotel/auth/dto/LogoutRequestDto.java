package com.mjc.hotel.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LogoutRequestDto {
    private Long memberSid;
    private String refreshToken;
}
