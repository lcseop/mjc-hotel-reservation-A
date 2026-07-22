package com.mjc.hotel.auth.oauth.provider.naver;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class NaverOAuth2TokenRequestParametersConverterTest {

    private final NaverOAuth2TokenRequestParametersConverter converter =
            new NaverOAuth2TokenRequestParametersConverter();

    @Test
    void addsExistingStateToNaverTokenRequest() {
        OAuth2AuthorizationCodeGrantRequest request = grantRequest("naver", "naver-state");

        MultiValueMap<String, String> parameters = converter.convert(request);

        assertThat(parameters.getFirst(OAuth2ParameterNames.STATE)).isEqualTo("naver-state");
    }

    @Test
    void doesNotAddStateToOtherProviderTokenRequest() {
        OAuth2AuthorizationCodeGrantRequest request = grantRequest("kakao", "kakao-state");

        MultiValueMap<String, String> parameters = converter.convert(request);

        assertThat(parameters).doesNotContainKey(OAuth2ParameterNames.STATE);
    }

    private OAuth2AuthorizationCodeGrantRequest grantRequest(String registrationId, String state) {
        String redirectUri = "http://localhost:33000/login/oauth2/code/" + registrationId;
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId(registrationId + "-client-id")
                .clientSecret(registrationId + "-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("id")
                .clientName(registrationId)
                .build();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .clientId(clientRegistration.getClientId())
                .redirectUri(redirectUri)
                .state(state)
                .build();
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success("code")
                .redirectUri(redirectUri)
                .state(state)
                .build();

        return new OAuth2AuthorizationCodeGrantRequest(
                clientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );
    }
}
