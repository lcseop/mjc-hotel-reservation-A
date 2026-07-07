$(function () {

    init();

});

function init() {

    bindEvent();

}

/* ==========================
    Event
========================== */

function bindEvent() {

    $("#loginForm").submit(login);

    $("#togglePassword").click(togglePassword);

    $("#email, #password").on("input", checkInput);

}

/* ==========================
    로그인
========================== */

function login(e) {

    e.preventDefault();

    const email = $("#email").val().trim();
    const password = $("#password").val();

    if (email === "") {
        alert("이메일을 입력해주세요.");
        $("#email").focus();
        return;
    }

    if (!isEmail(email)) {
        alert("올바른 이메일 형식이 아닙니다.");
        $("#email").focus();
        return;
    }

    if (password === "") {
        alert("비밀번호를 입력해주세요.");
        $("#password").focus();
        return;
    }

    // ===========================
    // TODO
    // Spring Boot JWT 로그인
    // ===========================

    console.log({
        email,
        password,
        remember: $("#remember").is(":checked")
    });

}

/* ==========================
    비밀번호 보기
========================== */

function togglePassword() {

    const input = $("#password");

    if (input.attr("type") === "password") {

        input.attr("type", "text");

        $("#togglePassword").text("🙈");

    } else {

        input.attr("type", "password");

        $("#togglePassword").text("👁");

    }

}

/* ==========================
    이메일 검사
========================== */

function isEmail(email) {

    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    return regex.test(email);

}

/* ==========================
    로그인 버튼 활성화
========================== */

function checkInput() {

    const email = $("#email").val().trim();

    const password = $("#password").val();

    if (email !== "" && password !== "") {

        $(".login-btn")
            .prop("disabled", false)
            .css({
                opacity: "1",
                cursor: "pointer"
            });

    } else {

        $(".login-btn")
            .prop("disabled", true)
            .css({
                opacity: ".5",
                cursor: "not-allowed"
            });

    }

}