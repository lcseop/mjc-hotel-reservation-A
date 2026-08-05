package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.mjc.hotel.member.entity.MemberRole.USER;
import static com.mjc.hotel.member.entity.MemberStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장할 수 있다")
    @Test
    void addMemberTest() {
        Member member = Member.builder()
                .name("회원 테스트")
                .phone("010-1234-5678")
                .email("member-test@mjc.com")
                .status(ACTIVE)
                .role(USER)
                .emailVerified(true)
                .point(1000)
                .build();

        Member savedMember = memberRepository.saveAndFlush(member);

        assertThat(savedMember.getSid()).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo("member-test@mjc.com");
        assertThat(savedMember.getStatus()).isEqualTo(ACTIVE);
        assertThat(savedMember.getRole()).isEqualTo(USER);
    }
}