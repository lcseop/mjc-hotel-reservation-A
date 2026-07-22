package com.mjc.hotel.auth.oauth.provider;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.provider.google.GoogleUserInfoMapper;
import com.mjc.hotel.auth.oauth.provider.kakao.KakaoUserInfoMapper;
import com.mjc.hotel.auth.oauth.provider.naver.NaverUserInfoMapper;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SocialUserInfoMapperTest {

    @Test
    void mapsGoogleClaims() {
        GoogleUserInfoMapper mapper = new GoogleUserInfoMapper();

        SocialUserInfo result = mapper.map(Map.of(
                "sub", "google-sub",
                "email", "Member@Example.com",
                "name", "Google Member",
                "email_verified", true
        ));

        assertThat(result.provider()).isEqualTo(MemberAuthProvider.GOOGLE);
        assertThat(result.providerUserId()).isEqualTo("google-sub");
        assertThat(result.email()).isEqualTo("member@example.com");
        assertThat(result.name()).isEqualTo("Google Member");
        assertThat(result.emailVerified()).isTrue();
    }

    @Test
    void mapsNestedKakaoResponse() {
        KakaoUserInfoMapper mapper = new KakaoUserInfoMapper();

        SocialUserInfo result = mapper.map(Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                        "email", "Kakao@Example.com",
                        "is_email_valid", true,
                        "is_email_verified", true,
                        "profile", Map.of("nickname", "카카오 회원")
                )
        ));

        assertThat(result.provider()).isEqualTo(MemberAuthProvider.KAKAO);
        assertThat(result.providerUserId()).isEqualTo("123456789");
        assertThat(result.email()).isEqualTo("kakao@example.com");
        assertThat(result.name()).isEqualTo("카카오 회원");
        assertThat(result.emailVerified()).isTrue();
    }

    @Test
    void mapsWrappedNaverResponse() {
        NaverUserInfoMapper mapper = new NaverUserInfoMapper();

        SocialUserInfo result = mapper.map(Map.of(
                "resultcode", "00",
                "message", "success",
                "response", Map.of(
                        "id", "naver-id",
                        "email", "Naver@Example.com",
                        "name", "네이버 회원",
                        "nickname", "별명"
                )
        ));

        assertThat(result.provider()).isEqualTo(MemberAuthProvider.NAVER);
        assertThat(result.providerUserId()).isEqualTo("naver-id");
        assertThat(result.email()).isEqualTo("naver@example.com");
        assertThat(result.name()).isEqualTo("네이버 회원");
        assertThat(result.emailVerified()).isFalse();
    }
}
