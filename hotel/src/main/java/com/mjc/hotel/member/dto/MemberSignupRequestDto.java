package com.mjc.hotel.member.dto;

import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class MemberSignupRequestDto {
    private String name;
    private String phone;
    private String email;
    private MemberStatus status;
    private MemberRole role;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private AuthAccountRequest authAccount;
    private List<TermAgreementRequest> termAgreements;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @Getter
    @Setter
    public static class AuthAccountRequest {
        private String provider;
        private String providerUserId;
        private String passwordHash;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @Getter
    @Setter
    public static class TermAgreementRequest {
        private Long sid;
        private Boolean isAgreed;
    }
}
