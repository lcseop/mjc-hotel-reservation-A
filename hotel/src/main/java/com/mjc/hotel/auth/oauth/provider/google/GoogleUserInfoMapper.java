package com.mjc.hotel.auth.oauth.provider.google;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapper;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class GoogleUserInfoMapper implements SocialUserInfoMapper {

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo map(Map<String, Object> attributes) {
        String providerUserId = requireText(
                attributes.get("sub"),
                "invalid_google_user",
                "구글 사용자 식별자(sub)가 없습니다."
        );
        String email = optionalText(attributes.get("email"));
        String normalizedEmail = email == null ? null : email.toLowerCase(Locale.ROOT);
        String name = optionalText(attributes.get("name"));

        return new SocialUserInfo(
                provider(),
                providerUserId,
                normalizedEmail,
                resolveName(name, normalizedEmail, "Google User"),
                Boolean.TRUE.equals(attributes.get("email_verified"))
        );
    }

    private String requireText(Object value, String code, String description) {
        String text = optionalText(value);
        if (text == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(code), description);
        }
        return text;
    }

    private String optionalText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String resolveName(String name, String email, String defaultName) {
        if (name != null) {
            return name;
        }
        if (email != null) {
            int separatorIndex = email.indexOf('@');
            return separatorIndex > 0 ? email.substring(0, separatorIndex) : email;
        }
        return defaultName;
    }
}
