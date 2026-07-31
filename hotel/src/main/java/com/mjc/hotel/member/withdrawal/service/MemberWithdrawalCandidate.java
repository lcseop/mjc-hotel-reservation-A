package com.mjc.hotel.member.withdrawal.service;

import com.mjc.hotel.member.entity.MemberAuthProvider;

import java.util.List;

public record MemberWithdrawalCandidate(
        Long memberSid,
        boolean localAccount,
        String localPasswordHash,
        List<MemberAuthProvider> socialProviders
) {
}
