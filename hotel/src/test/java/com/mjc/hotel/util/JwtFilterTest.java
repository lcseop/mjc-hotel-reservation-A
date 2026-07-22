package com.mjc.hotel.util;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtFilterTest {

    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final JwtFilter jwtFilter = new JwtFilter(jwtProvider, memberRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void oauth2EndpointsAreNotParsedAsStayNowJwt() {
        assertThat(shouldNotFilter("/oauth2/authorization/google")).isTrue();
        assertThat(shouldNotFilter("/login/oauth2/code/google")).isTrue();
        assertThat(shouldNotFilter("/api/auth/oauth2/success")).isTrue();
        assertThat(shouldNotFilter("/api/auth/oauth2/failure")).isTrue();
    }

    @Test
    void regularApiEndpointsStillUseStayNowJwtFilter() {
        assertThat(shouldNotFilter("/api/member/1")).isFalse();
    }

    @Test
    void authenticatesAValidTokenOnlyForAnActiveMember() throws Exception {
        String email = "member@example.com";
        when(jwtProvider.validateAccessToken("access-token")).thenReturn(true);
        when(jwtProvider.getName("access-token")).thenReturn(email);
        when(memberRepository.findActiveByEmail(email)).thenReturn(Optional.of(
                Member.builder().email(email).status(MemberStatus.ACTIVE).build()
        ));
        MockHttpServletResponse response = filter("Bearer access-token");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(email);
    }

    @Test
    void rejectsAFormerMemberEvenWhenTheAccessTokenSignatureIsValid() throws Exception {
        String email = "withdrawn@example.com";
        when(jwtProvider.validateAccessToken("access-token")).thenReturn(true);
        when(jwtProvider.getName("access-token")).thenReturn(email);
        when(memberRepository.findActiveByEmail(email)).thenReturn(Optional.empty());
        MockHttpServletResponse response = filter("Bearer access-token");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private boolean shouldNotFilter(String requestUri) {
        return jwtFilter.shouldNotFilter(new MockHttpServletRequest("GET", requestUri));
    }

    private MockHttpServletResponse filter(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/members/me/withdraw");
        request.addHeader("Authorization", authorization);
        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtFilter.doFilterInternal(request, response, new MockFilterChain());
        return response;
    }
}
