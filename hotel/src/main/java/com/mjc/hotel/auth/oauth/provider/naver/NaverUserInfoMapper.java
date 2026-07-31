package com.mjc.hotel.auth.oauth.provider.naver;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapper;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class NaverUserInfoMapper implements SocialUserInfoMapper {

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.NAVER;
    }

    @Override
    public SocialUserInfo map(Map<String, Object> attributes) {
        NaverUserInfoResponse response = toResponse(attributes);

        if (response.response() == null || !hasText(response.response().id())) {
            throw oauth2Exception("invalid_naver_user", "네이버 사용자 식별자(id)가 없습니다.");
        }

        NaverUserInfoResponse.Response profile = response.response();
        String email = normalizeEmail(profile.email());
        String name = firstText(profile.name(), profile.nickname(), resolveDefaultName(email));

        return new SocialUserInfo(
                provider(),
                profile.id().trim(),
                email,
                name == null ? "Naver User" : name,
                false
        );
    }

    private NaverUserInfoResponse toResponse(Map<String, Object> attributes) {
        Map<String, Object> responseAttributes = toMap(attributes.get("response"));
        NaverUserInfoResponse.Response profile = responseAttributes == null
                ? null
                : new NaverUserInfoResponse.Response(
                        optionalText(responseAttributes.get("id")),
                        optionalText(responseAttributes.get("email")),
                        optionalText(responseAttributes.get("name")),
                        optionalText(responseAttributes.get("nickname"))
                );
        return new NaverUserInfoResponse(
                optionalText(attributes.get("resultcode")),
                optionalText(attributes.get("message")),
                profile
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private String optionalText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeEmail(String email) {
        if (!hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveDefaultName(String email) {
        if (email == null) {
            return null;
        }
        int separatorIndex = email.indexOf('@');
        return separatorIndex > 0 ? email.substring(0, separatorIndex) : email;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private OAuth2AuthenticationException oauth2Exception(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), description);
    }
}
