package com.mjc.hotel.auth.dto;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long refreshTokenExpiresIn;
    private Long memberSid;
    private String email;
    private String name;
    private MemberRole role;
    private Integer point;
    private MemberAuthProvider provider;
}
