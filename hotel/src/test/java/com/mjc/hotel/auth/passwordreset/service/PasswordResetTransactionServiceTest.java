package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordResetTransactionServiceTest {

    private final MemberAuthAccountRepository authAccountRepository =
            mock(MemberAuthAccountRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PasswordResetTransactionService service =
            new PasswordResetTransactionService(authAccountRepository, passwordEncoder);

    @Test
    void replacesLocalPasswordHashAndReturnsMemberSid() {
        Member member = Member.builder().sid(7L).build();
        MemberAuthAccount authAccount = MemberAuthAccount.builder()
                .sid(11L)
                .member(member)
                .passwordHash("old-hash")
                .build();
        when(authAccountRepository.findActiveLocalBySid(11L))
                .thenReturn(Optional.of(authAccount));
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        Long memberSid = service.resetPassword(11L, "new-password");

        assertThat(memberSid).isEqualTo(7L);
        assertThat(authAccount.getPasswordHash()).isEqualTo("new-hash");
    }
}
