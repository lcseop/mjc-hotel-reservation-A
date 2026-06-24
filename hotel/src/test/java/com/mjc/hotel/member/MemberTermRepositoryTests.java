package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Commit
class MemberTermRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TermRepository termRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void savesMemberAndTermToMariaDb() {
        Member member = memberRepository.saveAndFlush(
                Member.builder()
                        .name("Repository Test User")
                        .phone("010-0000-0000")
                        .email("repository-test@example.com")
                        .status(MemberStatus.ACTIVE)
                        .role(MemberRole.USER)
                        .emailVerified(false)
                        .phoneVerified(false)
                        .build()
        );

        Term term = termRepository.saveAndFlush(
                Term.builder()
                        .termType("SERVICE")
                        .title("Repository Test Service Terms")
                        .version("1.0")
                        .isRequired(true)
                        .effectiveAt(LocalDateTime.now())
                        .build()
        );

        assertNotNull(member.getMemberId());
        assertNotNull(term.getTermId());

        Long memberId = member.getMemberId();
        Long termId = term.getTermId();
        entityManager.clear();

        Member savedMember = memberRepository.findById(memberId).orElseThrow();
        Term savedTerm = termRepository.findById(termId).orElseThrow();

        assertEquals("repository-test@example.com", savedMember.getEmail());
        assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());
        assertFalse(savedMember.getEmailVerified());
        assertEquals("Repository Test Service Terms", savedTerm.getTitle());
        assertEquals("1.0", savedTerm.getVersion());
        assertTrue(savedTerm.getIsRequired());
    }
}
