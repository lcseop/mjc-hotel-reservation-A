package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SocialProviderTokenService {

    private static final String KEY_PREFIX = "social-unlink-token:";
    private static final Duration MAX_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void save(
            Long memberSid,
            MemberAuthProvider provider,
            String accessToken,
            Instant expiresAt
    ) {
        if (memberSid == null || provider == null || provider == MemberAuthProvider.LOCAL
                || accessToken == null || accessToken.isBlank()) {
            return;
        }

        Duration providerTtl = expiresAt == null
                ? MAX_TTL
                : Duration.between(Instant.now(), expiresAt);
        Duration ttl = providerTtl.compareTo(MAX_TTL) > 0 ? MAX_TTL : providerTtl;
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        redisTemplate.opsForValue().set(key(memberSid, provider), accessToken, ttl);
    }

    public String get(Long memberSid, MemberAuthProvider provider) {
        return redisTemplate.opsForValue().get(key(memberSid, provider));
    }

    public void delete(Long memberSid, MemberAuthProvider provider) {
        redisTemplate.delete(key(memberSid, provider));
    }

    private String key(Long memberSid, MemberAuthProvider provider) {
        return KEY_PREFIX + memberSid + ":" + provider.name().toLowerCase();
    }
}
