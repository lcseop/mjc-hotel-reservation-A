package com.mjc.hotel.auth.oauth.provider.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverUserInfoResponse(
        String resultcode,
        String message,
        Response response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            String id,
            String email,
            String name,
            String nickname
    ) {
    }
}
