package com.mjc.hotel.auth.oauth;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSocialLoginService {

    private static final int SOCIAL_SIGNUP_POINT = 5000;

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository memberAuthAccountRepository;

    @Transactional
    public Member loginOrSignup(GoogleOAuth2UserInfo userInfo) {
        validateVerifiedEmail(userInfo);

        return memberAuthAccountRepository
                .findActiveByProviderAndProviderUserId(
                        MemberAuthProvider.GOOGLE,
                        userInfo.providerUserId()
                )
                .map(this::loginExistingMember)
                .orElseGet(() -> signupOrLinkGoogleMember(userInfo));
    }

    private Member loginExistingMember(MemberAuthAccount authAccount) {
        Member member = authAccount.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "inactive_member",
                    "로그인할 수 없는 회원입니다."
            );
        }

        authAccount.setLastLoginAt(LocalDateTime.now());
        member.setEmailVerified(true);
        return member;
    }

    private Member signupOrLinkGoogleMember(GoogleOAuth2UserInfo userInfo) {
        List<Member> membersWithSameEmail = memberRepository.findAllActiveByEmail(userInfo.email());
        if (membersWithSameEmail.size() > 1) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "ambiguous_email_account",
                    "동일한 이메일을 사용하는 회원이 여러 명이라 구글 계정을 안전하게 연결할 수 없습니다."
            );
        }

        if (membersWithSameEmail.size() == 1) {
            return linkGoogleAccount(membersWithSameEmail.getFirst(), userInfo);
        }

        Member member = Member.builder()
                .name(userInfo.name())
                .email(userInfo.email())
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(SOCIAL_SIGNUP_POINT)
                .build();
        Member savedMember = memberRepository.save(member);

        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(savedMember)
                .provider(MemberAuthProvider.GOOGLE)
                .providerUserId(userInfo.providerUserId())
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberAuthAccountRepository.save(authAccount);

        return savedMember;
    }

    private Member linkGoogleAccount(Member member, GoogleOAuth2UserInfo userInfo) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "inactive_member",
                    "로그인할 수 없는 회원입니다."
            );
        }

        if (memberAuthAccountRepository
                .findActiveByMemberSidAndProvider(member.getSid(), MemberAuthProvider.GOOGLE)
                .isPresent()) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "google_account_conflict",
                    "이미 다른 구글 계정이 연결된 회원입니다."
            );
        }

        MemberAuthAccount googleAuthAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.GOOGLE)
                .providerUserId(userInfo.providerUserId())
                .lastLoginAt(LocalDateTime.now())
                .build();
        memberAuthAccountRepository.save(googleAuthAccount);

        member.setEmailVerified(true);
        return member;
    }

    private void validateVerifiedEmail(GoogleOAuth2UserInfo userInfo) {
        if (!userInfo.emailVerified()) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "google_email_not_verified",
                    "인증되지 않은 구글 이메일로는 로그인할 수 없습니다."
            );
        }
    }
}
