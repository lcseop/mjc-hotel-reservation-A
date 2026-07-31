package com.mjc.hotel.auth.oauth.provider.naver;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class NaverOAuth2TokenRequestParametersConverter implements
        Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

    private static final String NAVER_REGISTRATION_ID = "naver";

    @Override
    public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        if (!NAVER_REGISTRATION_ID.equals(request.getClientRegistration().getRegistrationId())) {
            return parameters;
        }

        String state = request.getAuthorizationExchange()
                .getAuthorizationRequest()
                .getState();
        if (state != null && !state.isBlank()) {
            parameters.set(OAuth2ParameterNames.STATE, state);
        }

        return parameters;
    }
}
