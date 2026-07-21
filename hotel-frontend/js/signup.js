$(function () {

    init();

});

const SIGNUP_API = window.StayNowConfig.apiUrl("/auth/signup");
const TERMS_API = window.StayNowConfig.apiUrl("/term");
const EMAIL_VERIFY_SEND_API = window.StayNowConfig.apiUrl("/mail/verification/send");
const EMAIL_VERIFY_CONFIRM_API = window.StayNowConfig.apiUrl("/mail/verification/confirm");
const MEMBER_LIST_API = window.StayNowConfig.apiUrl("/member");
const TERM_TYPES = ["SERVICE", "PRIVACY", "MARKETING"];
const TERM_CONTENTS = {
    SERVICE: {
        badge: "필수",
        title: "이용약관 동의",
        body: `
            <h5>제1조 목적</h5>
            <p>본 약관은 StayNow가 제공하는 호텔 검색, 예약, 결제 및 관련 서비스의 이용 조건과 절차를 정하기 위한 더미 약관입니다.</p>
            <h5>제2조 서비스 이용</h5>
            <ul>
                <li>회원은 정확한 정보를 입력하고 본인의 계정으로 서비스를 이용해야 합니다.</li>
                <li>예약 가능 여부, 객실 가격, 프로모션은 호텔 및 객실 상태에 따라 변경될 수 있습니다.</li>
                <li>부정 이용, 타인의 정보 도용, 서비스 운영 방해 행위는 제한될 수 있습니다.</li>
            </ul>
            <h5>제3조 예약 및 취소</h5>
            <p>예약, 취소, 환불 조건은 예약 시점에 안내된 정책을 기준으로 처리됩니다. 테스트 결제 및 더미 데이터는 실제 숙박 계약으로 간주하지 않습니다.</p>
        `
    },
    PRIVACY: {
        badge: "필수",
        title: "개인정보 수집 및 이용 동의",
        body: `
            <h5>수집 항목</h5>
            <p>이름, 이메일, 휴대폰 번호, 비밀번호, 예약 정보, 서비스 이용 기록을 수집할 수 있습니다.</p>
            <h5>수집 목적</h5>
            <ul>
                <li>회원 식별 및 로그인 처리</li>
                <li>호텔 예약, 예약 확인, 고객 문의 대응</li>
                <li>부정 이용 방지 및 서비스 품질 개선</li>
            </ul>
            <h5>보유 기간</h5>
            <p>회원 탈퇴 또는 수집 목적 달성 시까지 보관하며, 관련 법령에 따라 필요한 정보는 정해진 기간 동안 보관할 수 있습니다.</p>
        `
    },
    MARKETING: {
        badge: "선택",
        title: "마케팅 정보 수신 동의",
        body: `
            <h5>수신 내용</h5>
            <p>특가 호텔, 쿠폰, 이벤트, 맞춤 추천, 프로모션 정보를 이메일 또는 앱/웹 알림으로 받을 수 있습니다.</p>
            <h5>이용 항목</h5>
            <p>이메일, 휴대폰 번호, 예약 및 관심 호텔 정보를 마케팅 메시지 발송과 맞춤 혜택 제공에 활용할 수 있습니다.</p>
            <h5>동의 철회</h5>
            <p>마케팅 정보 수신 동의는 선택 사항이며, 동의하지 않아도 회원가입과 기본 서비스 이용에는 제한이 없습니다.</p>
        `
    }
};

let verifiedEmail = "";
let checkedEmail = "";
let emailAvailable = false;
let emailCheckTimer = null;
let emailCheckRequest = null;

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

    $("#email").on("input", resetEmailVerification);

    $("#emailCode").on("input", function () {
        $(this).val($(this).val().replace(/[^0-9]/g, "").slice(0, 6));
    });

    $("#sendEmailCodeBtn").on("click", sendEmailVerificationCode);

    $("#confirmEmailCodeBtn").on("click", confirmEmailVerificationCode);

    $("#password").on("input", updatePasswordStrength);

    $("#agreeAll").change(toggleAllTerms);

    $(".required-term, .optional-term").change(syncTerms);

    $(".term-view-btn").on("click", openTermModal);

    $("[data-close-term-modal]").on("click", closeTermModal);

    $(document).on("keydown", function (event) {
        if (event.key === "Escape") {
            closeTermModal();
        }
    });

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

    if (!emailAvailable || checkedEmail !== email.toLowerCase()) {
        alert("이미 사용 중인 이메일인지 확인 중입니다. 잠시 후 다시 시도해주세요.");
        $("#email").focus();
        return;
    }

    if (!isEmailVerified(email)) {
        alert("이메일 인증을 완료해주세요.");
        $("#emailCode").focus();
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

function sendEmailVerificationCode() {
    const email = $("#email").val().trim();

    if (!isEmail(email)) {
        alert("올바른 이메일을 입력한 뒤 인증번호를 발송해주세요.");
        $("#email").focus();
        return;
    }

    if (!emailAvailable || checkedEmail !== email.toLowerCase()) {
        alert("사용 가능한 이메일인지 확인한 뒤 인증번호를 발송해주세요.");
        $("#email").focus();
        return;
    }

    setEmailVerificationLoading("send", true);

    $.ajax({
        url: EMAIL_VERIFY_SEND_API,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email }),
        success: function () {
            $("#emailCodeRow").prop("hidden", false);
            $("#emailVerificationMessage")
                .prop("hidden", false)
                .text("인증번호를 발송했습니다. 5분 안에 입력해주세요.")
                .removeClass("error");
            $("#emailCode").val("").focus();
        },
        error: function (xhr) {
            $("#emailVerificationMessage")
                .text(getErrorMessage(xhr, "인증번호 발송에 실패했습니다."))
                .addClass("error");
        },
        complete: function () {
            setEmailVerificationLoading("send", false);
            checkInput();
        }
    });
}

function confirmEmailVerificationCode() {
    const email = $("#email").val().trim();
    const code = $("#emailCode").val().trim();

    if (!isEmail(email)) {
        alert("올바른 이메일을 입력해주세요.");
        $("#email").focus();
        return;
    }

    if (code.length !== 6) {
        alert("인증번호 6자리를 입력해주세요.");
        $("#emailCode").focus();
        return;
    }

    setEmailVerificationLoading("confirm", true);

    $.ajax({
        url: EMAIL_VERIFY_CONFIRM_API,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ email, code }),
        success: function () {
            verifiedEmail = email.toLowerCase();
            $("#emailVerificationBox").addClass("verified");
            $("#emailCodeRow").prop("hidden", true);
            $("#emailVerificationMessage")
                .prop("hidden", true)
                .text("")
                .removeClass("error");
            $("#emailCode").prop("disabled", true);
            $("#sendEmailCodeBtn")
                .prop("disabled", true)
                .html('<i class="fa-solid fa-circle-check"></i> 이메일 인증 완료');
            $("#confirmEmailCodeBtn").prop("disabled", true);
        },
        error: function (xhr) {
            verifiedEmail = "";
            $("#emailVerificationMessage")
                .text(getErrorMessage(xhr, "인증번호가 올바르지 않거나 만료되었습니다."))
                .addClass("error");
        },
        complete: function () {
            if (!isEmailVerified(email)) {
                setEmailVerificationLoading("confirm", false);
            }
            checkInput();
        }
    });
}

function setEmailVerificationLoading(type, loading) {
    if (type === "send") {
        $("#sendEmailCodeBtn")
            .prop("disabled", loading)
            .html(loading ? '<i class="fa-solid fa-spinner fa-spin"></i> 발송 중...' : '<i class="fa-regular fa-paper-plane"></i> 인증번호 발송');
        return;
    }

    $("#confirmEmailCodeBtn")
        .prop("disabled", loading)
        .text(loading ? "확인 중" : "확인");
}

function resetEmailVerification() {
    const email = $("#email").val().trim().toLowerCase();
    if (verifiedEmail && verifiedEmail === email) {
        return;
    }

    verifiedEmail = "";
    $("#emailVerificationBox").removeClass("verified");
    $("#emailCodeRow").prop("hidden", true);
    $("#emailCode").prop("disabled", false);
    $("#sendEmailCodeBtn").prop("disabled", false).html('<i class="fa-regular fa-paper-plane"></i> 인증번호 발송');
    $("#confirmEmailCodeBtn").prop("disabled", false).text("확인");
    $("#emailVerificationMessage")
        .prop("hidden", true)
        .text("이메일 인증을 완료해주세요")
        .removeClass("error");
    checkInput();
}

function isEmailVerified(email) {
    return verifiedEmail !== "" && verifiedEmail === email.trim().toLowerCase();
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

    const activeTerms = Array.isArray(terms) ? terms.filter(function (term) {
        return term && term.sid && term.deleted !== true;
    }) : [];

    return TERM_TYPES
        .map(function (termType) {
            const term = findTermByType(activeTerms, termType);
            if (!term) {
                return null;
            }

            return {
                sid: term.sid,
                isAgreed: termType === "MARKETING" ? marketingAgree : true
            };
        })
        .filter(Boolean);

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

function openTermModal(event) {
    event.preventDefault();
    event.stopPropagation();

    const termType = $(this).data("term-type");
    const term = TERM_CONTENTS[termType] || TERM_CONTENTS.SERVICE;

    $("#termModalBadge").text(term.badge);
    $("#termModalTitle").text(term.title);
    $("#termModalBody").html(term.body);
    $("#termModal").prop("hidden", false);
    $("body").addClass("term-modal-open");
}

function closeTermModal() {
    $("#termModal").prop("hidden", true);
    $("body").removeClass("term-modal-open");
}

function findTermByType(terms, termType) {
    return terms.find(function (term) {
        return String(term.termType || "").toUpperCase() === termType;
    }) || terms.find(function (term) {
        return String(term.title || "").includes(TERM_CONTENTS[termType].title.replace(" 동의", ""));
    });
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
        emailAvailable &&
        checkedEmail === email.toLowerCase() &&
        isEmailVerified(email) &&
        password.length >= 8 &&
        password === passwordConfirm &&
        isRequiredTermsChecked();

    updateEmailMessage(email, emailValid);
    scheduleEmailDuplicateCheck(email, emailValid);

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

        if (checkedEmail === email.toLowerCase() && emailAvailable) {
            $("#emailMessage")
                .text("사용 가능한 이메일입니다")
                .removeClass("error");
        } else if (checkedEmail === email.toLowerCase() && !emailAvailable) {
            emailBox.removeClass("has-status").addClass("is-error");
            $("#emailMessage")
                .text("이미 사용 중인 이메일입니다")
                .addClass("error");
        } else {
            $("#emailMessage")
                .text("이메일 중복 확인 중입니다")
                .removeClass("error");
        }
    } else {
        emailBox.removeClass("has-status").addClass("is-error");

        $("#emailMessage")
            .text("올바른 이메일 형식이 아닙니다")
            .addClass("error");
    }

}

function scheduleEmailDuplicateCheck(email, isValid) {
    const normalizedEmail = email.trim().toLowerCase();

    if (!isValid) {
        checkedEmail = "";
        emailAvailable = false;
        if (emailCheckTimer) {
            clearTimeout(emailCheckTimer);
            emailCheckTimer = null;
        }
        if (emailCheckRequest) {
            emailCheckRequest.abort();
            emailCheckRequest = null;
        }
        return;
    }

    if (checkedEmail === normalizedEmail) {
        return;
    }

    emailAvailable = false;
    if (emailCheckTimer) {
        clearTimeout(emailCheckTimer);
    }

    emailCheckTimer = setTimeout(function () {
        checkEmailDuplicate(normalizedEmail);
    }, 350);
}

function checkEmailDuplicate(email) {
    if (emailCheckRequest) {
        emailCheckRequest.abort();
    }

    emailCheckRequest = $.ajax({
        url: MEMBER_LIST_API,
        type: "GET",
        success: function (result) {
            const members = Array.isArray(result.data) ? result.data : [];
            const used = members.some(function (member) {
                return String(member.email || "").trim().toLowerCase() === email;
            });

            checkedEmail = email;
            emailAvailable = !used;
            paintEmailAvailability(email, used);
        },
        error: function (xhr) {
            if (xhr.statusText === "abort") return;
            checkedEmail = "";
            emailAvailable = false;
            $("#email").closest(".input-box").removeClass("has-status").addClass("is-error");
            $("#emailMessage")
                .text("이메일 중복 확인에 실패했습니다")
                .addClass("error");
        },
        complete: function () {
            emailCheckRequest = null;
            checkInput();
        }
    });
}

function paintEmailAvailability(email, used) {
    if ($("#email").val().trim().toLowerCase() !== email) {
        return;
    }

    const emailBox = $("#email").closest(".input-box");
    if (used) {
        emailBox.removeClass("has-status").addClass("is-error");
        $("#emailMessage")
            .text("이미 사용 중인 이메일입니다")
            .addClass("error");
        resetEmailVerification();
        return;
    }

    emailBox.addClass("has-status").removeClass("is-error");
    $("#emailMessage")
        .text("사용 가능한 이메일입니다")
        .removeClass("error");
}
