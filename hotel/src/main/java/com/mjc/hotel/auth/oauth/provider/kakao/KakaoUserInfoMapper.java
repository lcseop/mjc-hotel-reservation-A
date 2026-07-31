package com.mjc.hotel.auth.oauth.provider.kakao;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapper;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class KakaoUserInfoMapper implements SocialUserInfoMapper {

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.KAKAO;
    }

    @Override
    public SocialUserInfo map(Map<String, Object> attributes) {
        KakaoUserInfoResponse response = toResponse(attributes);

        if (response.id() == null) {
            throw oauth2Exception("invalid_kakao_user", "카카오 사용자 식별자(id)가 없습니다.");
        }

        KakaoUserInfoResponse.KakaoAccount account = response.kakaoAccount();
        String email = account == null ? null : normalizeEmail(account.email());
        String accountName = account == null ? null : trimToNull(account.name());
        String nickname = account == null || account.profile() == null
                ? null
                : trimToNull(account.profile().nickname());
        boolean emailVerified = account != null
                && Boolean.TRUE.equals(account.emailValid())
                && Boolean.TRUE.equals(account.emailVerified());

        return new SocialUserInfo(
                provider(),
                response.id().toString(),
                email,
                resolveName(accountName, nickname, email),
                emailVerified
        );
    }

    private KakaoUserInfoResponse toResponse(Map<String, Object> attributes) {
        Long id = toLong(attributes.get("id"));
        Map<String, Object> accountAttributes = toMap(attributes.get("kakao_account"));
        Map<String, Object> profileAttributes = accountAttributes == null
                ? null
                : toMap(accountAttributes.get("profile"));

        KakaoUserInfoResponse.Profile profile = profileAttributes == null
                ? null
                : new KakaoUserInfoResponse.Profile(optionalText(profileAttributes.get("nickname")));
        KakaoUserInfoResponse.KakaoAccount account = accountAttributes == null
                ? null
                : new KakaoUserInfoResponse.KakaoAccount(
                        optionalText(accountAttributes.get("email")),
                        optionalText(accountAttributes.get("name")),
                        toBoolean(accountAttributes.get("is_email_valid")),
                        toBoolean(accountAttributes.get("is_email_verified")),
                        profile
                );
        return new KakaoUserInfoResponse(id, account);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value == null ? null : Boolean.valueOf(value.toString());
    }

    private String optionalText(Object value) {
        return value == null ? null : trimToNull(value.toString());
    }

    private String normalizeEmail(String email) {
        String text = trimToNull(email);
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }

    private String resolveName(String accountName, String nickname, String email) {
        if (accountName != null) {
            return accountName;
        }
        if (nickname != null) {
            return nickname;
        }
        if (email != null) {
            int separatorIndex = email.indexOf('@');
            return separatorIndex > 0 ? email.substring(0, separatorIndex) : email;
        }
        return "Kakao User";
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private OAuth2AuthenticationException oauth2Exception(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), description);
    }
}
