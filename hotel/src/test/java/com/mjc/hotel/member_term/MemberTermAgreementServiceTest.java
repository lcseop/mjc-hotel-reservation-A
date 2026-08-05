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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTermAgreementServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @DisplayName("회원의 약관 동의를 저장할 수 있다")
    @Test
    void addMemberTermAgreementTest() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .name("약관 동의 테스트 회원")
                .phone("010-3333-4444")
                .email("term-agreement-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(0)
                .build());
        Term term = termRepository.saveAndFlush(Term.builder()
                .termType("SERVICE")
                .title("서비스 이용약관")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build());
        LocalDateTime agreedAt = LocalDateTime.now();

        MemberTermAgreement termAgreement = MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .agreedAt(agreedAt)
                .build();

        MemberTermAgreement savedAgreement = memberTermAgreementRepository.saveAndFlush(termAgreement);

        assertThat(savedAgreement.getSid()).isNotNull();
        assertThat(savedAgreement.getMember().getSid()).isEqualTo(member.getSid());
        assertThat(savedAgreement.getTerm().getSid()).isEqualTo(term.getSid());
        assertThat(savedAgreement.getIsAgreed()).isTrue();
        assertThat(savedAgreement.getAgreedAt()).isEqualTo(agreedAt);
        assertThat(savedAgreement.getWithdrawnAt()).isNull();
    }
}
