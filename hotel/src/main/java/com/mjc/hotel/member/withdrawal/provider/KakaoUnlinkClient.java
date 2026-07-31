package com.mjc.hotel.member.withdrawal.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.withdrawal.exception.SocialUnlinkException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoUnlinkClient implements SocialUnlinkClient {

    private static final String UNLINK_URI = "https://kapi.kakao.com/v1/user/unlink";
    private final RestClient restClient = RestClient.create();

    @Override
    public MemberAuthProvider provider() {
        return MemberAuthProvider.KAKAO;
    }

    @Override
    public void unlink(String accessToken) {
        try {
            restClient.post()
                    .uri(UNLINK_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new SocialUnlinkException("카카오 계정 연결을 해제하지 못했습니다.", exception);
        }
    }
}
