const POINT_API_BASE = window.StayNowConfig.apiBase;

let pointAuth = null;
let pointHistories = [];

$(function () {
    pointAuth = readPointJson("staynowAuth");

    if (!pointAuth || !pointAuth.memberSid) {
        sessionStorage.setItem("afterLoginRedirect", "points.html");
        alert("포인트 페이지는 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    bindPointEvents();
    renderPointProfile();
    loadPointProfile();
    loadPointHistories();
});

function bindPointEvents() {
    $("#pointFilter").on("change", renderPointHistories);
    $("#sideLogoutBtn").on("click", function () {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
        location.href = "index.html";
    });
    $(document).on("click", ".point-history-hotel", function () {
        const hotelId = $(this).data("hotelId");
        if (hotelId) {
            location.href = "hotel-detail.html?id=" + encodeURIComponent(hotelId);
        }
    });
}

function loadPointProfile() {
    $.ajax({
        url: POINT_API_BASE + "/member/" + pointAuth.memberSid,
        type: "GET",
        headers: pointAuthHeaders(),
        success: function (result) {
            const member = result && result.data ? result.data : result;
            if (!member || typeof member !== "object") return;
            pointAuth = Object.assign({}, pointAuth, {
                name: member.name || pointAuth.name,
                email: member.email || pointAuth.email,
                point: Number(member.point || 0)
            });
            localStorage.setItem("staynowAuth", JSON.stringify(pointAuth));
            renderPointProfile();
            renderPointSummary();
        }
    });
}

function loadPointHistories() {
    $.ajax({
        url: POINT_API_BASE + "/reservation/point-history",
        type: "GET",
        headers: pointAuthHeaders(),
        data: {
            memberId: pointAuth.memberSid,
            page: 0,
            size: 100,
            sort: "createdAt,desc"
        },
        success: function (result) {
            const page = result && result.content ? result : (result.data || {});
            pointHistories = Array.isArray(page.content) ? page.content : [];
            renderPointProfile();
            renderPointSummary();
            renderPointHistories();
        },
        error: function () {
            $("#pointHistoryList").html('<div class="empty-state">포인트 내역을 불러오지 못했습니다.</div>');
        }
    });
}

function renderPointProfile() {
    const name = pointAuth.name || pointAuth.email || "회원";
    $("#profileInitial").text(String(name).slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(pointAuth.email || "");
    $("#profilePoint, #availablePoint").text(formatPointNumber(pointAuth.point || 0) + "P");
    $("#historyCount, #navPointCount").text(pointHistories.length);
}

function renderPointSummary() {
    const totalEarn = pointHistories
        .filter(function (history) { return Number(history.amount || 0) > 0; })
        .reduce(function (sum, history) { return sum + Number(history.amount || 0); }, 0);
    const totalUse = pointHistories
        .filter(function (history) { return Number(history.amount || 0) < 0; })
        .reduce(function (sum, history) { return sum + Math.abs(Number(history.amount || 0)); }, 0);

    $("#totalEarnPoint").text(formatPointNumber(totalEarn) + "P");
    $("#totalUsePoint").text(formatPointNumber(totalUse) + "P");
}

function renderPointHistories() {
    const filter = $("#pointFilter").val();
    const histories = pointHistories.filter(function (history) {
        const amount = Number(history.amount || 0);
        if (filter === "EARN") return amount > 0;
        if (filter === "USE") return amount < 0;
        return true;
    });

    if (!histories.length) {
        $("#pointHistoryList").html('<div class="empty-state">조건에 맞는 포인트 내역이 없습니다.</div>');
        return;
    }

    $("#pointHistoryList").html(histories.map(renderPointHistoryItem).join(""));
}

function renderPointHistoryItem(history) {
    const amount = Number(history.amount || 0);
    const info = getPointStatusInfo(history.pointStatus, amount);
    const sign = amount > 0 ? "+" : "";
    const hotelName = history.hotelName || "예약 정보";
    const roomName = history.roomName ? " · " + history.roomName : "";
    const reservationNumber = history.reservationNumber ? history.reservationNumber : "-";

    return `<article class="point-history-card ${info.kind}">
        <div class="point-history-icon"><i class="${info.icon}"></i></div>
        <button type="button" class="point-history-main point-history-hotel" data-hotel-id="${escapePoint(history.hotelId || "")}">
            <strong>${escapePoint(info.title)}</strong>
            <span>${escapePoint(hotelName + roomName)}</span>
            <small>${escapePoint(reservationNumber)} · ${formatPointDate(history.createdAt)}</small>
        </button>
        <div class="point-history-amount">
            <strong>${sign}${formatPointNumber(amount)}P</strong>
            <span>${escapePoint(info.badge)}</span>
        </div>
    </article>`;
}

function getPointStatusInfo(status, amount) {
    const normalized = String(status || "").toUpperCase();
    if (normalized === "USE") {
        return { title: "예약 결제 사용", badge: "사용", icon: "fa-solid fa-credit-card", kind: "use" };
    }
    if (normalized === "USE_CANCEL_REFUND") {
        return { title: "예약 취소 포인트 환급", badge: "환급", icon: "fa-solid fa-rotate-left", kind: "earn" };
    }
    if (normalized === "ACCUMULATION_CANCEL_REVOKE") {
        return { title: "예약 취소 적립 회수", badge: "회수", icon: "fa-solid fa-circle-minus", kind: "use" };
    }
    if (amount < 0) {
        return { title: "포인트 사용", badge: "사용", icon: "fa-solid fa-credit-card", kind: "use" };
    }
    return { title: "포인트 적립", badge: "적립", icon: "fa-solid fa-coins", kind: "earn" };
}

function pointAuthHeaders() {
    return pointAuth && pointAuth.token ? { Authorization: pointAuth.token } : {};
}

function readPointJson(key) {
    try {
        return JSON.parse(localStorage.getItem(key) || sessionStorage.getItem(key) || "null");
    } catch (e) {
        return null;
    }
}

function formatPointNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function formatPointDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value).slice(0, 10).replaceAll("-", ".");
    return date.getFullYear() + "." + String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0");
}

function escapePoint(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
