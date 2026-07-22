package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetTokenServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private PasswordEncoder passwordEncoder;
    private PasswordResetTokenService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new PasswordResetTokenService(redisTemplate, passwordEncoder);
    }

    @Test
    void storesHashedCodeUsingHashedEmailKey() {
        when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(Duration.ofMinutes(1))))
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-code");

        String code = service.issueVerificationCode("member@example.com");

        assertThat(code).matches("\\d{6}");
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.startsWith("password-reset:code:"),
                eq("encoded-code"),
                eq(Duration.ofMinutes(5))
        );
    }

    @Test
    void validCodeCreatesVerifiedMarkerBoundToEmailCodeAndAccount() {
        when(valueOperations.get(anyString())).thenReturn("encoded-code");
        when(passwordEncoder.matches("123456", "encoded-code")).thenReturn(true);

        service.verifyCode(
                "member@example.com",
                "123456",
                11L
        );

        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.startsWith("password-reset:verified:"),
                eq("11"),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    void verifiedEmailAndCodeCanOnlyBeConsumedOnceFromRedis() {
        when(valueOperations.getAndDelete(anyString())).thenReturn("11");

        assertThat(service.consumeVerifiedCode("member@example.com", "123456"))
                .isEqualTo(11L);
        verify(valueOperations).getAndDelete(
                org.mockito.ArgumentMatchers.startsWith("password-reset:verified:")
        );
    }

    @Test
    void expiredVerifiedCodeIsRejected() {
        when(valueOperations.getAndDelete(anyString())).thenReturn(null);

        assertThatThrownBy(() -> service.consumeVerifiedCode(
                "member@example.com",
                "123456"
        ))
                .isInstanceOf(PasswordResetException.class)
                .hasMessageContaining("만료");
    }
}
