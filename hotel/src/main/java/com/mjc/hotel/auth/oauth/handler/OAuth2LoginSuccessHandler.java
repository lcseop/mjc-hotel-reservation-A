package com.mjc.hotel.auth.oauth.handler;

import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.oauth.principal.SocialPrincipal;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2FrontendRedirectService redirectService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String redirectUrl;

        try {
            if (!(authentication.getPrincipal() instanceof SocialPrincipal principal)) {
                throw new AuthenticationFailedException("소셜 로그인 정보가 없습니다.");
            }

            MemberLoginResponseDto loginResponse = authService.loginOAuth2(
                    principal.getMemberSid(),
                    principal.getProvider()
            );
            redirectUrl = redirectService.createSuccessRedirect(session, loginResponse);
            log.info(
                    "OAuth2 login succeeded: provider={}, memberSid={}",
                    principal.getProvider(),
                    principal.getMemberSid()
            );
        } catch (Exception exception) {
            log.error("OAuth2 application token issuance failed", exception);
            redirectUrl = redirectService.createFailureRedirect(
                    session,
                    "oauth2_token_issue_failed",
                    safeFailureMessage(exception)
            );
        }

        invalidateOAuthSession(session);
        response.sendRedirect(redirectUrl);
    }

    private String safeFailureMessage(Exception exception) {
        if (exception instanceof AuthenticationFailedException && exception.getMessage() != null) {
            return exception.getMessage();
        }
        return "소셜 로그인 토큰을 발급하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }

    private void invalidateOAuthSession(HttpSession session) {
        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // 이미 종료된 OAuth 세션이면 추가 작업이 필요하지 않습니다.
        }
        SecurityContextHolder.clearContext();
    }
}
