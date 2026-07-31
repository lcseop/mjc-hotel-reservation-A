package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("memberTestData")
    @Test
    @Commit
    public void addMemberTest() {
        Member member = Member
                .builder()
                .name("회원 테스트")
                .phone("010-1234-5678")
                .email("member-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(1000)
                .build();

        memberRepository.save(member);
    }
}
