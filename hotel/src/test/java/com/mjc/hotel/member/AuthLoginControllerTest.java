package com.mjc.hotel.member;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class AuthLoginControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("authLoginTestData")
    @Test
    @Commit
    @Transactional
    public void addAuthLoginTest() {
        Member member = Member
                .builder()
                .name("로그인 테스트")
                .phone("010-1111-2222")
                .email("login-test1@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(false)
                .point(0)
                .build();

        Member savedMember = memberRepository.save(member);

        MemberAuthAccount authAccount = MemberAuthAccount
                .builder()
                .member(savedMember)
                .provider(MemberAuthProvider.LOCAL)
                .providerUserId("login-test@mjc.com")
                .passwordHash(passwordEncoder.encode("1234567899"))
                .build();

        memberAuthAccountRepository.save(authAccount);
    }
}
