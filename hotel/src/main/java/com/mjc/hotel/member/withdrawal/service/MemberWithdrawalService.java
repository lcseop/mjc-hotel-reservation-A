package com.mjc.hotel.member.withdrawal.service;

import com.mjc.hotel.auth.oauth.service.SocialProviderTokenService;
import com.mjc.hotel.auth.service.RefreshTokenService;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.withdrawal.dto.MemberWithdrawalRequest;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import com.mjc.hotel.member.withdrawal.provider.SocialUnlinkClientRegistry;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberWithdrawalService {

    private final MemberWithdrawalTransactionService transactionService;
    private final SocialProviderTokenService socialProviderTokenService;
    private final SocialUnlinkClientRegistry socialUnlinkClientRegistry;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public void withdraw(String authenticatedEmail, MemberWithdrawalRequest request) {
        validateRequest(request);
        MemberWithdrawalCandidate candidate = transactionService.loadCandidate(authenticatedEmail);
        validateLocalPassword(candidate, request.getPassword());

        Map<MemberAuthProvider, String> socialTokens = loadRequiredSocialTokens(candidate);
        socialTokens.forEach((provider, token) ->
                socialUnlinkClientRegistry.get(provider).unlink(token)
        );

        transactionService.markWithdrawn(candidate.memberSid());
        deleteRedisTokens(candidate);
    }

    private void validateRequest(MemberWithdrawalRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getConfirmed())) {
            throw new WithdrawalConflictException("회원 탈퇴 확인이 필요합니다.");
        }
    }

    private void validateLocalPassword(MemberWithdrawalCandidate candidate, String password) {
        if (!candidate.localAccount()) {
            return;
        }
        if (candidate.localPasswordHash() == null
                || password == null || password.isBlank()
                || !passwordEncoder.matches(password, candidate.localPasswordHash())) {
            throw new AuthenticationFailedException("현재 비밀번호가 올바르지 않습니다.");
        }
    }

    private Map<MemberAuthProvider, String> loadRequiredSocialTokens(
            MemberWithdrawalCandidate candidate
    ) {
        EnumMap<MemberAuthProvider, String> tokens = new EnumMap<>(MemberAuthProvider.class);
        for (MemberAuthProvider provider : candidate.socialProviders()) {
            String token = socialProviderTokenService.get(candidate.memberSid(), provider);
            if (token == null || token.isBlank()) {
                throw new WithdrawalConflictException(
                        providerName(provider)
                                + " 연결 해제를 위해 해당 소셜 계정으로 다시 로그인한 후 10분 안에 탈퇴해 주세요."
                );
            }
            tokens.put(provider, token);
        }
        return tokens;
    }

    private void deleteRedisTokens(MemberWithdrawalCandidate candidate) {
        try {
            refreshTokenService.delete(candidate.memberSid());
            candidate.socialProviders().forEach(provider ->
                    socialProviderTokenService.delete(candidate.memberSid(), provider)
            );
        } catch (DataAccessException exception) {
            log.error("탈퇴 회원 Redis 토큰 정리에 실패했습니다: memberSid={}", candidate.memberSid(), exception);
        }
    }

    private String providerName(MemberAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "구글";
            case KAKAO -> "카카오";
            case NAVER -> "네이버";
            case LOCAL -> "로컬";
        };
    }
}
