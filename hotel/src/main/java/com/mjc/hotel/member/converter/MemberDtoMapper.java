package com.mjc.hotel.member.converter;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.dto.MemberSignupRequestDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.term.entity.Term;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberDtoMapper {

    public Member toEntity(MemberRequestDto dto) {
        return Member.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .status(dto.getStatus())
                .role(dto.getRole())
                .emailVerified(dto.getEmailVerified())
                .phoneVerified(dto.getPhoneVerified())
                .build();
    }

    public Member toEntity(MemberSignupRequestDto dto) {
        return Member.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .status(dto.getStatus())
                .role(dto.getRole())
                .emailVerified(dto.getEmailVerified())
                .phoneVerified(dto.getPhoneVerified())
                .build();
    }

    public MemberAuthAccount toAuthAccount(MemberSignupRequestDto.AuthAccountRequest dto) {
        if (dto == null) {
            return null;
        }
        return MemberAuthAccount.builder()
                .provider(dto.getProvider())
                .providerUserId(dto.getProviderUserId())
                .passwordHash(dto.getPasswordHash())
                .build();
    }

    public MemberTermAgreement toTermAgreement(MemberSignupRequestDto.TermAgreementRequest dto, Term term) {
        return MemberTermAgreement.builder()
                .term(term)
                .isAgreed(dto.getIsAgreed())
                .agreedAt(Boolean.TRUE.equals(dto.getIsAgreed()) ? LocalDateTime.now() : null)
                .build();
    }

    public MemberResponseDto toResponseDto(Member member) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .status(member.getStatus())
                .role(member.getRole())
                .emailVerified(member.getEmailVerified())
                .phoneVerified(member.getPhoneVerified())
                .build();
    }
}
