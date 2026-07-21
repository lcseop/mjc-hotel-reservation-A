package com.mjc.hotel.auth.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Locale;

public record GoogleOAuth2UserInfo(
        String providerUserId,
        String email,
        String name,
        boolean emailVerified
) {

    public static GoogleOAuth2UserInfo from(OidcUser oidcUser) {
        String providerUserId = requireText(
                oidcUser.getSubject(),
                "invalid_google_user",
                "구글 사용자 식별자(sub)가 없습니다."
        );
        String email = requireText(
                oidcUser.getEmail(),
                "google_email_required",
                "구글 계정의 이메일 제공 동의가 필요합니다."
        ).toLowerCase(Locale.ROOT);
        String name = hasText(oidcUser.getFullName())
                ? oidcUser.getFullName().trim()
                : resolveDefaultName(email);

        return new GoogleOAuth2UserInfo(
                providerUserId,
                email,
                name,
                Boolean.TRUE.equals(oidcUser.getEmailVerified())
        );
    }

    private static String requireText(String value, String errorCode, String description) {
        if (!hasText(value)) {
            throw oauth2Exception(errorCode, description);
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String resolveDefaultName(String email) {
        int separatorIndex = email.indexOf('@');
        return separatorIndex > 0 ? email.substring(0, separatorIndex) : email;
    }

    static OAuth2AuthenticationException oauth2Exception(String errorCode, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode), description);
    }
}
