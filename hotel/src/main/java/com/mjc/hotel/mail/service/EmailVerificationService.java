package com.mjc.hotel.mail.service;

import com.mjc.hotel.mail.dto.EmailVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final String CODE_PREFIX = "email-verification:code:";
    private static final String VERIFIED_PREFIX = "email-verification:verified:";

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public EmailVerificationResponse sendCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        String code = createCode();

        redisTemplate.opsForValue().set(CODE_PREFIX + normalizedEmail, code, CODE_TTL);
        redisTemplate.delete(VERIFIED_PREFIX + normalizedEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(normalizedEmail);
        message.setSubject("[StayNow] 이메일 인증번호");
        message.setText("""
                StayNow 회원가입 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안만 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code));
        mailSender.send(message);

        return EmailVerificationResponse.builder()
                .email(normalizedEmail)
                .verified(false)
                .expiresInSeconds(CODE_TTL.toSeconds())
                .build();
    }

    public EmailVerificationResponse confirmCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = code == null ? "" : code.trim();
        String savedCode = redisTemplate.opsForValue().get(CODE_PREFIX + normalizedEmail);

        if (savedCode == null || !savedCode.equals(normalizedCode)) {
            throw new IllegalArgumentException("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        redisTemplate.delete(CODE_PREFIX + normalizedEmail);
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + normalizedEmail, "true", VERIFIED_TTL);

        return EmailVerificationResponse.builder()
                .email(normalizedEmail)
                .verified(true)
                .expiresInSeconds(VERIFIED_TTL.toSeconds())
                .build();
    }

    public boolean isVerified(String email) {
        String normalizedEmail = normalizeEmail(email);
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(VERIFIED_PREFIX + normalizedEmail));
    }

    private String normalizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일을 입력해주세요.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String createCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
