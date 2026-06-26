package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

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
                .phoneVerified(true)
                .build();

        memberRepository.save(member);

        Term term = Term
                .builder()
                .termType("SERVICE")
                .title("회원 테스트 서비스 이용약관")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();

        termRepository.save(term);

        MemberAuthAccount memberAuthAccount = MemberAuthAccount
                .builder()
                .member(member)
                .provider("LOCAL")
                .providerUserId("member-test@mjc.com")
                .passwordHash("test-password-hash")
                .lastLoginAt(LocalDateTime.now())
                .build();

        memberAuthAccountRepository.save(memberAuthAccount);

        MemberTermAgreement memberTermAgreement = MemberTermAgreement
                .builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build();

        memberTermAgreementRepository.save(memberTermAgreement);
    }
}
