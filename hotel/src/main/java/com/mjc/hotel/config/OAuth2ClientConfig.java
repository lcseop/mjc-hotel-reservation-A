package com.mjc.hotel.config;

import com.mjc.hotel.auth.oauth.provider.naver.NaverOAuth2TokenRequestParametersConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
    authorizationCodeTokenResponseClient(
            NaverOAuth2TokenRequestParametersConverter naverParametersConverter
    ) {
        RestClientAuthorizationCodeTokenResponseClient tokenResponseClient =
                new RestClientAuthorizationCodeTokenResponseClient();
        tokenResponseClient.addParametersConverter(naverParametersConverter);
        return tokenResponseClient;
    }
}
