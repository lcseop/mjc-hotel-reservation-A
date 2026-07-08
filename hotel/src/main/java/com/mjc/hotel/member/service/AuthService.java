package com.mjc.hotel.member.service;

import com.mjc.hotel.member.dto.MemberLoginRequestDto;
import com.mjc.hotel.member.dto.MemberLoginResponseDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.util.JwtProvider;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 1800L;

    private final MemberAuthAccountRepository memberAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        MemberAuthAccount authAccount = memberAuthAccountRepository
                .findLoginAuthAccount(request.getEmail(), MemberAuthProvider.LOCAL)
                .orElseThrow(() -> new AuthenticationFailedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        Member member = authAccount.getMember();
        validateLoginMember(member);
        validatePassword(request.getPassword(), authAccount.getPasswordHash());

        authAccount.setLastLoginAt(LocalDateTime.now());

        return MemberLoginResponseDto.builder()
                .accessToken(jwtProvider.createToken(member.getEmail()))
                .tokenType("Bearer")
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN_SECONDS)
                .memberSid(member.getSid())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .point(member.getPoint())
                .build();
    }

    private void validateLoginMember(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthenticationFailedException("로그인할 수 없는 회원입니다.");
        }
        if (!Boolean.TRUE.equals(member.getEmailVerified())) {
            throw new AuthenticationFailedException("이메일 인증이 필요합니다.");
        }
    }

    private void validatePassword(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || !passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new AuthenticationFailedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }
}
