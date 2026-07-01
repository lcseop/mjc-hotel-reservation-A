package com.mjc.hotel.member.dto;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class MemberAuthAccountRequestDto {
    private Long memberSid;
    private MemberAuthProvider provider;
    private String providerUserId;
    private String password;
    private String passwordHash;
    private LocalDateTime lastLoginAt;
}
