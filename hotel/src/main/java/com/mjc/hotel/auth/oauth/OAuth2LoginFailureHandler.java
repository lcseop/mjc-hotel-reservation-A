package com.mjc.hotel.auth.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2FrontendRedirectService redirectService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String errorCode = resolveErrorCode(exception);
        String message = resolveMessage(exception, errorCode);

        log.warn("Google OAuth2 login failed: code={}, message={}", errorCode, exception.getMessage());
        String redirectUrl = redirectService.createFailureRedirect(session, errorCode, message);

        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // 이미 종료된 OAuth 세션이면 추가 작업이 필요하지 않습니다.
        }
        response.sendRedirect(redirectUrl);
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception
                && oauth2Exception.getError() != null
                && oauth2Exception.getError().getErrorCode() != null) {
            return oauth2Exception.getError().getErrorCode();
        }
        return "oauth2_login_failed";
    }

    private String resolveMessage(AuthenticationException exception, String errorCode) {
        if ("access_denied".equals(errorCode)) {
            return "구글 로그인 동의가 취소되었거나 이 계정에 앱 접근 권한이 없습니다.";
        }
        if ("authorization_request_not_found".equals(errorCode)
                || "invalid_state_parameter".equals(errorCode)) {
            return "구글 로그인 세션이 만료되었습니다. 로그인 화면에서 다시 시도해 주세요.";
        }

        String message = exception.getMessage();
        if (message != null && !message.isBlank()) {
            String prefix = "[" + errorCode + "] ";
            return message.startsWith(prefix) ? message.substring(prefix.length()) : message;
        }
        return "구글 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.";
    }
}
