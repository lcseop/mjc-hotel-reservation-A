package com.mjc.hotel.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtFilterTest {

    private final JwtFilter jwtFilter = new JwtFilter(mock(JwtProvider.class));

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

    private boolean shouldNotFilter(String requestUri) {
        return jwtFilter.shouldNotFilter(new MockHttpServletRequest("GET", requestUri));
    }
}
