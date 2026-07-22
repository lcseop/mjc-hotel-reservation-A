package com.mjc.hotel.auth.oauth.model;

import com.mjc.hotel.member.entity.MemberAuthProvider;

public record SocialUserInfo(
        MemberAuthProvider provider,
        String providerUserId,
        String email,
        String name,
        boolean emailVerified
) {
}
