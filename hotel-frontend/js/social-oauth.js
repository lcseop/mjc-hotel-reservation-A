(function (window, $) {
    const SUPPORTED_PROVIDERS = ["google", "kakao", "naver"];
    const PROVIDER_NAMES = {
        google: "구글",
        kakao: "카카오",
        naver: "네이버"
    };

    $(function () {
        $(document).on("click", ".social-btn[data-provider]", startSocialLogin);
    });

    function startSocialLogin(event) {
        event.preventDefault();

        const $button = $(this);
        if ($button.data("oauthPending")) {
            return;
        }

        const provider = String($(this).data("provider") || "").toLowerCase();
        if (!SUPPORTED_PROVIDERS.includes(provider)) {
            alert("지원하지 않는 소셜 로그인입니다.");
            return;
        }

        const config = window.StayNowConfig;
        if (!config || typeof config.oauthAuthorizationUrl !== "function") {
            alert("소셜 로그인 설정을 불러오지 못했습니다.");
            return;
        }

        const currentRedirect = sessionStorage.getItem("afterLoginRedirect");
        if (!currentRedirect) {
            sessionStorage.setItem("afterLoginRedirect", resolveAfterLoginRedirect());
        }

        sessionStorage.setItem("oauthProvider", provider);
        sessionStorage.setItem("oauthRemember", $("#remember").is(":checked") ? "true" : "false");
        sessionStorage.setItem("oauthProviderName", PROVIDER_NAMES[provider] || "소셜");

        const frontendCallbackUrl = new URL("oauth-callback.html", window.location.href).href;
        $button.data("oauthPending", true).prop("disabled", true).attr("aria-busy", "true");
        location.href = config.oauthAuthorizationUrl(provider, frontendCallbackUrl);
    }

    function resolveAfterLoginRedirect() {
        const params = new URLSearchParams(window.location.search);
        const requested = params.get("redirect") || params.get("next");
        if (isSafeFrontendPath(requested)) {
            return requested;
        }

        return "index.html";
    }

    function isSafeFrontendPath(value) {
        if (!value || typeof value !== "string") {
            return false;
        }

        const trimmed = value.trim();
        return /^[A-Za-z0-9._/-]+(?:\?[A-Za-z0-9._~:/?#[\]@!$&'()*+,;=%-]*)?$/.test(trimmed)
            && !trimmed.startsWith("/")
            && !trimmed.includes("://")
            && !trimmed.startsWith("..");
    }
})(window, window.jQuery);
