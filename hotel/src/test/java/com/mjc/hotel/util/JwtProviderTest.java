package com.mjc.hotel.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String VALID_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void createsAndValidatesAccessTokenWithConfiguredSecret() {
        JwtProvider jwtProvider = new JwtProvider(VALID_SECRET);

        String token = jwtProvider.createAccessToken("member@example.com");

        assertThat(jwtProvider.validateAccessToken(token)).isTrue();
        assertThat(jwtProvider.getName(token)).isEqualTo("member@example.com");
    }

    @Test
    void rejectsMissingOrWeakSecret() {
        assertThatThrownBy(() -> new JwtProvider(""))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProvider("d2Vhaw=="))
                .isInstanceOf(IllegalStateException.class);
    }
}
