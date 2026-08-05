package com.mjc.hotel.member_auth;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberAuthAccountServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;

    @DisplayName("회원 인증 계정을 저장할 수 있다")
    @Test
    void addMemberAuthAccountTest() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .name("인증 계정 테스트 회원")
                .phone("010-1111-2222")
                .email("auth-account-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(0)
                .build());
        LocalDateTime lastLoginAt = LocalDateTime.now();

        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .member(member)
                .provider(MemberAuthProvider.KAKAO)
                .providerUserId("auth-account-test@mjc.com")
                .passwordHash("test-password-hash")
                .lastLoginAt(lastLoginAt)
                .build();

        MemberAuthAccount savedAuthAccount = memberAuthAccountRepository.saveAndFlush(authAccount);

        assertThat(savedAuthAccount.getSid()).isNotNull();
        assertThat(savedAuthAccount.getMember().getSid()).isEqualTo(member.getSid());
        assertThat(savedAuthAccount.getProvider()).isEqualTo(MemberAuthProvider.KAKAO);
        assertThat(savedAuthAccount.getProviderUserId()).isEqualTo("auth-account-test@mjc.com");
        assertThat(savedAuthAccount.getPasswordHash()).isEqualTo("test-password-hash");
        assertThat(savedAuthAccount.getLastLoginAt()).isEqualTo(lastLoginAt);
    }
}
