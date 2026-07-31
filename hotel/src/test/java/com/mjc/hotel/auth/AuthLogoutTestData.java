package com.mjc.hotel.auth;

import com.mjc.hotel.auth.dto.LogoutRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.auth.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuthLogoutTestData {

    private static final String LOGOUT_TEST_PASSWORD = "1234567899";

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @DisplayName("authLogoutTestData")
    @Test
    @Commit
    public void logoutAuthTestData() {
        String email = createTestEmail("logout");
        authService.signup(MemberSignupRequestDto.builder()
                .name("로그아웃 테스트")
                .phone("010-3333-4444")
                .email(email)
                .password(LOGOUT_TEST_PASSWORD)
                .passwordConfirm(LOGOUT_TEST_PASSWORD)
                .emailVerified(true)
                .build());

        MemberLoginResponseDto loginResponse = authService.login(
                MemberLoginRequestDto.builder()
                        .email(email)
                        .password(LOGOUT_TEST_PASSWORD)
                        .build()
        );

        assertThat(refreshTokenService.get(loginResponse.getMemberSid()))
                .isEqualTo(loginResponse.getRefreshToken());

        authService.logout(LogoutRequestDto.builder()
                .memberSid(loginResponse.getMemberSid())
                .refreshToken(loginResponse.getRefreshToken())
                .build());

        assertThat(refreshTokenService.get(loginResponse.getMemberSid())).isNull();
    }

    private String createTestEmail(String prefix) {
        return prefix + "-test-" + UUID.randomUUID() + "@mjc.com";
    }
}
