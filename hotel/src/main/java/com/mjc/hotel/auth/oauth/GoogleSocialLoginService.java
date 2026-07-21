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
                .orElseGet(() -> signupGoogleMember(userInfo));
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

    private Member signupGoogleMember(GoogleOAuth2UserInfo userInfo) {
        if (memberRepository.countActiveByEmail(userInfo.email()) > 0) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "account_link_required",
                    "동일한 이메일의 기존 계정이 있습니다. 로그인 후 구글 계정을 연결해 주세요."
            );
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

    private void validateVerifiedEmail(GoogleOAuth2UserInfo userInfo) {
        if (!userInfo.emailVerified()) {
            throw GoogleOAuth2UserInfo.oauth2Exception(
                    "google_email_not_verified",
                    "인증되지 않은 구글 이메일로는 로그인할 수 없습니다."
            );
        }
    }
}
