package com.mjc.hotel.member.converter;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberAuthAccountRequestDto;
import com.mjc.hotel.member.dto.MemberAuthAccountResponseDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.dto.MemberSignupRequestDto;
import com.mjc.hotel.member.dto.MemberTermAgreementRequestDto;
import com.mjc.hotel.member.dto.MemberTermAgreementResponseDto;
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
                .point(dto.getPoint())
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
                .agreedAt(LocalDateTime.now())
                .build();
    }

    public MemberAuthAccount toAuthAccount(MemberAuthAccountRequestDto dto, Member member) {
        return MemberAuthAccount.builder()
                .member(member)
                .provider(dto.getProvider())
                .providerUserId(dto.getProviderUserId())
                .passwordHash(dto.getPasswordHash())
                .lastLoginAt(dto.getLastLoginAt())
                .build();
    }

    public MemberTermAgreement toTermAgreement(MemberTermAgreementRequestDto dto, Member member, Term term) {
        return MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .isAgreed(dto.getIsAgreed())
                .agreedAt(dto.getAgreedAt() != null ? dto.getAgreedAt() : LocalDateTime.now())
                .withdrawnAt(dto.getWithdrawnAt())
                .build();
    }

    public MemberResponseDto toResponseDto(Member member) {
        return MemberResponseDto.builder()
                .sid(member.getSid())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .status(member.getStatus())
                .role(member.getRole())
                .emailVerified(member.getEmailVerified())
                .phoneVerified(member.getPhoneVerified())
                .deleted(Boolean.TRUE.equals(member.getDeleted()))
                .deletedAt(member.getDeletedAt())
                .build();
    }

    public MemberAuthAccountResponseDto toAuthAccountResponseDto(MemberAuthAccount authAccount) {
        return MemberAuthAccountResponseDto.builder()
                .sid(authAccount.getSid())
                .memberSid(authAccount.getMember().getSid())
                .provider(authAccount.getProvider())
                .providerUserId(authAccount.getProviderUserId())
                .lastLoginAt(authAccount.getLastLoginAt())
                .createdAt(authAccount.getCreatedAt())
                .deleted(Boolean.TRUE.equals(authAccount.getDeleted()))
                .deletedAt(authAccount.getDeletedAt())
                .build();
    }

    public MemberTermAgreementResponseDto toTermAgreementResponseDto(MemberTermAgreement termAgreement) {
        return MemberTermAgreementResponseDto.builder()
                .sid(termAgreement.getSid())
                .memberSid(termAgreement.getMember().getSid())
                .termSid(termAgreement.getTerm().getSid())
                .isAgreed(termAgreement.getIsAgreed())
                .agreedAt(termAgreement.getAgreedAt())
                .withdrawnAt(termAgreement.getWithdrawnAt())
                .deleted(Boolean.TRUE.equals(termAgreement.getDeleted()))
                .deletedAt(termAgreement.getDeletedAt())
                .build();
    }
}
