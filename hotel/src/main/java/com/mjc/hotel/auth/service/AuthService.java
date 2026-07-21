package com.mjc.hotel.auth.service;

import com.mjc.hotel.auth.dto.LogoutRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenResponseDto;
import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import com.mjc.hotel.util.JwtProvider;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberAuthAccountRepository memberAuthAccountRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final TermRepository termRepository;
    private final MemberDtoMapper memberDtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public Member signup(MemberSignupRequestDto request) {
        validatePasswordConfirm(request);

        List<MemberTermAgreement> termAgreements = Collections.emptyList();

        if (request.getTermAgreements() != null) {
            termAgreements = request.getTermAgreements().stream()
                    .map(termAgreementRequest -> {
                        Term term = termRepository.findById(termAgreementRequest.getSid())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. sid=" + termAgreementRequest.getSid()));
                        return memberDtoMapper.toTermAgreement(termAgreementRequest, term);
                    })
                    .toList();
        }

        MemberSignupRequestDto.AuthAccountRequest authAccountRequest = resolveAuthAccountRequest(request);
        MemberAuthAccount authAccount = memberDtoMapper.toAuthAccount(authAccountRequest);
        if (authAccount != null) {
            authAccount.setPasswordHash(resolvePasswordHash(
                    authAccountRequest.getPassword(),
                    authAccountRequest.getPasswordHash()
            ));
        }

        return memberService.createMember(memberDtoMapper.toEntity(request), authAccount, termAgreements);
    }

    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        MemberAuthAccount authAccount = memberAuthAccountRepository
                .findLoginAuthAccount(request.getEmail(), MemberAuthProvider.LOCAL)
                .orElseThrow(() -> new AuthenticationFailedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        Member member = authAccount.getMember();
        validateLoginMember(member);
        validatePassword(request.getPassword(), authAccount.getPasswordHash());

        authAccount.setLastLoginAt(LocalDateTime.now());

        return issueLoginTokens(member, MemberAuthProvider.LOCAL);
    }

    @Transactional
    public MemberLoginResponseDto loginOAuth2(Long memberSid, MemberAuthProvider provider) {
        if (provider == null || provider == MemberAuthProvider.LOCAL) {
            throw new AuthenticationFailedException("유효하지 않은 소셜 로그인 제공자입니다.");
        }

        MemberAuthAccount authAccount = memberAuthAccountRepository
                .findActiveByMemberSidAndProvider(memberSid, provider)
                .orElseThrow(() -> new AuthenticationFailedException("소셜 로그인 계정 정보가 없습니다."));

        Member member = authAccount.getMember();
        validateLoginMember(member);
        authAccount.setLastLoginAt(LocalDateTime.now());

        return issueLoginTokens(member, provider);
    }

    private MemberLoginResponseDto issueLoginTokens(Member member, MemberAuthProvider provider) {
        String accessToken = jwtProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());
        long refreshTokenExpiresIn = jwtProvider.getRefreshTokenExpiresInSeconds();

        refreshTokenService.save(member.getSid(), refreshToken, refreshTokenExpiresIn);

        return MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .memberSid(member.getSid())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .point(member.getPoint())
                .provider(provider)
                .build();
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponseDto refreshAccessToken(RefreshTokenRequestDto request) {
        String refreshToken = getRequiredRefreshToken(request);
        validateRefreshToken(refreshToken);

        String email = jwtProvider.getName(refreshToken);
        Member member = memberRepository
                .findActiveByEmail(email)
                .orElseThrow(() -> invalidRefreshToken());

        validateLoginMember(member);

        if (!refreshTokenService.matches(member.getSid(), refreshToken)) {
            throw invalidRefreshToken();
        }

        return RefreshTokenResponseDto.builder()
                .accessToken(jwtProvider.createAccessToken(member.getEmail()))
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .build();
    }

    public void logout(LogoutRequestDto request) {
        if (!hasLogoutCredentials(request)) {
            return;
        }

        String refreshToken = request.getRefreshToken();
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            return;
        }

        refreshTokenService.deleteIfMatches(request.getMemberSid(), refreshToken);
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

    private String getRequiredRefreshToken(RefreshTokenRequestDto request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw invalidRefreshToken();
        }
        return request.getRefreshToken();
    }

    private boolean hasLogoutCredentials(LogoutRequestDto request) {
        return request != null
                && request.getMemberSid() != null
                && request.getRefreshToken() != null
                && !request.getRefreshToken().isBlank();
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw invalidRefreshToken();
        }
    }

    private AuthenticationFailedException invalidRefreshToken() {
        return new AuthenticationFailedException("유효하지 않은 refresh token입니다.");
    }

    private String resolvePasswordHash(String password, String passwordHash) {
        if (password != null && !password.isBlank()) {
            return passwordEncoder.encode(password);
        }
        return passwordHash;
    }

    private void validatePasswordConfirm(MemberSignupRequestDto request) {
        String password = request.getPassword();
        if (password != null && !password.isBlank() && !password.equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private MemberSignupRequestDto.AuthAccountRequest resolveAuthAccountRequest(MemberSignupRequestDto request) {
        if (request.getAuthAccount() != null) {
            return request.getAuthAccount();
        }
        if (!hasAuthAccountInput(request)) {
            return null;
        }

        MemberAuthProvider provider = request.getProvider() != null ? request.getProvider() : MemberAuthProvider.LOCAL;
        String providerUserId = hasText(request.getProviderUserId()) ? request.getProviderUserId() : request.getEmail();

        return MemberSignupRequestDto.AuthAccountRequest.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .password(request.getPassword())
                .build();
    }

    private boolean hasAuthAccountInput(MemberSignupRequestDto request) {
        return request.getProvider() != null
                || hasText(request.getProviderUserId())
                || hasText(request.getPassword());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
