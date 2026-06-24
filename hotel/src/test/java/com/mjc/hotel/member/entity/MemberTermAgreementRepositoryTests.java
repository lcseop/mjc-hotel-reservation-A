package com.mjc.hotel.member.entity;

import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.term.entity.Term;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class MemberTermAgreementRepositoryTests {

    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Test
    @Commit
    public void testMemberTerm() {
        Member member2 = Member.builder().memberId(1L).build();
        Term term = Term.builder().termId(1L).build();
        MemberTermAgreement memberTermAgreement = MemberTermAgreement
                .builder()
                .member(member2)
                .term(term)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .withdrawnAt(LocalDateTime.now())
                .build();
        memberTermAgreementRepository.save(memberTermAgreement);
    }
}
