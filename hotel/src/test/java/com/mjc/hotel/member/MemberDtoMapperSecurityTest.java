package com.mjc.hotel.member;

import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDtoMapperSecurityTest {

    private final MemberDtoMapper mapper = new MemberDtoMapper();

    @Test
    void publicSignupCannotChoosePrivilegedMemberFields() {
        MemberSignupRequestDto request = MemberSignupRequestDto.builder()
                .email("user@example.com")
                .role(MemberRole.ADMIN)
                .status(MemberStatus.STOP)
                .emailVerified(false)
                .build();

        Member member = mapper.toEntity(request);

        assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getEmailVerified()).isFalse();
    }
}
