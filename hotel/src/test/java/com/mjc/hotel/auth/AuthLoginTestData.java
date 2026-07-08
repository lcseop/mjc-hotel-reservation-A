package com.mjc.hotel.auth;

import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class AuthLoginTestData {

    private static final String LOGIN_TEST_EMAIL = "login-test@mjc.com";
    private static final String LOGIN_TEST_PASSWORD = "1234567899";

    @Autowired
    private AuthService authService;

    @DisplayName("authLoginTestData")
    @Test
    @Commit
    public void addAuthLoginTestData() {
        MemberSignupRequestDto request = MemberSignupRequestDto.builder()
                .name("로그인 테스트")
                .phone("010-1111-2222")
                .email(LOGIN_TEST_EMAIL)
                .password(LOGIN_TEST_PASSWORD)
                .passwordConfirm(LOGIN_TEST_PASSWORD)
                .emailVerified(true)
                .phoneVerified(false)
                .build();

        authService.signup(request);
    }
}
