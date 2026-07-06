$(function () {
    function closeMobileMenu() {
        $(".mobile-menu").removeClass("open");
        $(".hamburger")
            .attr("aria-expanded", "false")
            .attr("aria-label", "메뉴 열기")
            .find("i")
            .removeClass("fa-xmark")
            .addClass("fa-bars");
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
});
