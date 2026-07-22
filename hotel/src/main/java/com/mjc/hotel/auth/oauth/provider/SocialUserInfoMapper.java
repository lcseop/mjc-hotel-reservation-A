package com.mjc.hotel.auth.oauth.provider;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.member.entity.MemberAuthProvider;

import java.util.Map;

public interface SocialUserInfoMapper {

    MemberAuthProvider provider();

    SocialUserInfo map(Map<String, Object> attributes);
}
