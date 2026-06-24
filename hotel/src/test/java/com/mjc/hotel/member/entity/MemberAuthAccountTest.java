package com.mjc.hotel.member.entity;

import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberAuthAccountTest {
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;

    @Test
    @Commit
    public void testMemberAccount() {
        Member member = Member.builder().memberId(1L).build();
        MemberAuthAccount memberAuthAccount = MemberAuthAccount
                .builder()
                .member(member)
                .provider("protest")
                .providerUserId("userid")
                .passwordHash("ha3")
                .lastLoginAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        memberAuthAccountRepository.save(memberAuthAccount);
    }
}
