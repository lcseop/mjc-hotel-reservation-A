package com.mjc.hotel.member;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private MemberAuthAccountRepository memberAuthAccountRepository;
    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @DisplayName("회원 생성 API는 ApiResponse 형식으로 생성 결과를 반환한다")
    @Test
    public void insertMemberApiTest() throws Exception {
        MemberRequestDto request = buildRequest("회원 생성", "create-member-api@mjc.com");

        mockMvc.perform(post("/api/member/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.email").value("create-member-api@mjc.com"));
    }

    @DisplayName("회원가입 API는 회원, 인증 계정, 약관 동의를 함께 생성한다")
    @Test
    public void signupMemberApiTest() throws Exception {
        Term serviceTerm = saveTerm("SERVICE", "회원가입 서비스 이용약관", true);
        Term marketingTerm = saveTerm("MARKETING", "마케팅 수신 동의", false);

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSignupJson(serviceTerm.getSid(), marketingTerm.getSid())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member signup success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.email").value("signup-member-api@mjc.com"));

        assertThat(memberAuthAccountRepository.findAll())
                .anySatisfy(authAccount -> {
                    assertThat(authAccount.getProvider()).isEqualTo("LOCAL");
                    assertThat(authAccount.getProviderUserId()).isEqualTo("signup-member-api@mjc.com");
                    assertThat(authAccount.getMember().getEmail()).isEqualTo("signup-member-api@mjc.com");
                });
        assertThat(memberTermAgreementRepository.findAll())
                .anySatisfy(agreement -> {
                    assertThat(agreement.getTerm().getSid()).isEqualTo(serviceTerm.getSid());
                    assertThat(agreement.getIsAgreed()).isTrue();
                    assertThat(agreement.getMember().getEmail()).isEqualTo("signup-member-api@mjc.com");
                })
                .anySatisfy(agreement -> {
                    assertThat(agreement.getTerm().getSid()).isEqualTo(marketingTerm.getSid());
                    assertThat(agreement.getIsAgreed()).isFalse();
                    assertThat(agreement.getMember().getEmail()).isEqualTo("signup-member-api@mjc.com");
                });
    }

    @DisplayName("회원 단건 조회 API는 ApiResponse 형식으로 조회 결과를 반환한다")
    @Test
    public void getMemberApiTest() throws Exception {
        Long sid = memberService.saveMember(buildMember("회원 조회", "read-member-api@mjc.com")).getSid();

        mockMvc.perform(get("/api/member/{sid}", sid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member select success"))
                .andExpect(jsonPath("$.data.sid").value(sid))
                .andExpect(jsonPath("$.data.email").value("read-member-api@mjc.com"));
    }

    @DisplayName("회원 삭제 API는 회원을 물리 삭제하지 않고 삭제 표시 후 조회할 수 있게 한다")
    @Test
    public void deleteMemberApiSoftDeleteTest() throws Exception {
        Long sid = memberService.saveMember(buildMember("회원 삭제", "soft-delete-member-api@mjc.com")).getSid();

        mockMvc.perform(delete("/api/member/{sid}", sid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("member delete success"));

        mockMvc.perform(get("/api/member/{sid}", sid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.sid").value(sid))
                .andExpect(jsonPath("$.data.email").value("soft-delete-member-api@mjc.com"))
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.deletedAt", notNullValue()));
    }

    private MemberRequestDto buildRequest(String name, String email) {
        return MemberRequestDto.builder()
                .name(name)
                .phone("010-1234-5678")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
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

    private Term saveTerm(String termType, String title, Boolean isRequired) {
        return termRepository.save(Term.builder()
                .termType(termType)
                .title(title)
                .version("1.0")
                .isRequired(isRequired)
                .effectiveAt(LocalDateTime.now())
                .build());
    }

    private String toJson(MemberRequestDto request) {
        return """
                {
                  "name": "%s",
                  "phone": "%s",
                  "email": "%s",
                  "status": "%s",
                  "role": "%s",
                  "emailVerified": %s,
                  "phoneVerified": %s
                }
                """.formatted(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getStatus(),
                request.getRole(),
                request.getEmailVerified(),
                request.getPhoneVerified()
        );
    }

    private String toSignupJson(Long servicesid, Long marketingsid) {
        return """
                {
                  "name": "회원가입 생성",
                  "phone": "010-1111-2222",
                  "email": "signup-member-api@mjc.com",
                  "status": "ACTIVE",
                  "role": "USER",
                  "emailVerified": false,
                  "phoneVerified": false,
                  "authAccount": {
                    "provider": "LOCAL",
                    "providerUserId": "signup-member-api@mjc.com",
                    "passwordHash": "signup-password-hash"
                  },
                  "termAgreements": [
                    {
                      "sid": %d,
                      "isAgreed": true
                    },
                    {
                      "sid": %d,
                      "isAgreed": false
                    }
                  ]
                }
                """.formatted(servicesid, marketingsid);
    }
}
