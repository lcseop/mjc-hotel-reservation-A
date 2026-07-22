package com.mjc.hotel.auth.oauth.handler;

import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class OAuth2FrontendRedirectService {

    private static final String CALLBACK_SESSION_ATTRIBUTE =
            OAuth2FrontendRedirectService.class.getName() + ".CALLBACK_URI";

    private final URI defaultCallbackUri;

    public OAuth2FrontendRedirectService(
            @Value("${staynow.oauth2.frontend-callback-url:http://localhost/oauth-callback.html}")
            String defaultCallbackUrl
    ) {
        this.defaultCallbackUri = requireCallbackUri(defaultCallbackUrl);
    }

    public boolean rememberRequestedCallback(
            String requestedCallbackUrl,
            HttpServletRequest request,
            HttpSession session
    ) {
        URI callbackUri = parseCallbackUri(requestedCallbackUrl);
        if (callbackUri == null || !isAllowedCallback(callbackUri, request.getServerName())) {
            return false;
        }

        session.setAttribute(CALLBACK_SESSION_ATTRIBUTE, callbackUri.toASCIIString());
        return true;
    }

    public String createSuccessRedirect(HttpSession session, MemberLoginResponseDto loginResponse) {
        URI callbackUri = consumeCallbackUri(session);
        String fragment = String.join("&",
                parameter("accessToken", loginResponse.getAccessToken()),
                parameter("refreshToken", loginResponse.getRefreshToken()),
                parameter("tokenType", loginResponse.getTokenType()),
                parameter("expiresIn", loginResponse.getExpiresIn()),
                parameter("refreshTokenExpiresIn", loginResponse.getRefreshTokenExpiresIn()),
                parameter("memberSid", loginResponse.getMemberSid()),
                parameter("email", loginResponse.getEmail()),
                parameter("name", loginResponse.getName()),
                parameter("role", loginResponse.getRole()),
                parameter("point", loginResponse.getPoint() == null ? 0 : loginResponse.getPoint()),
                parameter("provider", loginResponse.getProvider())
        );
        return callbackUri.toASCIIString() + "#" + fragment;
    }

    public String createFailureRedirect(HttpSession session, String message) {
        return createFailureRedirect(session, "oauth2_login_failed", message);
    }

    public String createFailureRedirect(HttpSession session, String errorCode, String message) {
        URI callbackUri = consumeCallbackUri(session);
        return callbackUri.toASCIIString()
                + "#"
                + parameter("error", errorCode)
                + "&"
                + parameter("message", message);
    }

    private URI consumeCallbackUri(HttpSession session) {
        Object callback = session.getAttribute(CALLBACK_SESSION_ATTRIBUTE);
        session.removeAttribute(CALLBACK_SESSION_ATTRIBUTE);

        URI callbackUri = parseCallbackUri(callback instanceof String value ? value : null);
        return callbackUri != null ? callbackUri : defaultCallbackUri;
    }

    private URI requireCallbackUri(String callbackUrl) {
        URI callbackUri = parseCallbackUri(callbackUrl);
        if (callbackUri == null || !isStructurallyValid(callbackUri)) {
            throw new IllegalArgumentException("올바른 OAuth2 프론트 콜백 URL이 필요합니다.");
        }
        return callbackUri;
    }

    private URI parseCallbackUri(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            return null;
        }

        try {
            return URI.create(callbackUrl.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isAllowedCallback(URI callbackUri, String requestHost) {
        if (!isStructurallyValid(callbackUri)) {
            return false;
        }

        String callbackHost = normalizeHost(callbackUri.getHost());
        String normalizedRequestHost = normalizeHost(requestHost);
        return callbackHost.equals(normalizedRequestHost)
                || (isLoopbackHost(callbackHost) && isLoopbackHost(normalizedRequestHost));
    }

    private boolean isStructurallyValid(URI callbackUri) {
        String scheme = callbackUri.getScheme();
        String path = callbackUri.getPath();
        return callbackUri.isAbsolute()
                && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                && callbackUri.getHost() != null
                && callbackUri.getUserInfo() == null
                && callbackUri.getQuery() == null
                && callbackUri.getFragment() == null
                && path != null
                && path.endsWith("/oauth-callback.html");
    }

    private String normalizeHost(String host) {
        return host == null ? "" : host.toLowerCase(Locale.ROOT);
    }

    private boolean isLoopbackHost(String host) {
        return "localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host);
    }

    private String parameter(String name, Object value) {
        return encode(name) + "=" + encode(value == null ? "" : value.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
