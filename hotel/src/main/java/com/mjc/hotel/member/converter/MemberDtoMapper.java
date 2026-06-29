package com.mjc.hotel.member.converter;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.entity.Member;
import org.springframework.stereotype.Component;

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
