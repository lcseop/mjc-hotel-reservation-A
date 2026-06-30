package com.mjc.hotel.member_term;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberTermAgreementServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @DisplayName("memberTermAgreementTestData")
    @Test
    @Commit
    @Transactional
    public void addMemberTermAgreementTest() {
        Member member = memberRepository.save(Member
                .builder()
                .name("약관 동의 테스트 회원")
                .phone("010-7777-8888")
                .email("term-agreement-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .point(700)
                .build());

        Term term = termRepository.save(Term
                .builder()
                .termType("PRIVACY")
                .title("개인정보 처리방침")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build());

        MemberTermAgreement termAgreement = MemberTermAgreement
                .builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build();

        memberTermAgreementRepository.save(termAgreement);
    }
}
