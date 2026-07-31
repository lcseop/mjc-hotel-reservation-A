package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetTransactionService {

    private final MemberAuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long resetPassword(Long authAccountSid, String newPassword) {
        MemberAuthAccount authAccount = authAccountRepository.findActiveLocalBySid(authAccountSid)
                .orElseThrow(() -> new PasswordResetException(
                        "비밀번호를 재설정할 수 없는 계정입니다."
                ));

        if (authAccount.getPasswordHash() != null
                && passwordEncoder.matches(newPassword, authAccount.getPasswordHash())) {
            throw new PasswordResetException("기존 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }

        authAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        return authAccount.getMember().getSid();
    }
}
