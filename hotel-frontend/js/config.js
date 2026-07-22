(function (window) {
    const localFrontend = ["localhost", "127.0.0.1"].includes(window.location.hostname)
        && window.location.port
        && !["80", "33000"].includes(window.location.port);
    const defaultApiBase = localFrontend ? "http://localhost:33000/api" : "/api";
    const API_BASE = window.STAYNOW_API_BASE || defaultApiBase;
    const ASSET_BASE = API_BASE.startsWith("http") ? API_BASE.replace(/\/api$/, "") : "";
    let refreshPromise = null;

    window.StayNowConfig = {
        apiBase: API_BASE,
        assetBase: ASSET_BASE,
        tossClientKey: "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm",
        oauthBase: API_BASE.replace(/\/api\/?$/, ""),
        apiUrl: function (path) {
            return API_BASE + "/" + String(path || "").replace(/^\/+/, "");
        },
        assetUrl: function (path) {
            return this.assetBase + "/" + String(path || "").replace(/^\/+/, "");
        },
        oauthAuthorizationUrl: function (provider, frontendCallbackUrl) {
            const normalizedProvider = String(provider || "").toLowerCase();
            if (frontendCallbackUrl) {
                return this.apiUrl("/auth/oauth2/" + encodeURIComponent(normalizedProvider) + "/start")
                    + "?redirectUri=" + encodeURIComponent(frontendCallbackUrl);
            }

            const base = this.oauthBase || "";
            return base + "/oauth2/authorization/" + encodeURIComponent(normalizedProvider);
        },
        getAuth: getStoredAuth,
        saveAuth: saveStoredAuth,
        clearAuth: clearStoredAuth,
        authHeaders: function () {
            const auth = getStoredAuth();
            return auth && auth.token ? { Authorization: auth.token } : {};
        },
        refreshAccessToken: refreshAccessToken,
        logout: logoutWithRefreshToken
    };

    function getAuthStorage() {
        return localStorage.getItem("staynowAuth") ? localStorage : sessionStorage;
    }

    function getStoredAuth() {
        const value = localStorage.getItem("staynowAuth") || sessionStorage.getItem("staynowAuth");
        if (!value) return null;
        try {
            return JSON.parse(value);
        } catch (e) {
            clearStoredAuth();
            return null;
        }
    }

    function saveStoredAuth(auth) {
        if (!auth) return;
        const storage = getAuthStorage();
        storage.setItem("staynowAuth", JSON.stringify(auth));
    }

    function clearStoredAuth() {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
    }

    function normalizeToken(tokenType, accessToken) {
        if (!accessToken) return "";
        return (tokenType || "Bearer") + " " + accessToken;
    }

    function isAuthEndpoint(url) {
        const target = String(url || "");
        return target.includes("/api/auth/login")
            || target.includes("/api/auth/signup")
            || target.includes("/api/member/password-reset")
            || target.includes("/api/auth/refresh")
            || target.includes("/api/auth/logout")
            || target.includes("/api/auth/oauth2/");
    }

    function refreshAccessToken() {
        const auth = getStoredAuth();
        if (!auth || !auth.refreshToken) {
            return $.Deferred().reject().promise();
        }

        if (refreshPromise) {
            return refreshPromise;
        }

        refreshPromise = $.ajax({
            url: API_BASE + "/auth/refresh",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({ refreshToken: auth.refreshToken })
        }).then(function (response) {
            const data = response && response.data ? response.data : response;
            const nextAuth = Object.assign({}, auth, {
                accessToken: data.accessToken,
                tokenType: data.tokenType || auth.tokenType || "Bearer",
                token: normalizeToken(data.tokenType || auth.tokenType || "Bearer", data.accessToken),
                expiresIn: data.expiresIn || auth.expiresIn,
                refreshedAt: new Date().toISOString()
            });
            saveStoredAuth(nextAuth);
            return nextAuth;
        }, function (xhr) {
            clearStoredAuth();
            return $.Deferred().reject(xhr).promise();
        }).always(function () {
            refreshPromise = null;
        });

        return refreshPromise;
    }

    function logoutWithRefreshToken() {
        const auth = getStoredAuth();
        clearStoredAuth();

        if (!auth || !auth.memberSid || !auth.refreshToken) {
            return $.Deferred().resolve().promise();
        }

        return $.ajax({
            url: API_BASE + "/auth/logout",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                memberSid: auth.memberSid,
                refreshToken: auth.refreshToken
            })
        }).then(null, function () {
            return $.Deferred().resolve().promise();
        });
    }

    if (window.jQuery) {
        $.ajaxPrefilter(function (options) {
            if (isAuthEndpoint(options.url)) return;
            if (!String(options.url || "").startsWith(API_BASE)) return;

            const auth = getStoredAuth();
            if (!auth || !auth.token) return;

            options.headers = options.headers || {};
            if (!options.headers.Authorization) {
                options.headers.Authorization = auth.token;
            }
        });

        const originalAjax = $.ajax;
        $.ajax = function () {
            const rawOptions = typeof arguments[0] === "string"
                ? Object.assign({}, arguments[1] || {}, { url: arguments[0] })
                : Object.assign({}, arguments[0] || {});

            const request = originalAjax.apply($, arguments);
            const shouldRetry = !rawOptions._stayNowRetry
                && !isAuthEndpoint(rawOptions.url)
                && String(rawOptions.url || "").startsWith(API_BASE);

            if (!shouldRetry) {
                return request;
            }

            const deferred = $.Deferred();

            request.done(function () {
                deferred.resolveWith(this, arguments);
            });

            request.fail(function (xhr) {
                const failContext = this;
                const failArgs = arguments;

                if (!xhr || xhr.status !== 401 || !getStoredAuth()?.refreshToken) {
                    deferred.rejectWith(failContext, failArgs);
                    return;
                }

                refreshAccessToken().then(function (auth) {
                    const retryOptions = Object.assign({}, rawOptions, {
                        _stayNowRetry: true,
                        headers: Object.assign({}, rawOptions.headers || {}, {
                            Authorization: auth.token
                        })
                    });

                    originalAjax.call($, retryOptions)
                        .done(function () {
                            deferred.resolveWith(this, arguments);
                        })
                        .fail(function () {
                            deferred.rejectWith(this, arguments);
                        });
                }, function () {
                    deferred.rejectWith(failContext, failArgs);
                });
            });

            return deferred.promise(request);
        };
    }
})(window);
