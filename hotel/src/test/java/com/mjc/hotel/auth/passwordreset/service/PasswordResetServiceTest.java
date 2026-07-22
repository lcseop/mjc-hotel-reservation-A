package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.auth.passwordreset.dto.PasswordResetConfirmRequest;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetAccountNotFoundException;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import com.mjc.hotel.auth.service.RefreshTokenService;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private MemberAuthAccountRepository authAccountRepository;
    @Mock
    private PasswordResetTokenService tokenService;
    @Mock
    private PasswordResetMailService mailService;
    @Mock
    private PasswordResetTransactionService transactionService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                authAccountRepository,
                tokenService,
                mailService,
                transactionService,
                refreshTokenService
        );
    }

    @Test
    void unknownEmailReturnsAccountNotFoundAsRequiredByApiContract() {
        when(authAccountRepository.findAllActiveLocalByEmail("unknown@example.com"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.requestVerificationCode("Unknown@Example.com"))
                .isInstanceOf(PasswordResetAccountNotFoundException.class);

        verify(tokenService, never()).issueVerificationCode("unknown@example.com");
        verify(mailService, never()).sendVerificationCode(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void sendsDedicatedResetCodeForTheOnlyActiveLocalAccount() {
        MemberAuthAccount account = MemberAuthAccount.builder().sid(11L).build();
        when(authAccountRepository.findAllActiveLocalByEmail("member@example.com"))
                .thenReturn(List.of(account));
        when(tokenService.issueVerificationCode("member@example.com")).thenReturn("123456");

        service.requestVerificationCode("Member@Example.com");

        verify(mailService).sendVerificationCode("member@example.com", "123456");
    }

    @Test
    void verifiedCodeCreatesOneTimeVerificationBoundToTheAccount() {
        MemberAuthAccount account = MemberAuthAccount.builder().sid(11L).build();
        when(authAccountRepository.findAllActiveLocalByEmail("member@example.com"))
                .thenReturn(List.of(account));
        service.verifyCode(
                "member@example.com",
                "123456"
        );

        verify(tokenService).verifyCode("member@example.com", "123456", 11L);
    }

    @Test
    void consumesTokenChangesPasswordAndRevokesRefreshToken() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "member@example.com",
                "123456",
                "new-password"
        );
        MemberAuthAccount account = MemberAuthAccount.builder().sid(11L).build();
        when(authAccountRepository.findAllActiveLocalByEmail("member@example.com"))
                .thenReturn(List.of(account));
        when(tokenService.consumeVerifiedCode("member@example.com", "123456"))
                .thenReturn(11L);
        when(transactionService.resetPassword(11L, "new-password")).thenReturn(7L);

        service.resetPassword(request);

        verify(refreshTokenService).delete(7L);
    }

    @Test
    void rejectsShortPasswordBeforeConsumingVerifiedCode() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "member@example.com",
                "123456",
                "short"
        );

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(PasswordResetException.class)
                .hasMessageContaining("8자 이상");
        verify(tokenService, never()).consumeVerifiedCode(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
