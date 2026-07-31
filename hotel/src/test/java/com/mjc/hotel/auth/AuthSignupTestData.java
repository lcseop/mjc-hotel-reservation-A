package com.mjc.hotel.auth;

import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuthSignupTestData {

    private static final String SIGNUP_TEST_PASSWORD = "1234567899";

    @Autowired
    private AuthService authService;

    @DisplayName("authSignupTestData")
    @Test
    @Commit
    public void addAuthSignupTestData() {
        String email = createTestEmail("signup");
        MemberSignupRequestDto request = MemberSignupRequestDto.builder()
                .name("회원가입 테스트")
                .phone("010-2222-3333")
                .email(email)
                .password(SIGNUP_TEST_PASSWORD)
                .passwordConfirm(SIGNUP_TEST_PASSWORD)
                .emailVerified(true)
                .build();

        Member member = authService.signup(request);

        assertThat(member.getSid()).isNotNull();
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getName()).isEqualTo("회원가입 테스트");
    }

    private String createTestEmail(String prefix) {
        return prefix + "-test-" + UUID.randomUUID() + "@mjc.com";
    }
}
