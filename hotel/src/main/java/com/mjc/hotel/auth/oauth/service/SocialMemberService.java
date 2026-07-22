package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SocialMemberService {

    private static final int SOCIAL_SIGNUP_POINT = 5000;

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository memberAuthAccountRepository;

    @Transactional
    public Member loginOrSignup(SocialUserInfo userInfo) {
        validateProvider(userInfo);

        return memberAuthAccountRepository
                .findActiveByProviderAndProviderUserId(
                        userInfo.provider(),
                        userInfo.providerUserId()
                )
                .map(authAccount -> loginExistingMember(authAccount, userInfo))
                .orElseGet(() -> signupOrLinkMember(userInfo));
    }

    private Member loginExistingMember(MemberAuthAccount authAccount, SocialUserInfo userInfo) {
        Member member = authAccount.getMember();
        validateActiveMember(member);

        authAccount.setLastLoginAt(LocalDateTime.now());
        if (userInfo.emailVerified()) {
            member.setEmailVerified(true);
        }
        return member;
    }

    private Member signupOrLinkMember(SocialUserInfo userInfo) {
        validateSignupInformation(userInfo);

        List<Member> membersWithSameEmail = memberRepository.findAllActiveByEmail(userInfo.email());
        if (membersWithSameEmail.size() > 1) {
            throw oauth2Exception(
                    "ambiguous_email_account",
                    "동일한 이메일을 사용하는 회원이 여러 명이라 소셜 계정을 안전하게 연결할 수 없습니다."
            );
        }

        if (membersWithSameEmail.size() == 1) {
            if (!userInfo.emailVerified()) {
                throw oauth2Exception(
                        "account_link_required",
                        "기존 회원과 이메일이 같습니다. 기존 계정으로 로그인한 뒤 소셜 계정을 연결해 주세요."
                );
            }
            return linkSocialAccount(membersWithSameEmail.getFirst(), userInfo);
        }

        Member member = Member.builder()
                .name(userInfo.name())
                .email(userInfo.email())
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(userInfo.emailVerified())
                .point(SOCIAL_SIGNUP_POINT)
                .build();
        Member savedMember = memberRepository.save(member);

        memberAuthAccountRepository.save(createAuthAccount(savedMember, userInfo));
        return savedMember;
    }

    private Member linkSocialAccount(Member member, SocialUserInfo userInfo) {
        validateActiveMember(member);

        if (memberAuthAccountRepository
                .findActiveByMemberSidAndProvider(member.getSid(), userInfo.provider())
                .isPresent()) {
            throw oauth2Exception(
                    "social_account_conflict",
                    "이미 다른 " + providerName(userInfo.provider()) + " 계정이 연결된 회원입니다."
            );
        }

        memberAuthAccountRepository.save(createAuthAccount(member, userInfo));
        member.setEmailVerified(true);
        return member;
    }

    private MemberAuthAccount createAuthAccount(Member member, SocialUserInfo userInfo) {
        return MemberAuthAccount.builder()
                .member(member)
                .provider(userInfo.provider())
                .providerUserId(userInfo.providerUserId())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    private void validateProvider(SocialUserInfo userInfo) {
        if (userInfo == null
                || userInfo.provider() == null
                || userInfo.provider() == MemberAuthProvider.LOCAL
                || userInfo.providerUserId() == null
                || userInfo.providerUserId().isBlank()) {
            throw oauth2Exception(
                    "invalid_social_user",
                    "유효한 소셜 사용자 정보가 없습니다."
            );
        }
    }

    private void validateSignupInformation(SocialUserInfo userInfo) {
        if (userInfo.email() == null || userInfo.email().isBlank()) {
            throw oauth2Exception(
                    providerCode(userInfo.provider()) + "_email_required",
                    providerName(userInfo.provider()) + " 계정의 이메일 제공 동의가 필요합니다."
            );
        }

        if (requiresVerifiedEmail(userInfo.provider()) && !userInfo.emailVerified()) {
            throw oauth2Exception(
                    providerCode(userInfo.provider()) + "_email_not_verified",
                    "인증되지 않은 " + providerName(userInfo.provider()) + " 이메일로는 가입할 수 없습니다."
            );
        }
    }

    private boolean requiresVerifiedEmail(MemberAuthProvider provider) {
        return provider == MemberAuthProvider.GOOGLE || provider == MemberAuthProvider.KAKAO;
    }

    private void validateActiveMember(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw oauth2Exception("inactive_member", "로그인할 수 없는 회원입니다.");
        }
    }

    private String providerCode(MemberAuthProvider provider) {
        return provider.name().toLowerCase(Locale.ROOT);
    }

    private String providerName(MemberAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "구글";
            case KAKAO -> "카카오";
            case NAVER -> "네이버";
            case LOCAL -> "로컬";
        };
    }

    private OAuth2AuthenticationException oauth2Exception(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), description);
    }
}
