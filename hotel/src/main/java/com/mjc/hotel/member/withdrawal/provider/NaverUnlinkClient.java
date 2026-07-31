package com.mjc.hotel.member.withdrawal.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.withdrawal.exception.SocialUnlinkException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NaverUnlinkClient implements SocialUnlinkClient {

    private static final String REVOKE_URI = "https://nid.naver.com/oauth2.0/revoke";

    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;

    public NaverUnlinkClient(
            @Value("${spring.security.oauth2.client.registration.naver.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.naver.client-secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.NAVER;
    }

    @Override
    public void unlink(String accessToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.set("client_id", clientId);
        form.set("client_secret", clientSecret);
        form.set("token", accessToken);
        form.set("token_type_hint", "access_token");

        try {
            restClient.post()
                    .uri(REVOKE_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new SocialUnlinkException("네이버 계정 연결을 해제하지 못했습니다.", exception);
        }
    }
}
