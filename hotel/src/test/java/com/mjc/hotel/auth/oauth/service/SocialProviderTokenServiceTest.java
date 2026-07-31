package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SocialProviderTokenServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SocialProviderTokenService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new SocialProviderTokenService(redisTemplate);
    }

    @Test
    void storesSocialAccessTokenForAtMostTenMinutes() {
        service.save(7L, MemberAuthProvider.NAVER, "naver-token", null);

        verify(valueOperations).set(
                "social-unlink-token:7:naver",
                "naver-token",
                Duration.ofMinutes(10)
        );
    }

    @Test
    void doesNotStoreLocalAccountCredentials() {
        service.save(7L, MemberAuthProvider.LOCAL, "password", null);

        verify(valueOperations, never()).set(any(), any(), any(Duration.class));
    }
}
