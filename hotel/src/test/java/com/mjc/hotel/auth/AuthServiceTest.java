package com.mjc.hotel.auth;

import com.mjc.hotel.auth.dto.LogoutRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenResponseDto;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.auth.service.RefreshTokenService;
import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.term.repository.TermRepository;
import com.mjc.hotel.util.JwtProvider;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String TEST_JWT_SECRET = "test-jwt-secret-key-with-at-least-32-bytes";

    private final MemberAuthAccountRepository authAccountRepository = mock(MemberAuthAccountRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final MemberService memberService = mock(MemberService.class);
    private final TermRepository termRepository = mock(TermRepository.class);
    private final MemberDtoMapper memberDtoMapper = mock(MemberDtoMapper.class);
    private final JwtProvider jwtProvider = new JwtProvider(TEST_JWT_SECRET);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthService authService = new AuthService(
            authAccountRepository,
            memberRepository,
            memberService,
            termRepository,
            memberDtoMapper,
            passwordEncoder,
            jwtProvider,
            refreshTokenService
    );

    @Test
    void signupCreatesLocalMemberAndAuthAccount() {
        MemberSignupRequestDto request = MemberSignupRequestDto.builder()
                .name("회원가입 테스트")
                .phone("010-2222-3333")
                .email("signup-user@example.com")
                .password("password")
                .passwordConfirm("password")
                .emailVerified(true)
                .build();
        Member member = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .build();
        Member savedMember = Member.builder()
                .sid(1L)
                .email(request.getEmail())
                .name(request.getName())
                .build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .provider(MemberAuthProvider.LOCAL)
                .providerUserId(request.getEmail())
                .build();

        when(memberDtoMapper.toEntity(request)).thenReturn(member);
        when(memberDtoMapper.toAuthAccount(any(MemberSignupRequestDto.AuthAccountRequest.class)))
                .thenReturn(authAccount);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(memberService.createMember(member, authAccount, List.of())).thenReturn(savedMember);

        Member result = authService.signup(request);

        assertThat(result.getSid()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(authAccount.getPasswordHash()).isEqualTo("encoded-password");
        verify(memberService).createMember(member, authAccount, List.of());
    }

    @Test
    void logoutDeletesMatchingRefreshToken() {
        String refreshToken = jwtProvider.createRefreshToken("logout-user@example.com");
        LogoutRequestDto request = LogoutRequestDto.builder()
                .memberSid(1L)
                .refreshToken(refreshToken)
                .build();

        authService.logout(request);

        verify(refreshTokenService).deleteIfMatches(1L, refreshToken);
    }

    @Test
    void googleLoginIssuesTheSameApplicationTokensAsLocalLogin() {
        Member member = Member.builder()
                .sid(1L)
                .email("google-user@example.com")
                .name("구글 회원")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(5000)
                .build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.GOOGLE)
                .providerUserId("google-sub")
                .build();

        when(authAccountRepository.findActiveByMemberSidAndProvider(1L, MemberAuthProvider.GOOGLE))
                .thenReturn(Optional.of(authAccount));

        MemberLoginResponseDto response = authService.loginOAuth2(1L, MemberAuthProvider.GOOGLE);

        assertThat(jwtProvider.validateAccessToken(response.getAccessToken())).isTrue();
        assertThat(jwtProvider.validateRefreshToken(response.getRefreshToken())).isTrue();
        assertThat(jwtProvider.getName(response.getAccessToken())).isEqualTo(member.getEmail());
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getRefreshTokenExpiresIn()).isEqualTo(1209600L);
        assertThat(response.getMemberSid()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("google-user@example.com");
        assertThat(response.getName()).isEqualTo("구글 회원");
        assertThat(response.getRole()).isEqualTo(MemberRole.USER);
        assertThat(response.getPoint()).isEqualTo(5000);
        assertThat(response.getProvider()).isEqualTo(MemberAuthProvider.GOOGLE);
        assertThat(authAccount.getLastLoginAt()).isNotNull();
        verify(refreshTokenService).save(1L, response.getRefreshToken(), 1209600L);
    }

    @Test
    void naverLoginIssuesApplicationTokensWithoutClaimingTheEmailIsVerified() {
        Member member = Member.builder()
                .sid(3L)
                .email("naver-user@example.com")
                .name("네이버 회원")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .point(5000)
                .build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.NAVER)
                .providerUserId("naver-id")
                .build();

        when(authAccountRepository.findActiveByMemberSidAndProvider(3L, MemberAuthProvider.NAVER))
                .thenReturn(Optional.of(authAccount));

        MemberLoginResponseDto response = authService.loginOAuth2(3L, MemberAuthProvider.NAVER);

        assertThat(jwtProvider.validateAccessToken(response.getAccessToken())).isTrue();
        assertThat(jwtProvider.validateRefreshToken(response.getRefreshToken())).isTrue();
        assertThat(response.getProvider()).isEqualTo(MemberAuthProvider.NAVER);
        assertThat(member.getEmailVerified()).isFalse();
        verify(refreshTokenService).save(3L, response.getRefreshToken(), 1209600L);
    }

    @Test
    void naverMemberCanRefreshAnApplicationAccessToken() {
        Member member = Member.builder()
                .sid(3L)
                .email("naver-user@example.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .point(5000)
                .build();
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(refreshToken);

        when(memberRepository.findActiveByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(refreshTokenService.matches(3L, refreshToken)).thenReturn(true);

        RefreshTokenResponseDto response = authService.refreshAccessToken(request);

        assertThat(jwtProvider.validateAccessToken(response.getAccessToken())).isTrue();
        assertThat(jwtProvider.getName(response.getAccessToken())).isEqualTo(member.getEmail());
    }

    @Test
    void localLoginResponseAlsoContainsPointAndProvider() {
        Member member = Member.builder()
                .sid(2L)
                .email("local-user@example.com")
                .name("로컬 회원")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(0)
                .build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.LOCAL)
                .passwordHash("password-hash")
                .build();
        MemberLoginRequestDto request = new MemberLoginRequestDto();
        request.setEmail(member.getEmail());
        request.setPassword("password");

        when(authAccountRepository.findLoginAuthAccount(member.getEmail(), MemberAuthProvider.LOCAL))
                .thenReturn(Optional.of(authAccount));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);

        MemberLoginResponseDto response = authService.login(request);

        assertThat(response.getProvider()).isEqualTo(MemberAuthProvider.LOCAL);
        assertThat(response.getPoint()).isZero();
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(jwtProvider.validateAccessToken(response.getAccessToken())).isTrue();
    }

    @Test
    void localLoginStillRejectsAnUnverifiedEmail() {
        Member member = Member.builder()
                .sid(4L)
                .email("unverified@example.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .point(0)
                .build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.LOCAL)
                .passwordHash("password-hash")
                .build();
        MemberLoginRequestDto request = new MemberLoginRequestDto();
        request.setEmail(member.getEmail());
        request.setPassword("password");

        when(authAccountRepository.findLoginAuthAccount(member.getEmail(), MemberAuthProvider.LOCAL))
                .thenReturn(Optional.of(authAccount));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("이메일 인증");
    }
}
