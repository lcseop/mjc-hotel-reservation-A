package com.mjc.hotel.auth.oauth.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SocialUserInfoMapperRegistry {

    private final Map<MemberAuthProvider, SocialUserInfoMapper> mappers;

    public SocialUserInfoMapperRegistry(List<SocialUserInfoMapper> mapperList) {
        EnumMap<MemberAuthProvider, SocialUserInfoMapper> registered =
                new EnumMap<>(MemberAuthProvider.class);

        for (SocialUserInfoMapper mapper : mapperList) {
            SocialUserInfoMapper previous = registered.put(mapper.provider(), mapper);
            if (previous != null) {
                throw new IllegalStateException(
                        "소셜 사용자 정보 Mapper가 중복 등록되었습니다: " + mapper.provider()
                );
            }
        }

        this.mappers = Map.copyOf(registered);
    }

    public SocialUserInfoMapper get(String registrationId) {
        MemberAuthProvider provider = resolveProvider(registrationId);
        SocialUserInfoMapper mapper = mappers.get(provider);
        if (mapper == null) {
            throw oauth2Exception(
                    "unsupported_oauth2_provider",
                    "지원하지 않는 소셜 로그인 제공자입니다: " + registrationId
            );
        }
        return mapper;
    }

    private MemberAuthProvider resolveProvider(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            throw oauth2Exception(
                    "invalid_oauth2_provider",
                    "소셜 로그인 제공자 정보가 없습니다."
            );
        }

        try {
            MemberAuthProvider provider = MemberAuthProvider.valueOf(
                    registrationId.trim().toUpperCase(Locale.ROOT)
            );
            if (provider == MemberAuthProvider.LOCAL) {
                throw new IllegalArgumentException();
            }
            return provider;
        } catch (IllegalArgumentException exception) {
            throw oauth2Exception(
                    "unsupported_oauth2_provider",
                    "지원하지 않는 소셜 로그인 제공자입니다: " + registrationId
            );
        }
    }

    private OAuth2AuthenticationException oauth2Exception(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), description);
    }
}
