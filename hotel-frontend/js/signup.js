$(function () {

    init();

});

const SIGNUP_API = window.StayNowConfig.apiUrl("/member/signup");
const TERMS_API = window.StayNowConfig.apiUrl("/term");

function init() {

    bindEvent();
    checkInput();
    updatePasswordStrength();

}

/* ==========================
    Event
========================== */

function bindEvent() {

    $("#signupForm").submit(signup);

    $(".toggle-password").click(togglePassword);

    $("#phone").on("input", formatPhone);

    $("#name, #email, #password, #passwordConfirm").on("input", checkInput);

    $("#password").on("input", updatePasswordStrength);

    $("#agreeAll").change(toggleAllTerms);

    $(".required-term, .optional-term").change(syncTerms);

}

/* ==========================
    회원가입
========================== */

function signup(e) {

    e.preventDefault();

    const name = $("#name").val().trim();
    const phone = $("#phone").val().trim();
    const email = $("#email").val().trim();
    const password = $("#password").val();
    const passwordConfirm = $("#passwordConfirm").val();

    if (name === "") {
        alert("이름을 입력해주세요.");
        $("#name").focus();
        return;
    }

    if (phone === "") {
        alert("휴대폰 번호를 입력해주세요.");
        $("#phone").focus();
        return;
    }

    if (!isPhone(phone)) {
        alert("올바른 휴대폰 번호 형식이 아닙니다.");
        $("#phone").focus();
        return;
    }

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

    if (password.length < 8) {
        alert("비밀번호는 8자 이상 입력해주세요.");
        $("#password").focus();
        return;
    }

    if (password !== passwordConfirm) {
        alert("비밀번호가 일치하지 않습니다.");
        $("#passwordConfirm").focus();
        return;
    }

    if (!isRequiredTermsChecked()) {
        alert("필수 약관에 동의해주세요.");
        return;
    }

    requestSignup({
        name,
        phone,
        email,
        password,
        marketingAgree: $(".optional-term").is(":checked")
    });

}

function requestSignup(formData) {

    setSignupLoading(true);

    loadTerms()
        .done(function (termsResult) {
            const terms = termsResult.data || [];
            const payload = makeSignupPayload(formData, terms);

            $.ajax({
                url: SIGNUP_API,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(payload),

                success: function () {
                    alert("회원가입이 완료되었습니다. 로그인해주세요.");
                    location.href = "login.html";
                },

                error: function (xhr) {
                    alert(getErrorMessage(xhr, "회원가입 중 오류가 발생했습니다."));
                },

                complete: function () {
                    setSignupLoading(false);
                    checkInput();
                }
            });
        })
        .fail(function (xhr) {
            setSignupLoading(false);
            checkInput();
            alert(getErrorMessage(xhr, "약관 정보를 불러오지 못했습니다."));
        });

}

function loadTerms() {

    return $.ajax({
        url: TERMS_API,
        type: "GET"
    });

}

function makeSignupPayload(formData, terms) {

    return {
        name: formData.name,
        phone: formData.phone,
        email: formData.email,
        status: "ACTIVE",
        role: "USER",
        emailVerified: true,
        phoneVerified: false,
        point: 0,
        authAccount: {
            provider: "LOCAL",
            providerUserId: formData.email,
            password: formData.password,
            passwordHash: null
        },
        termAgreements: makeTermAgreements(terms, formData.marketingAgree)
    };

}

function makeTermAgreements(terms, marketingAgree) {

    return terms
        .filter(function (term) {
            return term && term.sid && term.deleted !== true;
        })
        .map(function (term) {
            return {
                sid: term.sid,
                isAgreed: term.isRequired ? true : marketingAgree
            };
        });

}

function setSignupLoading(loading) {

    $("#signupBtn")
        .prop("disabled", loading)
        .html(loading ? '<i class="fa-solid fa-spinner fa-spin"></i> 가입 중...' : '<i class="fa-solid fa-user-plus"></i> 무료 회원가입');

}

function getErrorMessage(xhr, fallbackMessage) {

    if (xhr.responseJSON) {
        return xhr.responseJSON.data || xhr.responseJSON.message || fallbackMessage;
    }

    return fallbackMessage;

}

/* ==========================
    비밀번호 보기
========================== */

function togglePassword() {

    const button = $(this);
    const input = $(button.data("target"));
    const icon = button.find("i");

    if (input.attr("type") === "password") {

        input.attr("type", "text");
        icon.removeClass("fa-eye-slash").addClass("fa-eye");

    } else {

        input.attr("type", "password");
        icon.removeClass("fa-eye").addClass("fa-eye-slash");

    }

}

/* ==========================
    휴대폰 번호 자동 포맷
========================== */

function formatPhone() {

    const numbers = $(this).val().replace(/[^0-9]/g, "").slice(0, 11);
    let formatted = numbers;

    if (numbers.length > 7) {
        formatted = numbers.replace(/(\d{3})(\d{4})(\d{0,4})/, "$1-$2-$3");
    } else if (numbers.length > 3) {
        formatted = numbers.replace(/(\d{3})(\d{0,4})/, "$1-$2");
    }

    $(this).val(formatted);

    checkInput();

}

/* ==========================
    비밀번호 강도
========================== */

function updatePasswordStrength() {

    const password = $("#password").val();
    const level = getPasswordStrength(password);
    const strengthText = ["약함", "약함", "보통", "좋음", "강함"];
    const strengthColor = ["#ef4444", "#ef4444", "#f59e0b", "#10b981", "#2563eb"];

    $(".strength")
        .removeClass("level-1 level-2 level-3 level-4")
        .addClass("level-" + level);

    $("#strengthText")
        .text(strengthText[level])
        .css("color", strengthColor[level]);

}

function getPasswordStrength(password) {

    let score = 0;

    if (password.length >= 8) {
        score++;
    }

    if (/[A-Za-z]/.test(password) && /[0-9]/.test(password)) {
        score++;
    }

    if (/[^A-Za-z0-9]/.test(password)) {
        score++;
    }

    if (password.length >= 12) {
        score++;
    }

    return Math.max(1, score);

}

/* ==========================
    약관
========================== */

function toggleAllTerms() {

    const checked = $(this).is(":checked");

    $(".required-term, .optional-term").prop("checked", checked);

    checkInput();

}

function syncTerms() {

    const totalCount = $(".required-term, .optional-term").length;
    const checkedCount = $(".required-term:checked, .optional-term:checked").length;

    $("#agreeAll").prop("checked", totalCount === checkedCount);

    checkInput();

}

function isRequiredTermsChecked() {

    return $(".required-term").length === $(".required-term:checked").length;

}

/* ==========================
    검사
========================== */

function isEmail(email) {

    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    return regex.test(email);

}

function isPhone(phone) {

    const regex = /^010-\d{4}-\d{4}$/;

    return regex.test(phone);

}

/* ==========================
    버튼 활성화
========================== */

function checkInput() {

    const name = $("#name").val().trim();
    const phone = $("#phone").val().trim();
    const email = $("#email").val().trim();
    const password = $("#password").val();
    const passwordConfirm = $("#passwordConfirm").val();
    const emailValid = isEmail(email);
    const enabled =
        name !== "" &&
        isPhone(phone) &&
        emailValid &&
        password.length >= 8 &&
        password === passwordConfirm &&
        isRequiredTermsChecked();

    updateEmailMessage(email, emailValid);

    $("#signupBtn").prop("disabled", !enabled);

}

function updateEmailMessage(email, isValid) {

    const emailBox = $("#email").closest(".input-box");

    if (email === "") {
        emailBox.removeClass("has-status is-error");

        $("#emailMessage")
            .text("이메일을 입력해주세요")
            .removeClass("error");
        return;
    }

    if (isValid) {
        emailBox.addClass("has-status").removeClass("is-error");

        $("#emailMessage")
            .text("사용 가능한 이메일입니다")
            .removeClass("error");
    } else {
        emailBox.removeClass("has-status").addClass("is-error");

        $("#emailMessage")
            .text("올바른 이메일 형식이 아닙니다")
            .addClass("error");
    }

}
