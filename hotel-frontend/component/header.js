$(function () {
    renderAuthHeader();
    setTimeout(renderAuthHeader, 200);

    function closeMobileMenu() {
        $(".mobile-menu").removeClass("open");
        $(".hamburger")
            .attr("aria-expanded", "false")
            .attr("aria-label", "메뉴 열기")
            .find("i")
            .removeClass("fa-xmark")
            .addClass("fa-bars");
    }

    function closeUserMenu() {
        $(".user-menu, .mobile-user-panel").removeClass("open");
    }

    $(document).on("click", ".hamburger", function () {
        const $button = $(this);
        const isOpen = $button.attr("aria-expanded") === "true";

        $(".mobile-menu").toggleClass("open", !isOpen);
        $button.attr("aria-expanded", String(!isOpen));
        $button.attr("aria-label", isOpen ? "메뉴 열기" : "메뉴 닫기");
        $button.find("i").toggleClass("fa-bars fa-xmark");
    });

    $(document).on("click", ".mobile-nav a:not([aria-disabled='true'])", function () {
        closeMobileMenu();
    });

    $(window).on("resize", function () {
        if (window.innerWidth > 860) {
            closeMobileMenu();
        }
    });

    $(document).on("click", ".auth-user-btn", function (e) {
        e.stopPropagation();

        const auth = getAuthData();
        if (!auth || !auth.token) {
            location.href = "login.html";
            return;
        }

        const panel = $(this).closest(".user-menu, .mobile-user-panel");
        const isOpen = panel.hasClass("open");

        closeUserMenu();
        panel.toggleClass("open", !isOpen);
    });

    $(document).on("click", ".user-dropdown", function (e) {
        e.stopPropagation();
    });

    $(document).on("click", ".logout-btn", function () {
        const logoutRequest = window.StayNowConfig && window.StayNowConfig.logout
            ? window.StayNowConfig.logout()
            : $.Deferred().resolve().promise();

        closeUserMenu();
        renderAuthHeader();

        logoutRequest.always(function () {
            location.href = "index.html";
        });
    });

    $(document).on("click", function () {
        closeUserMenu();
    });

    setTimeout(renderAuthHeader, 500);
    setTimeout(renderAuthHeader, 1000);
});

function getAuthData() {
    const stored = localStorage.getItem("staynowAuth") || sessionStorage.getItem("staynowAuth");

    if (!stored) {
        return null;
    }

    try {
        return JSON.parse(stored);
    } catch (e) {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
        return null;
    }
}

function renderAuthHeader() {
    const auth = getAuthData();
    const isLoggedIn = auth && auth.token;

    if ($(".auth-user-btn").length === 0) {
        return;
    }

    if (isLoggedIn) {
        $(".auth-user-name").text((auth.name || auth.email || "회원") + "님");
        $(".auth-user-email").text(auth.email || "");
        $(".user-dropdown").addClass("is-logged-in");
        $(".user-dropdown").toggleClass("is-admin", auth.role === "ADMIN");
        $(".wishlist-link").prop("hidden", false);
        $(".auth-chevron").prop("hidden", false);
    } else {
        $(".auth-user-name").text("로그인");
        $(".auth-user-email").text("로그인이 필요합니다");
        $(".user-dropdown").removeClass("is-logged-in is-admin");
        $(".wishlist-link").prop("hidden", true);
        $(".auth-chevron").prop("hidden", true);
    }
}
