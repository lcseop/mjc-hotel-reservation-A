package com.mjc.hotel.member.entity;

import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberMappingTests {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Commit
    public void testMember() {
        Member member = Member
                .builder()
                .name("member")
                .phone("123456789")
                .email("lll")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .emailVerified(true)
                .phoneVerified(true)
                .build();
        memberRepository.save(member);
    }
}
