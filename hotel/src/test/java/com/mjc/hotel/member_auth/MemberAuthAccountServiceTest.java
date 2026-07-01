package com.mjc.hotel.member_auth;

import com.mjc.hotel.member.entity.*;
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
        Member member = memberRepository.findById(1L).orElseThrow();

        MemberAuthAccount authAccount = MemberAuthAccount
                .builder()
                .member(member)
                .provider(MemberAuthProvider.KAKAO)
                .providerUserId("auth-account-test@mjc.com")
                .passwordHash("test-password-hash")
                .lastLoginAt(LocalDateTime.now())
                .build();

        memberAuthAccountRepository.save(authAccount);
    }
}
