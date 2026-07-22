package com.mjc.hotel.member.withdrawal.provider;

import com.mjc.hotel.member.entity.MemberAuthProvider;

public interface SocialUnlinkClient {

    MemberAuthProvider provider();

    void unlink(String accessToken);
}
