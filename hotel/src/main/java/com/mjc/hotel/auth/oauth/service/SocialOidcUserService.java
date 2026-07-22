package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.auth.oauth.principal.SocialOidcUser;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapper;
import com.mjc.hotel.auth.oauth.provider.SocialUserInfoMapperRegistry;
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
public class SocialOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final SocialUserInfoMapperRegistry mapperRegistry;
    private final SocialMemberService socialMemberService;
    private final SocialProviderTokenService socialProviderTokenService;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialUserInfoMapper mapper = mapperRegistry.get(registrationId);

        OidcUser oidcUser = delegate.loadUser(userRequest);
        SocialUserInfo userInfo = mapper.map(oidcUser.getAttributes());
        Member member = socialMemberService.loginOrSignup(userInfo);
        socialProviderTokenService.save(
                member.getSid(),
                userInfo.provider(),
                userRequest.getAccessToken().getTokenValue(),
                userRequest.getAccessToken().getExpiresAt()
        );

        return new SocialOidcUser(oidcUser, member, userInfo.provider());
    }
}
