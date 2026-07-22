package com.mjc.hotel.member.withdrawal.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.withdrawal.exception.SocialUnlinkException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleUnlinkClient implements SocialUnlinkClient {

    private static final String REVOKE_URI = "https://oauth2.googleapis.com/revoke";
    private final RestClient restClient = RestClient.create();

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.GOOGLE;
    }

    @Override
    public void unlink(String accessToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.set("token", accessToken);

        try {
            restClient.post()
                    .uri(REVOKE_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new SocialUnlinkException("구글 계정 연결을 해제하지 못했습니다.", exception);
        }
    }
}
