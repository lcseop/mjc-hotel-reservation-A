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
public class OAuth2MemberResponseDto {
    private Long memberSid;
    private MemberAuthProvider provider;
    private String email;
    private String name;
    private MemberRole role;
}
