package com.mjc.hotel.auth;

import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
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
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceOAuth2Test {

    private final MemberAuthAccountRepository authAccountRepository = mock(MemberAuthAccountRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final JwtProvider jwtProvider = new JwtProvider();
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthService authService = new AuthService(
            authAccountRepository,
            memberRepository,
            mock(MemberService.class),
            mock(TermRepository.class),
            mock(MemberDtoMapper.class),
            passwordEncoder,
            jwtProvider,
            refreshTokenService
    );

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
        verify(refreshTokenService).save(eq(1L), eq(response.getRefreshToken()), eq(1209600L));
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
}
