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
public class MemberTermAgreementRequestDto {
    private Long memberSid;
    private Long termSid;
    private Boolean isAgreed;
    private LocalDateTime agreedAt;
    private LocalDateTime withdrawnAt;
}
