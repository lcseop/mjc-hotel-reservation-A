$(function () {
    handleOAuthCallback();
});

function handleOAuthCallback() {
    const params = new URLSearchParams(location.search);
    const hashParams = new URLSearchParams(String(location.hash || "").replace(/^#/, ""));
    const getValue = function (name) {
        return params.get(name) || hashParams.get(name);
    };

    if (location.hash) {
        history.replaceState(null, document.title, location.pathname + location.search);
    }

    const error = getValue("error") || getValue("code");
    if (error && !getValue("accessToken")) {
        showOAuthError(getValue("message") || getValue("error_description") || "소셜 로그인에 실패했습니다.");
        return;
    }

    const loginData = {
        accessToken: getValue("accessToken"),
        refreshToken: getValue("refreshToken"),
        tokenType: getValue("tokenType") || "Bearer",
        expiresIn: Number(getValue("expiresIn") || 0) || null,
        refreshTokenExpiresIn: Number(getValue("refreshTokenExpiresIn") || 0) || null,
        memberSid: Number(getValue("memberSid") || 0) || null,
        email: getValue("email") || "",
        name: getValue("name") || "",
        role: getValue("role") || "USER",
        point: Number(getValue("point") || 0),
        provider: getValue("provider") || sessionStorage.getItem("oauthProvider") || ""
    };

    if (!loginData.accessToken || !loginData.memberSid) {
        showOAuthError("소셜 로그인 응답에 토큰 또는 회원 정보가 없습니다. 백엔드 OAuth 성공 응답을 확인해주세요.");
        return;
    }

    saveOAuthLoginData(loginData);

    const redirectUrl = sessionStorage.getItem("afterLoginRedirect") || "index.html";
    sessionStorage.removeItem("afterLoginRedirect");
    sessionStorage.removeItem("oauthProvider");
    sessionStorage.removeItem("oauthRemember");

    location.replace(redirectUrl);
}

function saveOAuthLoginData(loginData) {
    const remember = sessionStorage.getItem("oauthRemember") === "true";
    const storage = remember ? localStorage : sessionStorage;
    const token = loginData.tokenType + " " + loginData.accessToken;

    localStorage.removeItem("staynowAuth");
    sessionStorage.removeItem("staynowAuth");

    storage.setItem("staynowAuth", JSON.stringify({
        token,
        accessToken: loginData.accessToken,
        refreshToken: loginData.refreshToken,
        tokenType: loginData.tokenType,
        expiresIn: loginData.expiresIn,
        refreshTokenExpiresIn: loginData.refreshTokenExpiresIn,
        memberSid: loginData.memberSid,
        email: loginData.email,
        name: loginData.name,
        role: loginData.role,
        point: Number(loginData.point || 0),
        provider: loginData.provider,
        loggedInAt: new Date().toISOString()
    }));
}

function showOAuthError(message) {
    $("#oauthTitle").text("소셜 로그인을 완료하지 못했어요");
    $("#oauthMessage").html(
        escapeHtml(message) +
        '<br><br><a class="oauth-callback-link" href="login.html">로그인 화면으로 돌아가기</a>'
    );
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
