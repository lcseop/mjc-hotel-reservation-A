const PASSWORD_RESET_API = window.StayNowConfig.apiUrl("/member/password-reset");

let passwordResetEmail = "";
let verifiedPasswordResetCode = "";
let resendTimer = null;

$(function () {
    $("#sendResetCodeBtn").on("click", requestPasswordResetCode);
    $("#resendResetCodeBtn").on("click", requestPasswordResetCode);
    $("#verifyResetCodeBtn").on("click", verifyPasswordResetCode);
    $("#confirmPasswordResetBtn").on("click", confirmPasswordReset);
    $("#resetCode").on("input", function () {
        $(this).val(String($(this).val() || "").replace(/\D/g, "").slice(0, 6));
    });
});

function requestPasswordResetCode() {
    const email = String($("#resetEmail").val() || passwordResetEmail).trim().toLowerCase();
    if (!isResetEmail(email)) {
        alert("올바른 이메일을 입력해 주세요.");
        $("#resetEmail").focus();
        return;
    }

    setResetLoading("send", true);
    $.ajax({
        url: PASSWORD_RESET_API + "/send-code",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email: email })
    }).done(function () {
        passwordResetEmail = email;
        $("#resetEmail").val(email);
        $("#resetCodeGuide").text(
            email + "로 인증번호를 발송했습니다. 가입된 LOCAL 계정인 경우에만 메일이 도착합니다."
        );
        showResetStep(2);
        $("#resetCode").val("").focus();
        startResendCooldown();
    }).fail(function (xhr) {
        alert(resetErrorMessage(xhr, "인증번호 요청에 실패했습니다."));
    }).always(function () {
        setResetLoading("send", false);
    });
}

function verifyPasswordResetCode() {
    const code = String($("#resetCode").val() || "").trim();
    if (!passwordResetEmail) {
        showResetStep(1);
        return;
    }
    if (!/^\d{6}$/.test(code)) {
        alert("인증번호 6자리를 입력해 주세요.");
        $("#resetCode").focus();
        return;
    }

    setResetLoading("verify", true);
    $.ajax({
        url: PASSWORD_RESET_API + "/verify-code",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email: passwordResetEmail, code: code })
    }).done(function () {
        verifiedPasswordResetCode = code;
        showResetStep(3);
        $("#newPassword").focus();
    }).fail(function (xhr) {
        alert(resetErrorMessage(xhr, "인증번호가 올바르지 않거나 만료되었습니다."));
    }).always(function () {
        setResetLoading("verify", false);
    });
}

function confirmPasswordReset() {
    const password = String($("#newPassword").val() || "");
    const passwordConfirm = String($("#newPasswordConfirm").val() || "");
    if (!verifiedPasswordResetCode) {
        alert("이메일 인증을 다시 진행해 주세요.");
        showResetStep(1);
        return;
    }
    if (password.length < 8) {
        alert("비밀번호는 8자 이상 입력해 주세요.");
        $("#newPassword").focus();
        return;
    }
    if (password !== passwordConfirm) {
        alert("비밀번호 확인이 일치하지 않습니다.");
        $("#newPasswordConfirm").focus();
        return;
    }

    setResetLoading("confirm", true);
    $.ajax({
        url: PASSWORD_RESET_API,
        type: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({
            email: passwordResetEmail,
            code: verifiedPasswordResetCode,
            newPassword: password
        })
    }).done(function () {
        verifiedPasswordResetCode = "";
        alert("비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.");
        location.replace("login.html");
    }).fail(function (xhr) {
        alert(resetErrorMessage(xhr, "비밀번호 변경에 실패했습니다."));
    }).always(function () {
        setResetLoading("confirm", false);
    });
}

function showResetStep(step) {
    $(".reset-step").prop("hidden", true);
    $("#resetStep" + step).prop("hidden", false);
    $(".reset-progress li").each(function () {
        const progress = Number($(this).data("progress"));
        $(this).toggleClass("active", progress === step);
        $(this).toggleClass("complete", progress < step);
    });
}

function startResendCooldown() {
    let remaining = 60;
    const button = $("#resendResetCodeBtn");
    button.prop("disabled", true).text("다시 받기 (" + remaining + "초)");
    if (resendTimer) clearInterval(resendTimer);
    resendTimer = setInterval(function () {
        remaining -= 1;
        if (remaining <= 0) {
            clearInterval(resendTimer);
            resendTimer = null;
            button.prop("disabled", false).text("인증번호 다시 받기");
            return;
        }
        button.text("다시 받기 (" + remaining + "초)");
    }, 1000);
}

function setResetLoading(type, loading) {
    const settings = {
        send: ["#sendResetCodeBtn", "발송 중...", "인증번호 발송"],
        verify: ["#verifyResetCodeBtn", "확인 중...", "인증번호 확인"],
        confirm: ["#confirmPasswordResetBtn", "변경 중...", "비밀번호 변경"]
    };
    const setting = settings[type];
    $(setting[0]).prop("disabled", loading).text(loading ? setting[1] : setting[2]);
}

function isResetEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function resetErrorMessage(xhr, fallback) {
    const response = xhr && xhr.responseJSON;
    return response && (response.data || response.message)
        ? (response.data || response.message)
        : fallback;
}
