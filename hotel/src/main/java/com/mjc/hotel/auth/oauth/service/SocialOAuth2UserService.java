package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.principal.SocialOAuth2User;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapper;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapperRegistry;
import com.mjc.hotel.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final SocialUserInfoMapperRegistry mapperRegistry;
    private final SocialMemberService socialMemberService;
    private final SocialProviderTokenService socialProviderTokenService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialUserInfoMapper mapper = mapperRegistry.get(registrationId);

        OAuth2User oauth2User = delegate.loadUser(userRequest);
        SocialUserInfo userInfo = mapper.map(oauth2User.getAttributes());
        Member member = socialMemberService.loginOrSignup(userInfo);
        socialProviderTokenService.save(
                member.getSid(),
                userInfo.provider(),
                userRequest.getAccessToken().getTokenValue(),
                userRequest.getAccessToken().getExpiresAt()
        );

        return new SocialOAuth2User(oauth2User, member, userInfo.provider());
    }
}
