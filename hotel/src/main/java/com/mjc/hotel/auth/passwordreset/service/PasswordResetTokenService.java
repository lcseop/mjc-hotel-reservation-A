package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_COOLDOWN = Duration.ofMinutes(1);
    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final String CODE_PREFIX = "password-reset:code:";
    private static final String ATTEMPTS_PREFIX = "password-reset:attempts:";
    private static final String COOLDOWN_PREFIX = "password-reset:cooldown:";
    private static final String VERIFIED_PREFIX = "password-reset:verified:";

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public String issueVerificationCode(String normalizedEmail) {
        String emailKey = digest(normalizedEmail);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                COOLDOWN_PREFIX + emailKey,
                "1",
                REQUEST_COOLDOWN
        );
        if (!Boolean.TRUE.equals(acquired)) {
            return null;
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(
                CODE_PREFIX + emailKey,
                passwordEncoder.encode(code),
                CODE_TTL
        );
        redisTemplate.delete(ATTEMPTS_PREFIX + emailKey);
        return code;
    }

    public void verifyCode(
            String normalizedEmail,
            String code,
            Long authAccountSid
    ) {
        String emailKey = digest(normalizedEmail);
        String codeKey = CODE_PREFIX + emailKey;
        String savedCodeHash = redisTemplate.opsForValue().get(codeKey);
        if (savedCodeHash == null || code == null || code.isBlank()) {
            throw invalidCode();
        }

        if (!passwordEncoder.matches(code.trim(), savedCodeHash)) {
            registerFailedAttempt(emailKey, codeKey);
            throw invalidCode();
        }

        redisTemplate.opsForValue().set(
                verifiedKey(normalizedEmail, code),
                authAccountSid.toString(),
                VERIFIED_CODE_TTL
        );
        redisTemplate.delete(codeKey);
        redisTemplate.delete(ATTEMPTS_PREFIX + emailKey);
    }

    public Long consumeVerifiedCode(String normalizedEmail, String code) {
        if (code == null || code.isBlank()) {
            throw invalidVerifiedCode();
        }

        String authAccountSid = redisTemplate.opsForValue().getAndDelete(
                verifiedKey(normalizedEmail, code)
        );
        if (authAccountSid == null) {
            throw invalidVerifiedCode();
        }

        try {
            return Long.valueOf(authAccountSid);
        } catch (NumberFormatException exception) {
            throw invalidVerifiedCode();
        }
    }

    public void clearVerification(String normalizedEmail) {
        String emailKey = digest(normalizedEmail);
        redisTemplate.delete(CODE_PREFIX + emailKey);
        redisTemplate.delete(ATTEMPTS_PREFIX + emailKey);
    }

    private void registerFailedAttempt(String emailKey, String codeKey) {
        String attemptsKey = ATTEMPTS_PREFIX + emailKey;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptsKey, CODE_TTL);
        }
        if (attempts != null && attempts >= MAX_CODE_ATTEMPTS) {
            redisTemplate.delete(codeKey);
        }
    }

    private String verifiedKey(String normalizedEmail, String code) {
        return VERIFIED_PREFIX + digest(normalizedEmail + "\n" + code.trim());
    }

    private String digest(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private PasswordResetException invalidCode() {
        return new PasswordResetException("인증번호가 올바르지 않거나 만료되었습니다.");
    }

    private PasswordResetException invalidVerifiedCode() {
        return new PasswordResetException("인증번호 확인이 만료되었거나 유효하지 않습니다. 다시 인증해 주세요.");
    }
}
