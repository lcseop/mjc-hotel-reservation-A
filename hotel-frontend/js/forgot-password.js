$(function () {
    initForgotPassword();
});

const PASSWORD_RESET_SEND_API = window.StayNowConfig.apiUrl("/auth/password-reset/send");
const PASSWORD_RESET_CONFIRM_API = window.StayNowConfig.apiUrl("/auth/password-reset/confirm");
const PASSWORD_RESET_API = window.StayNowConfig.apiUrl("/auth/password-reset");

let verifiedResetEmail = "";
let verifiedResetCode = "";
let resetPreviewMode = false;

function initForgotPassword() {
    bindForgotPasswordEvents();
    updateResetButtonState();
}

function bindForgotPasswordEvents() {
    $("#sendResetCodeBtn").on("click", sendPasswordResetCode);
    $("#confirmResetCodeBtn").on("click", confirmPasswordResetCode);
    $("#forgotPasswordForm").on("submit", resetPassword);
    $("#resetEmail").on("input", resetVerificationState);
    $("#resetCode").on("input", function () {
        $(this).val($(this).val().replace(/[^0-9]/g, "").slice(0, 6));
    });
    $("#newPassword, #newPasswordConfirm").on("input", function () {
        updatePasswordState();
        updateResetButtonState();
    });
    $(".toggle-password").on("click", togglePasswordVisibility);
}

function sendPasswordResetCode() {
    const email = getResetEmail();

    if (!isEmail(email)) {
        showFieldMessage("#emailGuide", "올바른 이메일을 입력해주세요.", "error");
        $("#resetEmail").focus();
        return;
    }

    setButtonLoading("#sendResetCodeBtn", true, "발송 중...");

    $.ajax({
        url: PASSWORD_RESET_SEND_API,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email }),
        success: function () {
            openVerificationPanel("인증번호를 발송했습니다. 메일함을 확인해주세요.");
        },
        error: function (xhr) {
            if (canUsePreviewMode(xhr)) {
                resetPreviewMode = true;
                openVerificationPanel("백엔드 연결 전 미리보기 모드입니다. 인증번호 123456을 입력해주세요.");
                return;
            }
            showFieldMessage("#emailGuide", getErrorMessage(xhr, "인증번호 발송에 실패했습니다."), "error");
        },
        complete: function () {
            setButtonLoading("#sendResetCodeBtn", false, "인증번호 재발송");
        }
    });
}

function confirmPasswordResetCode() {
    const email = getResetEmail();
    const code = $("#resetCode").val().trim();

    if (!isEmail(email)) {
        showFieldMessage("#emailGuide", "올바른 이메일을 입력해주세요.", "error");
        $("#resetEmail").focus();
        return;
    }

    if (code.length !== 6) {
        showFieldMessage("#codeGuide", "인증번호 6자리를 입력해주세요.", "error");
        $("#resetCode").focus();
        return;
    }

    if (resetPreviewMode) {
        if (code !== "123456") {
            showFieldMessage("#codeGuide", "미리보기 인증번호는 123456입니다.", "error");
            return;
        }
        completeCodeVerification(email, code);
        return;
    }

    setButtonLoading("#confirmResetCodeBtn", true, "확인 중");

    $.ajax({
        url: PASSWORD_RESET_CONFIRM_API,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email, code }),
        success: function () {
            completeCodeVerification(email, code);
        },
        error: function (xhr) {
            showFieldMessage("#codeGuide", getErrorMessage(xhr, "인증번호가 올바르지 않거나 만료되었습니다."), "error");
        },
        complete: function () {
            setButtonLoading("#confirmResetCodeBtn", false, "확인");
        }
    });
}

function resetPassword(e) {
    e.preventDefault();

    const email = getResetEmail();
    const code = verifiedResetCode;
    const newPassword = $("#newPassword").val();
    const newPasswordConfirm = $("#newPasswordConfirm").val();

    if (!isResetCodeVerified(email)) {
        showFieldMessage("#codeGuide", "이메일 인증을 먼저 완료해주세요.", "error");
        $("#resetCode").focus();
        return;
    }

    if (!isValidPassword(newPassword)) {
        showFieldMessage("#passwordGuide", "비밀번호는 8자 이상이어야 합니다.", "error");
        $("#newPassword").focus();
        return;
    }

    if (newPassword !== newPasswordConfirm) {
        showFieldMessage("#passwordConfirmGuide", "비밀번호가 서로 일치하지 않습니다.", "error");
        $("#newPasswordConfirm").focus();
        return;
    }

    setButtonLoading("#resetPasswordBtn", true, "재설정 중...");

    if (resetPreviewMode) {
        setTimeout(function () {
            finishPasswordReset();
        }, 350);
        return;
    }

    $.ajax({
        url: PASSWORD_RESET_API,
        type: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({
            email,
            code,
            newPassword,
            newPasswordConfirm
        }),
        success: finishPasswordReset,
        error: function (xhr) {
            showFieldMessage("#passwordConfirmGuide", getErrorMessage(xhr, "비밀번호 재설정에 실패했습니다."), "error");
            setButtonLoading("#resetPasswordBtn", false, "비밀번호 재설정");
            updateResetButtonState();
        }
    });
}

function openVerificationPanel(message) {
    $("#verifyPanel").prop("hidden", false);
    $("#resetCode").val("").focus();
    showFieldMessage("#emailGuide", "인증번호 발송이 완료되었습니다.", "success");
    showFieldMessage("#codeGuide", message, "success");
    setStepState("code");
}

function completeCodeVerification(email, code) {
    verifiedResetEmail = email.toLowerCase();
    verifiedResetCode = code;
    $("#resetEmail").prop("disabled", true);
    $("#resetCode").prop("disabled", true);
    $("#confirmResetCodeBtn").prop("disabled", true).text("인증 완료");
    $("#sendResetCodeBtn").prop("disabled", true).text("인증번호 인증 완료");
    $("#passwordPanel").prop("hidden", false);
    $("#newPassword").focus();
    showFieldMessage("#codeGuide", "이메일 인증이 완료되었습니다. 새 비밀번호를 입력해주세요.", "success");
    setStepState("password");
    updateResetButtonState();
}

function finishPasswordReset() {
    showFieldMessage("#passwordConfirmGuide", "비밀번호가 재설정되었습니다. 로그인 페이지로 이동합니다.", "success");
    $("#resetPasswordBtn").text("재설정 완료");
    setTimeout(function () {
        location.href = "login.html";
    }, 900);
}

function resetVerificationState() {
    verifiedResetEmail = "";
    verifiedResetCode = "";
    resetPreviewMode = false;
    $("#verifyPanel").prop("hidden", true);
    $("#passwordPanel").prop("hidden", true);
    $("#resetCode").val("").prop("disabled", false);
    $("#newPassword, #newPasswordConfirm").val("");
    $("#sendResetCodeBtn").prop("disabled", false).text("인증번호 발송");
    $("#confirmResetCodeBtn").prop("disabled", false).text("확인");
    showFieldMessage("#emailGuide", "이메일 입력 후 인증번호를 발송해주세요.");
    showFieldMessage("#codeGuide", "인증번호는 일정 시간이 지나면 만료될 수 있습니다.");
    showFieldMessage("#passwordGuide", "영문, 숫자, 특수문자를 섞으면 더 안전합니다.");
    showFieldMessage("#passwordConfirmGuide", "비밀번호 확인을 입력해주세요.");
    setStepState("email");
    updatePasswordState();
    updateResetButtonState();
}

function updatePasswordState() {
    const password = $("#newPassword").val();
    const confirm = $("#newPasswordConfirm").val();
    const score = getPasswordScore(password);
    const meter = $(".password-meter");

    meter.removeClass("is-medium is-strong");
    meter.find("span").css("width", (score * 25) + "%");

    if (score >= 4) {
        meter.addClass("is-strong");
        showFieldMessage("#passwordGuide", "안전한 비밀번호입니다.", "success");
    } else if (score >= 2) {
        meter.addClass("is-medium");
        showFieldMessage("#passwordGuide", "사용 가능하지만 조금 더 복잡하게 만들 수 있어요.");
    } else if (password.length > 0) {
        showFieldMessage("#passwordGuide", "비밀번호는 8자 이상이어야 합니다.", "error");
    } else {
        showFieldMessage("#passwordGuide", "영문, 숫자, 특수문자를 섞으면 더 안전합니다.");
    }

    if (!confirm) {
        showFieldMessage("#passwordConfirmGuide", "비밀번호 확인을 입력해주세요.");
    } else if (password === confirm) {
        showFieldMessage("#passwordConfirmGuide", "비밀번호가 일치합니다.", "success");
    } else {
        showFieldMessage("#passwordConfirmGuide", "비밀번호가 서로 일치하지 않습니다.", "error");
    }
}

function updateResetButtonState() {
    const email = getResetEmail();
    const enabled = isResetCodeVerified(email)
        && isValidPassword($("#newPassword").val())
        && $("#newPassword").val() === $("#newPasswordConfirm").val();

    $("#resetPasswordBtn").prop("disabled", !enabled);
}

function setStepState(activeStep) {
    const order = ["email", "code", "password"];
    const activeIndex = order.indexOf(activeStep);

    $(".step").each(function () {
        const step = $(this).data("step");
        const index = order.indexOf(step);
        $(this)
            .toggleClass("is-active", step === activeStep)
            .toggleClass("is-done", index < activeIndex);
    });
}

function togglePasswordVisibility() {
    const target = $($(this).data("target"));
    const isPassword = target.attr("type") === "password";
    target.attr("type", isPassword ? "text" : "password");
    $(this).text(isPassword ? "🙈" : "👁");
}

function setButtonLoading(selector, loading, text) {
    $(selector)
        .prop("disabled", loading)
        .text(text);
}

function showFieldMessage(selector, message, type) {
    $(selector)
        .text(message || "")
        .removeClass("error success")
        .addClass(type || "");
}

function getResetEmail() {
    return $("#resetEmail").val().trim();
}

function isResetCodeVerified(email) {
    return verifiedResetEmail !== "" && verifiedResetEmail === email.trim().toLowerCase() && verifiedResetCode !== "";
}

function isEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPassword(password) {
    return String(password || "").length >= 8;
}

function getPasswordScore(password) {
    let score = 0;
    if (password.length >= 8) score += 1;
    if (password.length >= 12) score += 1;
    if (/[A-Za-z]/.test(password) && /[0-9]/.test(password)) score += 1;
    if (/[^A-Za-z0-9]/.test(password)) score += 1;
    return score;
}

function canUsePreviewMode(xhr) {
    return xhr && (xhr.status === 404 || xhr.status === 405 || xhr.status === 0);
}

function getErrorMessage(xhr, fallback) {
    if (!xhr || !xhr.responseJSON) return fallback;
    return xhr.responseJSON.data || xhr.responseJSON.message || fallback;
}
