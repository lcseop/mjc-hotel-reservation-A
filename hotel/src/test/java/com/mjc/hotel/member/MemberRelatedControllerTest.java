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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberRelatedControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private EntityManager entityManager;

    @DisplayName("로그인 인증 정보 생성 API는 회원 sid로 인증 정보를 생성한다")
    @Test
    public void insertAuthAccountApiTest() throws Exception {
        Member member = saveMember("인증 생성 회원", "create-auth-account@mjc.com");

        mockMvc.perform(post("/api/member-auth-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toAuthAccountJson(member.getSid(), "LOCAL", "create-auth-account@mjc.com", "created-password-hash")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member auth account insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.data.providerUserId").value("create-auth-account@mjc.com"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @DisplayName("로그인 인증 정보 단건 조회 API는 회원 sid와 인증 정보를 반환한다")
    @Test
    public void getAuthAccountApiTest() throws Exception {
        Member member = saveMember("인증 조회 회원", "read-auth-account@mjc.com");
        MemberAuthAccount authAccount = saveAuthAccount(member);

        mockMvc.perform(get("/api/member-auth-accounts/{sid}", authAccount.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member auth account select success"))
                .andExpect(jsonPath("$.data.sid").value(authAccount.getSid()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.data.providerUserId").value("read-auth-account@mjc.com"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.deleted").value(false));
    }

    @DisplayName("회원별 로그인 인증 정보 목록 조회 API는 해당 회원의 인증 정보를 반환한다")
    @Test
    public void getAuthAccountsByMemberApiTest() throws Exception {
        Member member = saveMember("인증 목록 회원", "list-auth-account@mjc.com");
        MemberAuthAccount authAccount = saveAuthAccount(member);

        mockMvc.perform(get("/api/member/{memberSid}/auth-accounts", member.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member auth account select success"))
                .andExpect(jsonPath("$.data[0].sid").value(authAccount.getSid()))
                .andExpect(jsonPath("$.data[0].memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data[0].provider").value("LOCAL"));
    }

    @DisplayName("로그인 인증 정보 수정 API는 인증 정보를 수정한다")
    @Test
    public void updateAuthAccountApiTest() throws Exception {
        Member member = saveMember("인증 수정 회원", "update-auth-account@mjc.com");
        MemberAuthAccount authAccount = saveAuthAccount(member);

        mockMvc.perform(put("/api/member-auth-accounts/{sid}", authAccount.getSid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toAuthAccountJson(member.getSid(), "KAKAO", "kakao-update-user", "updated-password-hash")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member auth account update success"))
                .andExpect(jsonPath("$.data.sid").value(authAccount.getSid()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.provider").value("KAKAO"))
                .andExpect(jsonPath("$.data.providerUserId").value("kakao-update-user"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        entityManager.flush();
        entityManager.clear();

        MemberAuthAccount updatedAuthAccount = memberAuthAccountRepository.findById(authAccount.getSid()).orElseThrow();
        assertThat(updatedAuthAccount.getPasswordHash()).isEqualTo("updated-password-hash");
    }

    @DisplayName("로그인 인증 정보 삭제 API는 물리 삭제하지 않고 삭제 표시한다")
    @Test
    public void deleteAuthAccountApiTest() throws Exception {
        Member member = saveMember("인증 삭제 회원", "delete-auth-account@mjc.com");
        MemberAuthAccount authAccount = saveAuthAccount(member);

        mockMvc.perform(delete("/api/member-auth-accounts/{sid}", authAccount.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member auth account delete success"));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/member-auth-accounts/{sid}", authAccount.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sid").value(authAccount.getSid()))
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.deletedAt", notNullValue()));
    }

    @DisplayName("회원 약관 동의 생성 API는 회원 sid와 약관 sid로 약관 동의를 생성한다")
    @Test
    public void insertTermAgreementApiTest() throws Exception {
        Member member = saveMember("동의 생성 회원", "create-term-agreement@mjc.com");
        Term term = saveTerm();

        mockMvc.perform(post("/api/member-term-agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toTermAgreementJson(member.getSid(), term.getSid(), true, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member term agreement insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.termSid").value(term.getSid()))
                .andExpect(jsonPath("$.data.isAgreed").value(true));
    }

    @DisplayName("회원 약관 동의 단건 조회 API는 회원 sid와 약관 sid를 반환한다")
    @Test
    public void getTermAgreementApiTest() throws Exception {
        Member member = saveMember("동의 조회 회원", "read-term-agreement@mjc.com");
        Term term = saveTerm();
        MemberTermAgreement termAgreement = saveTermAgreement(member, term);

        mockMvc.perform(get("/api/member-term-agreements/{sid}", termAgreement.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member term agreement select success"))
                .andExpect(jsonPath("$.data.sid").value(termAgreement.getSid()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.termSid").value(term.getSid()))
                .andExpect(jsonPath("$.data.isAgreed").value(true))
                .andExpect(jsonPath("$.data.deleted").value(false));
    }

    @DisplayName("회원별 약관 동의 목록 조회 API는 해당 회원의 약관 동의를 반환한다")
    @Test
    public void getTermAgreementsByMemberApiTest() throws Exception {
        Member member = saveMember("동의 목록 회원", "list-term-agreement@mjc.com");
        Term term = saveTerm();
        MemberTermAgreement termAgreement = saveTermAgreement(member, term);

        mockMvc.perform(get("/api/member/{memberSid}/term-agreements", member.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member term agreement select success"))
                .andExpect(jsonPath("$.data[0].sid").value(termAgreement.getSid()))
                .andExpect(jsonPath("$.data[0].memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data[0].termSid").value(term.getSid()));
    }

    @DisplayName("회원 약관 동의 수정 API는 약관 동의를 수정한다")
    @Test
    public void updateTermAgreementApiTest() throws Exception {
        Member member = saveMember("동의 수정 회원", "update-term-agreement@mjc.com");
        Term term = saveTerm();
        MemberTermAgreement termAgreement = saveTermAgreement(member, term);

        mockMvc.perform(put("/api/member-term-agreements/{sid}", termAgreement.getSid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toTermAgreementJson(member.getSid(), term.getSid(), false, LocalDateTime.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member term agreement update success"))
                .andExpect(jsonPath("$.data.sid").value(termAgreement.getSid()))
                .andExpect(jsonPath("$.data.memberSid").value(member.getSid()))
                .andExpect(jsonPath("$.data.termSid").value(term.getSid()))
                .andExpect(jsonPath("$.data.isAgreed").value(false))
                .andExpect(jsonPath("$.data.withdrawnAt", notNullValue()));
    }

    @DisplayName("회원 약관 동의 삭제 API는 물리 삭제하지 않고 삭제 표시한다")
    @Test
    public void deleteTermAgreementApiTest() throws Exception {
        Member member = saveMember("동의 삭제 회원", "delete-term-agreement@mjc.com");
        Term term = saveTerm();
        MemberTermAgreement termAgreement = saveTermAgreement(member, term);

        mockMvc.perform(delete("/api/member-term-agreements/{sid}", termAgreement.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member term agreement delete success"));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/member-term-agreements/{sid}", termAgreement.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sid").value(termAgreement.getSid()))
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.deletedAt", notNullValue()));
    }

    private Member saveMember(String name, String email) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phone("010-1234-5678")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .phoneVerified(false)
                .build());
    }

    private MemberAuthAccount saveAuthAccount(Member member) {
        return memberAuthAccountRepository.save(MemberAuthAccount.builder()
                .member(member)
                .provider("LOCAL")
                .providerUserId(member.getEmail())
                .passwordHash("secret-password-hash")
                .lastLoginAt(LocalDateTime.now())
                .build());
    }

    private MemberTermAgreement saveTermAgreement(Member member, Term term) {
        return memberTermAgreementRepository.save(MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .agreedAt(LocalDateTime.now())
                .build());
    }

    private Term saveTerm() {
        return termRepository.save(Term.builder()
                .termType("SERVICE")
                .title("서비스 이용약관")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build());
    }

    private String toAuthAccountJson(Long memberSid, String provider, String providerUserId, String passwordHash) {
        return """
                {
                  "memberSid": %d,
                  "provider": "%s",
                  "providerUserId": "%s",
                  "passwordHash": "%s",
                  "lastLoginAt": "%s"
                }
                """.formatted(memberSid, provider, providerUserId, passwordHash, LocalDateTime.now());
    }

    private String toTermAgreementJson(Long memberSid, Long termSid, Boolean isAgreed, LocalDateTime withdrawnAt) {
        return """
                {
                  "memberSid": %d,
                  "termSid": %d,
                  "isAgreed": %s,
                  "agreedAt": "%s",
                  "withdrawnAt": %s
                }
                """.formatted(
                memberSid,
                termSid,
                isAgreed,
                LocalDateTime.now(),
                withdrawnAt == null ? null : "\"" + withdrawnAt + "\""
        );
    }
}
