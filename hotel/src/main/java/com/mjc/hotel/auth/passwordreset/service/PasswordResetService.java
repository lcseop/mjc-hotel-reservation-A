package com.mjc.hotel.auth.passwordreset.service;

import com.mjc.hotel.auth.passwordreset.dto.PasswordResetConfirmRequest;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetAccountNotFoundException;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import com.mjc.hotel.auth.service.RefreshTokenService;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final MemberAuthAccountRepository authAccountRepository;
    private final PasswordResetTokenService tokenService;
    private final PasswordResetMailService mailService;
    private final PasswordResetTransactionService transactionService;
    private final RefreshTokenService refreshTokenService;

    public void requestVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        List<MemberAuthAccount> accounts =
                authAccountRepository.findAllActiveLocalByEmail(normalizedEmail);

        if (accounts.size() != 1) {
            throw new PasswordResetAccountNotFoundException();
        }

        String code = tokenService.issueVerificationCode(normalizedEmail);
        if (code == null) {
            return;
        }

        try {
            mailService.sendVerificationCode(normalizedEmail, code);
        } catch (MailException exception) {
            tokenService.clearVerification(normalizedEmail);
            log.error("비밀번호 재설정 이메일 발송에 실패했습니다: email={}", normalizedEmail, exception);
            throw new PasswordResetException("인증번호 메일을 발송하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    public void verifyCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        List<MemberAuthAccount> accounts =
                authAccountRepository.findAllActiveLocalByEmail(normalizedEmail);
        if (accounts.size() != 1) {
            throw new PasswordResetAccountNotFoundException();
        }

        tokenService.verifyCode(
                normalizedEmail,
                code,
                accounts.getFirst().getSid()
        );
    }

    public void resetPassword(PasswordResetConfirmRequest request) {
        validateNewPassword(request);
        String normalizedEmail = normalizeEmail(request.getEmail());
        List<MemberAuthAccount> accounts =
                authAccountRepository.findAllActiveLocalByEmail(normalizedEmail);
        if (accounts.size() != 1) {
            throw new PasswordResetAccountNotFoundException();
        }
        Long authAccountSid = tokenService.consumeVerifiedCode(
                normalizedEmail,
                request.getCode()
        );
        if (!accounts.getFirst().getSid().equals(authAccountSid)) {
            throw new PasswordResetException("인증번호가 해당 계정과 일치하지 않습니다.");
        }
        Long memberSid = transactionService.resetPassword(
                authAccountSid,
                request.getNewPassword()
        );

        try {
            refreshTokenService.delete(memberSid);
        } catch (DataAccessException exception) {
            log.error("비밀번호 재설정 후 refresh token 삭제에 실패했습니다: memberSid={}", memberSid, exception);
        }
    }

    private void validateNewPassword(PasswordResetConfirmRequest request) {
        if (request == null) {
            throw new PasswordResetException("비밀번호 재설정 요청이 올바르지 않습니다.");
        }
        String password = request.getNewPassword();
        if (password == null || password.length() < 8) {
            throw new PasswordResetException("비밀번호는 8자 이상 입력해 주세요.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new PasswordResetException("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new PasswordResetException("인증번호가 필요합니다.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new PasswordResetException("올바른 이메일을 입력해 주세요.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
