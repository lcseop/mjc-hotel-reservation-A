(function (window, $) {
    const SUPPORTED_PROVIDERS = ["google", "kakao", "naver"];

    $(function () {
        $(document).on("click", ".social-btn[data-provider]", startSocialLogin);
    });

    function startSocialLogin(event) {
        event.preventDefault();

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
            sessionStorage.setItem("afterLoginRedirect", "index.html");
        }

        sessionStorage.setItem("oauthProvider", provider);
        sessionStorage.setItem("oauthRemember", $("#remember").is(":checked") ? "true" : "false");

        const frontendCallbackUrl = new URL("oauth-callback.html", window.location.href).href;
        location.href = config.oauthAuthorizationUrl(provider, frontendCallbackUrl);
    }
})(window, window.jQuery);
