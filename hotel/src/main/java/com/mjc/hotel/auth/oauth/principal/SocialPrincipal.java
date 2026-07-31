package com.mjc.hotel.auth.oauth.principal;

import com.mjc.hotel.member.entity.MemberAuthProvider;

public interface SocialPrincipal {

    Long getMemberSid();

    MemberAuthProvider getProvider();
}
