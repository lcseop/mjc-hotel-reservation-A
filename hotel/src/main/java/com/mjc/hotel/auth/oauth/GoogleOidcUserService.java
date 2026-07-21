package com.mjc.hotel.auth.oauth;

import com.mjc.hotel.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final GoogleSocialLoginService googleSocialLoginService;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        validateGoogleRegistration(userRequest);

        OidcUser oidcUser = delegate.loadUser(userRequest);
        GoogleOAuth2UserInfo userInfo = GoogleOAuth2UserInfo.from(oidcUser);
        Member member = googleSocialLoginService.loginOrSignup(userInfo);

        return new GoogleOidcUser(oidcUser, member);
    }

    private void validateGoogleRegistration(OidcUserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!GOOGLE_REGISTRATION_ID.equals(registrationId)) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "unsupported_oidc_provider",
                    "지원하지 않는 OIDC 제공자입니다: " + registrationId
            );
        }
    }
}
