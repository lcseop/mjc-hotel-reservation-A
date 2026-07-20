$(function () {

    init();

});

const LOGIN_API = window.StayNowConfig.apiUrl("/auth/login");

function init() {

    bindEvent();
    checkInput();

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

    requestLogin({
        email,
        password
    });

}

function requestLogin(payload) {

    setLoginLoading(true);

    $.ajax({
        url: LOGIN_API,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload),

        success: function (result) {

            const loginData = result.data;

            if (!loginData || !loginData.accessToken) {
                alert("로그인 응답이 올바르지 않습니다.");
                return;
            }

            saveLoginData(loginData);
            const redirectUrl = sessionStorage.getItem("afterLoginRedirect");
            sessionStorage.removeItem("afterLoginRedirect");
            location.href = redirectUrl || "index.html";

        },

        error: function (xhr) {

            const message =
                xhr.responseJSON &&
                xhr.responseJSON.data
                    ? xhr.responseJSON.data
                    : "이메일 또는 비밀번호를 확인해주세요.";

            alert(message);
            $("#password").focus();

        },

        complete: function () {

            setLoginLoading(false);
            checkInput();

        }
    });

}

function saveLoginData(loginData) {

    const storage = $("#remember").is(":checked") ? localStorage : sessionStorage;
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
        loggedInAt: new Date().toISOString()
    }));

}

function setLoginLoading(loading) {

    $("#loginBtn")
        .prop("disabled", loading)
        .text(loading ? "로그인 중..." : "로그인");

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
