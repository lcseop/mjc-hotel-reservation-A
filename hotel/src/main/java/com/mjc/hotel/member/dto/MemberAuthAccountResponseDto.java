package com.mjc.hotel.member.dto;

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
public class MemberAuthAccountResponseDto {
    private Long sid;
    private Long memberSid;
    private String provider;
    private String providerUserId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
