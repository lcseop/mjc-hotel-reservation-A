package com.mjc.hotel.member_auth;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberAuthAccountServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;

    @DisplayName("memberAuthAccountTestData")
    @Test
    @Commit
    @Transactional
    public void addMemberAuthAccountTest() {
        Member member = memberRepository.save(Member
                .builder()
                .name("인증 계정 테스트 회원")
                .phone("010-5555-6666")
                .email("auth-account-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .point(500)
                .build());

        MemberAuthAccount authAccount = MemberAuthAccount
                .builder()
                .member(member)
                .provider("LOCAL")
                .providerUserId("auth-account-test@mjc.com")
                .passwordHash("test-password-hash")
                .lastLoginAt(LocalDateTime.now())
                .build();

        memberAuthAccountRepository.save(authAccount);
    }
}
