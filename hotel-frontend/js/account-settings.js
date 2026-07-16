const ACCOUNT_API_BASE = window.StayNowConfig.apiBase;

let accountAuth = null;
let accountMember = null;
let accountAuthAccounts = [];

$(function () {
    accountAuth = readAccountJson("staynowAuth");

    if (!accountAuth || !accountAuth.memberSid) {
        sessionStorage.setItem("afterLoginRedirect", "account-settings.html");
        alert("내 정보는 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    bindAccountEvents();
    renderAccountProfile();
    loadAccountMember();
    loadAccountAuthAccounts();
});

function bindAccountEvents() {
    $("#sideLogoutBtn").on("click", function () {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
        location.href = "index.html";
    });

    $("#accountPhone").on("input", function () {
        $(this).val(formatAccountPhone($(this).val()));
    });

    $("#accountForm").on("submit", submitAccountForm);
}

function loadAccountMember() {
    $.ajax({
        url: ACCOUNT_API_BASE + "/member/" + accountAuth.memberSid,
        type: "GET",
        headers: accountAuthHeaders(),
        success: function (result) {
            accountMember = result && result.data ? result.data : result;
            accountAuth = Object.assign({}, accountAuth, {
                name: accountMember.name || accountAuth.name,
                email: accountMember.email || accountAuth.email,
                phone: accountMember.phone || accountAuth.phone,
                point: Number(accountMember.point || 0),
                role: accountMember.role || accountAuth.role
            });
            saveAccountAuth();
            renderAccountProfile();
            fillAccountForm();
        },
        error: function () {
            alert("회원 정보를 불러오지 못했습니다.");
        }
    });
}

function loadAccountAuthAccounts() {
    $.ajax({
        url: ACCOUNT_API_BASE + "/member/" + accountAuth.memberSid + "/auth-accounts",
        type: "GET",
        headers: accountAuthHeaders(),
        success: function (result) {
            accountAuthAccounts = result && result.data ? result.data : [];
        }
    });
}

function fillAccountForm() {
    $("#accountName").val(accountAuth.name || "");
    $("#accountPhone").val(formatAccountPhone(accountAuth.phone || ""));
    $("#accountEmail").val(accountAuth.email || "");
}

function renderAccountProfile() {
    const name = accountAuth.name || accountAuth.email || "회원";
    $("#profileInitial").text(String(name).slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(accountAuth.email || "");
    $("#profilePoint").text(formatAccountNumber(accountAuth.point || 0));
    $("#profileStatus").text(accountMember && accountMember.status ? formatAccountStatus(accountMember.status) : "정상");
}

function submitAccountForm(event) {
    event.preventDefault();

    const name = $("#accountName").val().trim();
    const phone = normalizeAccountPhone($("#accountPhone").val());
    const password = $("#accountPassword").val();
    const passwordConfirm = $("#accountPasswordConfirm").val();

    if (!name) {
        alert("이름을 입력해주세요.");
        return;
    }

    if (phone && phone.length < 10) {
        alert("전화번호를 확인해주세요.");
        return;
    }

    if (password || passwordConfirm) {
        if (password.length < 8) {
            alert("비밀번호는 8자 이상으로 입력해주세요.");
            return;
        }
        if (password !== passwordConfirm) {
            alert("비밀번호 확인이 일치하지 않습니다.");
            return;
        }
    }

    $("#accountSaveBtn").prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 저장 중');

    saveAccountMember(name, phone)
        .then(function () {
            if (!password) return $.Deferred().resolve().promise();
            return saveAccountPassword(password);
        })
        .done(function () {
            $("#accountPassword, #accountPasswordConfirm").val("");
            $("#accountPasswordHint").text("");
            alert("내 정보가 저장되었습니다.");
        })
        .fail(function (xhr) {
            const message = xhr && xhr.responseJSON && xhr.responseJSON.data
                ? xhr.responseJSON.data
                : "내 정보 저장에 실패했습니다.";
            alert(message);
        })
        .always(function () {
            $("#accountSaveBtn").prop("disabled", false).html('<i class="fa-regular fa-floppy-disk"></i> 저장하기');
        });
}

function saveAccountMember(name, phone) {
    return $.ajax({
        url: ACCOUNT_API_BASE + "/member",
        type: "PATCH",
        headers: accountAuthHeaders(),
        contentType: "application/json",
        data: JSON.stringify({
            sid: accountAuth.memberSid,
            name: name,
            phone: phone,
            email: accountAuth.email,
            status: accountMember && accountMember.status,
            role: accountMember && accountMember.role,
            emailVerified: accountMember && accountMember.emailVerified,
            point: accountMember && accountMember.point
        }),
        success: function (result) {
            const member = result && result.data ? result.data : result;
            accountMember = member;
            accountAuth = Object.assign({}, accountAuth, {
                name: member.name || name,
                phone: member.phone || phone,
                email: member.email || accountAuth.email,
                point: Number(member.point || accountAuth.point || 0),
                role: member.role || accountAuth.role
            });
            saveAccountAuth();
            renderAccountProfile();
            fillAccountForm();
        }
    });
}

function saveAccountPassword(password) {
    const localAccount = accountAuthAccounts.find(function (authAccount) {
        return String(authAccount.provider || "").toUpperCase() === "LOCAL";
    });

    if (!localAccount || !localAccount.sid) {
        return $.Deferred().reject({
            responseJSON: { data: "LOCAL 로그인 계정이 없어 비밀번호를 변경할 수 없습니다." }
        }).promise();
    }

    return $.ajax({
        url: ACCOUNT_API_BASE + "/member-auth-accounts",
        type: "PATCH",
        headers: accountAuthHeaders(),
        contentType: "application/json",
        data: JSON.stringify({
            sid: localAccount.sid,
            memberSid: accountAuth.memberSid,
            provider: localAccount.provider || "LOCAL",
            providerUserId: localAccount.providerUserId || accountAuth.email,
            password: password,
            lastLoginAt: localAccount.lastLoginAt || null
        })
    });
}

function formatAccountPhone(value) {
    const digits = normalizeAccountPhone(value).slice(0, 11);
    if (digits.length <= 3) return digits;
    if (digits.length <= 7) return digits.slice(0, 3) + "-" + digits.slice(3);
    return digits.slice(0, 3) + "-" + digits.slice(3, 7) + "-" + digits.slice(7);
}

function normalizeAccountPhone(value) {
    return String(value || "").replace(/\D/g, "");
}

function formatAccountStatus(status) {
    const normalized = String(status || "").toUpperCase();
    if (normalized === "ACTIVE") return "정상";
    if (normalized === "SUSPENDED") return "정지";
    if (normalized === "WITHDRAWN") return "탈퇴";
    return status || "정상";
}

function accountAuthHeaders() {
    return accountAuth && accountAuth.token ? { Authorization: accountAuth.token } : {};
}

function readAccountJson(key) {
    try {
        return JSON.parse(localStorage.getItem(key) || sessionStorage.getItem(key) || "null");
    } catch (e) {
        return null;
    }
}

function saveAccountAuth() {
    localStorage.setItem("staynowAuth", JSON.stringify(accountAuth));
}

function formatAccountNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}
