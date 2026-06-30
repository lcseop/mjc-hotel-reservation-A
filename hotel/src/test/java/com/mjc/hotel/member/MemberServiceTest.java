package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;
    @Autowired
    private EntityManager entityManager;

    @DisplayName("회원과 인증 계정, 약관 동의를 생성하고 커밋한다")
    @Test
    @Commit
    public void createMemberTest() {
        Term term = saveTerm();
        MemberAuthAccount authAccount = buildAuthAccount();
        MemberTermAgreement termAgreement = buildTermAgreement(term);

        Member savedMember = memberService.createMember(
                buildMember("회원 생성", "create-member@mjc.com"),
                authAccount,
                List.of(termAgreement)
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(savedMember.getSid()).isNotNull();
        assertThat(memberRepository.findById(savedMember.getSid())).isPresent();
        assertThat(memberAuthAccountRepository.findAll())
                .anySatisfy(savedAuthAccount -> {
                    assertThat(savedAuthAccount.getMember().getSid()).isEqualTo(savedMember.getSid());
                    assertThat(savedAuthAccount.getProvider()).isEqualTo("LOCAL");
                    assertThat(savedAuthAccount.getProviderUserId()).isEqualTo("member-test@mjc.com");
                });
        assertThat(memberTermAgreementRepository.findAll())
                .anySatisfy(savedAgreement -> {
                    assertThat(savedAgreement.getMember().getSid()).isEqualTo(savedMember.getSid());
                    assertThat(savedAgreement.getTerm().getSid()).isEqualTo(term.getSid());
                    assertThat(savedAgreement.getIsAgreed()).isTrue();
                });
    }

    @DisplayName("회원 목록과 단건 회원을 조회한다")
    @Test
    public void readMemberTest() {
        Member savedMember = memberService.saveMember(buildMember("회원 조회", "read-member@mjc.com"));

        assertThat(memberService.getMembers())
                .extracting(Member::getSid)
                .contains(savedMember.getSid());
        assertThat(memberService.getMember(savedMember.getSid()).getEmail()).isEqualTo("read-member@mjc.com");
    }

    @DisplayName("회원 정보를 수정한다")
    @Test
    public void updateMemberTest() {
        Member savedMember = memberService.saveMember(buildMember("회원 수정 전", "before-update@mjc.com"));
        Member updateRequest = Member.builder()
                .name("회원 수정 후")
                .phone("010-9999-8888")
                .email("after-update@mjc.com")
                .status(MemberStatus.STOP)
                .role(MemberRole.ADMIN)
                .emailVerified(true)
                .phoneVerified(true)
                .build();

        Member updatedMember = memberService.updateMember(savedMember.getSid(), updateRequest);

        assertThat(updatedMember.getSid()).isEqualTo(savedMember.getSid());
        assertThat(updatedMember.getName()).isEqualTo("회원 수정 후");
        assertThat(updatedMember.getPhone()).isEqualTo("010-9999-8888");
        assertThat(updatedMember.getEmail()).isEqualTo("after-update@mjc.com");
        assertThat(updatedMember.getStatus()).isEqualTo(MemberStatus.STOP);
        assertThat(updatedMember.getRole()).isEqualTo(MemberRole.ADMIN);
        assertThat(updatedMember.getEmailVerified()).isTrue();
        assertThat(updatedMember.getPhoneVerified()).isTrue();

        entityManager.flush();
        entityManager.clear();

        Member foundMember = memberService.getMember(savedMember.getSid());
        assertThat(foundMember.getEmail()).isEqualTo("after-update@mjc.com");
    }

    @DisplayName("회원을 삭제한다")
    @Test
    public void deleteMemberTest() {
        Member savedMember = memberService.saveMember(buildMember("회원 삭제", "delete-member@mjc.com"));

        memberService.deleteMember(savedMember.getSid());
        entityManager.flush();
        entityManager.clear();

        Member deletedMember = memberRepository.findById(savedMember.getSid()).orElseThrow();
        assertThat(deletedMember.getDeleted()).isTrue();
        assertThat(deletedMember.getDeletedAt()).isNotNull();
    }

    @DisplayName("존재하지 않는 회원 삭제는 예외를 던진다")
    @Test
    public void deleteMissingMemberTest() {
        assertThatThrownBy(() -> memberService.deleteMember(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다. sid=0");
    }

    @DisplayName("로그인 인증 정보를 삭제 표시한다")
    @Test
    public void deleteAuthAccountTest() {
        Member savedMember = memberService.saveMember(buildMember("인증 삭제", "delete-auth@mjc.com"));
        MemberAuthAccount authAccount = buildAuthAccount();
        authAccount.setMember(savedMember);
        MemberAuthAccount savedAuthAccount = memberAuthAccountRepository.save(authAccount);

        memberService.deleteAuthAccount(savedAuthAccount.getSid());
        entityManager.flush();
        entityManager.clear();

        MemberAuthAccount deletedAuthAccount = memberAuthAccountRepository.findById(savedAuthAccount.getSid()).orElseThrow();
        assertThat(deletedAuthAccount.getDeleted()).isTrue();
        assertThat(deletedAuthAccount.getDeletedAt()).isNotNull();
    }

    @DisplayName("존재하지 않는 로그인 인증 정보 삭제는 예외를 던진다")
    @Test
    public void deleteMissingAuthAccountTest() {
        assertThatThrownBy(() -> memberService.deleteAuthAccount(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 로그인 인증 정보입니다. sid=0");
    }

    @DisplayName("회원 약관 동의를 삭제 표시한다")
    @Test
    public void deleteTermAgreementTest() {
        Member savedMember = memberService.saveMember(buildMember("동의 삭제", "delete-agreement@mjc.com"));
        Term savedTerm = saveTerm();
        MemberTermAgreement termAgreement = buildTermAgreement(savedTerm);
        termAgreement.setMember(savedMember);
        MemberTermAgreement savedTermAgreement = memberTermAgreementRepository.save(termAgreement);

        memberService.deleteTermAgreement(savedTermAgreement.getSid());
        entityManager.flush();
        entityManager.clear();

        MemberTermAgreement deletedTermAgreement = memberTermAgreementRepository.findById(savedTermAgreement.getSid()).orElseThrow();
        assertThat(deletedTermAgreement.getDeleted()).isTrue();
        assertThat(deletedTermAgreement.getDeletedAt()).isNotNull();
    }

    @DisplayName("존재하지 않는 회원 약관 동의 삭제는 예외를 던진다")
    @Test
    public void deleteMissingTermAgreementTest() {
        assertThatThrownBy(() -> memberService.deleteTermAgreement(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 약관 동의입니다. sid=0");
    }

    private Member buildMember(String name, String email) {
        return Member.builder()
                .name(name)
                .phone("010-1234-5678")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
    }

    private MemberAuthAccount buildAuthAccount() {
        return MemberAuthAccount.builder()
                .provider("LOCAL")
                .providerUserId("member-test@mjc.com")
                .passwordHash("test-password-hash")
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    private MemberTermAgreement buildTermAgreement(Term term) {
        return MemberTermAgreement.builder()
                .term(term)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build();
    }

    private Term saveTerm() {
        Term term = Term.builder()
                .termType("SERVICE")
                .title("회원 테스트 서비스 이용약관")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();

        return termRepository.save(term);
    }
}
