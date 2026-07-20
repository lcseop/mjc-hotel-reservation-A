package com.mjc.hotel.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh-token:";
    private static final RedisScript<Long> DELETE_IF_MATCHES_SCRIPT = RedisScript.of("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public void save(Long memberSid, String refreshToken, long expiresInSeconds) {
        validateMemberSid(memberSid);
        validateRefreshToken(refreshToken);

        stringRedisTemplate.opsForValue()
                .set(getKey(memberSid), refreshToken, Duration.ofSeconds(expiresInSeconds));
    }

    public String get(Long memberSid) {
        validateMemberSid(memberSid);
        return stringRedisTemplate.opsForValue().get(getKey(memberSid));
    }

    public boolean matches(Long memberSid, String refreshToken) {
        validateRefreshToken(refreshToken);

        String savedRefreshToken = get(memberSid);
        return refreshToken.equals(savedRefreshToken);
    }

    public void delete(Long memberSid) {
        validateMemberSid(memberSid);
        stringRedisTemplate.delete(getKey(memberSid));
    }

    public boolean deleteIfMatches(Long memberSid, String refreshToken) {
        validateMemberSid(memberSid);
        validateRefreshToken(refreshToken);

        Long deletedCount = stringRedisTemplate.execute(
                DELETE_IF_MATCHES_SCRIPT,
                List.of(getKey(memberSid)),
                refreshToken
        );
        return deletedCount != null && deletedCount > 0;
    }

    private String getKey(Long memberSid) {
        return REFRESH_TOKEN_KEY_PREFIX + memberSid;
    }

    private void validateMemberSid(Long memberSid) {
        if (memberSid == null) {
            throw new IllegalArgumentException("memberSid는 필수입니다.");
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken은 필수입니다.");
        }
    }
}
