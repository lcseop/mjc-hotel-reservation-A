package com.mjc.hotel.member.withdrawal.service;

import com.mjc.hotel.auth.oauth.service.SocialProviderTokenService;
import com.mjc.hotel.auth.service.RefreshTokenService;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.withdrawal.dto.MemberWithdrawalRequest;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import com.mjc.hotel.member.withdrawal.provider.SocialUnlinkClient;
import com.mjc.hotel.member.withdrawal.provider.SocialUnlinkClientRegistry;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceTest {

    @Mock
    private MemberWithdrawalTransactionService transactionService;
    @Mock
    private SocialProviderTokenService socialProviderTokenService;
    @Mock
    private SocialUnlinkClientRegistry socialUnlinkClientRegistry;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SocialUnlinkClient socialUnlinkClient;

    private MemberWithdrawalService service;

    @BeforeEach
    void setUp() {
        service = new MemberWithdrawalService(
                transactionService,
                socialProviderTokenService,
                socialUnlinkClientRegistry,
                refreshTokenService,
                passwordEncoder
        );
    }

    @Test
    void withdrawsLocalMemberAndDeletesRefreshToken() {
        MemberWithdrawalCandidate candidate = new MemberWithdrawalCandidate(
                1L,
                true,
                "encoded-password",
                List.of()
        );
        when(transactionService.loadCandidate("member@example.com")).thenReturn(candidate);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        service.withdraw(
                "member@example.com",
                new MemberWithdrawalRequest("password", true)
        );

        verify(transactionService).markWithdrawn(1L);
        verify(refreshTokenService).delete(1L);
    }

    @Test
    void rejectsWrongLocalPassword() {
        MemberWithdrawalCandidate candidate = new MemberWithdrawalCandidate(
                1L,
                true,
                "encoded-password",
                List.of()
        );
        when(transactionService.loadCandidate("member@example.com")).thenReturn(candidate);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> service.withdraw(
                "member@example.com",
                new MemberWithdrawalRequest("wrong", true)
        )).isInstanceOf(AuthenticationFailedException.class);

        verify(transactionService, never()).markWithdrawn(1L);
    }

    @Test
    void unlinksNaverBeforeWithdrawingSocialMember() {
        MemberWithdrawalCandidate candidate = new MemberWithdrawalCandidate(
                2L,
                false,
                null,
                List.of(MemberAuthProvider.NAVER)
        );
        when(transactionService.loadCandidate("naver@example.com")).thenReturn(candidate);
        when(socialProviderTokenService.get(2L, MemberAuthProvider.NAVER))
                .thenReturn("naver-access-token");
        when(socialUnlinkClientRegistry.get(MemberAuthProvider.NAVER))
                .thenReturn(socialUnlinkClient);

        service.withdraw(
                "naver@example.com",
                new MemberWithdrawalRequest(null, true)
        );

        verify(socialUnlinkClient).unlink("naver-access-token");
        verify(transactionService).markWithdrawn(2L);
        verify(socialProviderTokenService).delete(2L, MemberAuthProvider.NAVER);
    }

    @Test
    void requiresRecentSocialLoginToken() {
        MemberWithdrawalCandidate candidate = new MemberWithdrawalCandidate(
                2L,
                false,
                null,
                List.of(MemberAuthProvider.NAVER)
        );
        when(transactionService.loadCandidate("naver@example.com")).thenReturn(candidate);

        assertThatThrownBy(() -> service.withdraw(
                "naver@example.com",
                new MemberWithdrawalRequest(null, true)
        )).isInstanceOf(WithdrawalConflictException.class)
                .hasMessageContaining("다시 로그인");

        verify(transactionService, never()).markWithdrawn(2L);
    }
}
