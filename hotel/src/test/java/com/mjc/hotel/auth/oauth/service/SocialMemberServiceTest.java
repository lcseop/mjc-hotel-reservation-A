package com.mjc.hotel.auth.oauth.service;

import com.mjc.hotel.auth.oauth.model.SocialUserInfo;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SocialMemberServiceTest {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final MemberAuthAccountRepository authAccountRepository = mock(MemberAuthAccountRepository.class);
    private final SocialMemberService service =
            new SocialMemberService(memberRepository, authAccountRepository);

    @Test
    void verifiedSocialEmailLinksToTheOnlyExistingMember() {
        SocialUserInfo userInfo = socialUser(
                MemberAuthProvider.GOOGLE,
                "google-sub",
                "member@example.com",
                true
        );
        Member existingMember = activeMember(7L, "member@example.com");
        existingMember.setEmailVerified(false);

        when(authAccountRepository.findActiveByProviderAndProviderUserId(
                MemberAuthProvider.GOOGLE,
                "google-sub"
        )).thenReturn(Optional.empty());
        when(memberRepository.findAllActiveByEmail("member@example.com"))
                .thenReturn(List.of(existingMember));
        when(authAccountRepository.findActiveByMemberSidAndProvider(7L, MemberAuthProvider.GOOGLE))
                .thenReturn(Optional.empty());

        Member result = service.loginOrSignup(userInfo);

        ArgumentCaptor<MemberAuthAccount> accountCaptor = ArgumentCaptor.forClass(MemberAuthAccount.class);
        verify(authAccountRepository).save(accountCaptor.capture());
        MemberAuthAccount linkedAccount = accountCaptor.getValue();

        assertThat(result).isSameAs(existingMember);
        assertThat(existingMember.getEmailVerified()).isTrue();
        assertThat(linkedAccount.getMember()).isSameAs(existingMember);
        assertThat(linkedAccount.getProvider()).isEqualTo(MemberAuthProvider.GOOGLE);
        assertThat(linkedAccount.getProviderUserId()).isEqualTo("google-sub");
        assertThat(linkedAccount.getLastLoginAt()).isNotNull();
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void newVerifiedSocialEmailCreatesMemberAndAuthAccount() {
        SocialUserInfo userInfo = socialUser(
                MemberAuthProvider.KAKAO,
                "123456789",
                "new@example.com",
                true
        );

        when(authAccountRepository.findActiveByProviderAndProviderUserId(
                MemberAuthProvider.KAKAO,
                "123456789"
        )).thenReturn(Optional.empty());
        when(memberRepository.findAllActiveByEmail("new@example.com")).thenReturn(List.of());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setSid(8L);
            return member;
        });

        Member result = service.loginOrSignup(userInfo);

        ArgumentCaptor<MemberAuthAccount> accountCaptor = ArgumentCaptor.forClass(MemberAuthAccount.class);
        verify(authAccountRepository).save(accountCaptor.capture());

        assertThat(result.getSid()).isEqualTo(8L);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPoint()).isEqualTo(5000);
        assertThat(result.getEmailVerified()).isTrue();
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(MemberAuthProvider.KAKAO);
    }

    @Test
    void newNaverMemberKeepsEmailUnverifiedAndUsesProviderIdForAuthentication() {
        SocialUserInfo userInfo = socialUser(
                MemberAuthProvider.NAVER,
                "naver-id",
                "naver@example.com",
                false
        );

        when(authAccountRepository.findActiveByProviderAndProviderUserId(
                MemberAuthProvider.NAVER,
                "naver-id"
        )).thenReturn(Optional.empty());
        when(memberRepository.findAllActiveByEmail("naver@example.com")).thenReturn(List.of());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setSid(9L);
            return member;
        });

        Member result = service.loginOrSignup(userInfo);

        ArgumentCaptor<MemberAuthAccount> accountCaptor = ArgumentCaptor.forClass(MemberAuthAccount.class);
        verify(authAccountRepository).save(accountCaptor.capture());
        assertThat(result.getEmailVerified()).isFalse();
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(MemberAuthProvider.NAVER);
        assertThat(accountCaptor.getValue().getProviderUserId()).isEqualTo("naver-id");
    }

    @Test
    void unverifiedNaverEmailDoesNotAutomaticallyLinkExistingMember() {
        SocialUserInfo userInfo = socialUser(
                MemberAuthProvider.NAVER,
                "naver-id",
                "member@example.com",
                false
        );

        when(authAccountRepository.findActiveByProviderAndProviderUserId(
                MemberAuthProvider.NAVER,
                "naver-id"
        )).thenReturn(Optional.empty());
        when(memberRepository.findAllActiveByEmail("member@example.com"))
                .thenReturn(List.of(activeMember(9L, "member@example.com")));

        assertThatThrownBy(() -> service.loginOrSignup(userInfo))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode())
                                .isEqualTo("account_link_required")
                );
        verify(authAccountRepository, never()).save(any(MemberAuthAccount.class));
    }

    @Test
    void duplicateActiveEmailDoesNotChooseAnArbitraryMember() {
        SocialUserInfo userInfo = socialUser(
                MemberAuthProvider.GOOGLE,
                "google-sub",
                "duplicate@example.com",
                true
        );

        when(authAccountRepository.findActiveByProviderAndProviderUserId(
                MemberAuthProvider.GOOGLE,
                "google-sub"
        )).thenReturn(Optional.empty());
        when(memberRepository.findAllActiveByEmail("duplicate@example.com"))
                .thenReturn(List.of(
                        activeMember(10L, "duplicate@example.com"),
                        activeMember(11L, "duplicate@example.com")
                ));

        assertThatThrownBy(() -> service.loginOrSignup(userInfo))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode())
                                .isEqualTo("ambiguous_email_account")
                );
        verify(authAccountRepository, never()).save(any(MemberAuthAccount.class));
    }

    private SocialUserInfo socialUser(
            MemberAuthProvider provider,
            String providerUserId,
            String email,
            boolean emailVerified
    ) {
        return new SocialUserInfo(provider, providerUserId, email, "Social Member", emailVerified);
    }

    private Member activeMember(Long sid, String email) {
        return Member.builder()
                .sid(sid)
                .name("Existing Member")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(0)
                .build();
    }
}
