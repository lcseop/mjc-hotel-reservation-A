const MY_API_BASE = "http://localhost:33000/api";
let myAuth = null;
let reservations = [];
let activeFilter = "ALL";
let currentReviewTarget = null;
let currentRating = 5;

$(function () {
    myAuth = getMyAuth();

    if (!myAuth || !myAuth.memberSid) {
        sessionStorage.setItem("afterLoginRedirect", "my-reservations.html");
        alert("마이페이지는 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    renderProfile();
    bindMyPageEvents();
    loadReservations();
});

function bindMyPageEvents() {
    $(".status-tabs button").on("click", function () {
        activeFilter = $(this).data("filter");
        $(".status-tabs button").removeClass("active");
        $(this).addClass("active");
        renderReservations();
    });

    $("#sortSelect").on("change", renderReservations);
    $("#sideLogoutBtn").on("click", logoutMyPage);
    $("#reviewCloseBtn").on("click", closeReviewModal);
    $("#reviewModal").on("click", function (event) {
        if (event.target === this) closeReviewModal();
    });

    $("#ratingPicker button").on("click", function () {
        currentRating = Number($(this).data("rating"));
        paintRating();
    });

    $(".review-modal").on("submit", submitReview);
}

function loadReservations() {
    $.ajax({
        url: MY_API_BASE + "/reservation/search",
        data: { memberId: myAuth.memberSid, page: 0, size: 100, sort: "createdAt,desc" },
        success: function (page) {
            reservations = (page.content || []).map(mergeLocalReservation);
            renderProfile();
            renderCounts();
            renderReservations();
        },
        error: function () {
            const cached = readJson("completedReservation");
            reservations = cached && cached.reservation ? [mergeCompletedData(cached)] : [];
            renderProfile();
            renderCounts();
            renderReservations();
        }
    });
}

function renderReservations() {
    const list = $("#reservationList");
    const items = sortReservations(filterReservations(reservations));

    list.empty();

    if (!items.length) {
        list.html('<div class="empty-state">조건에 맞는 예약 내역이 없습니다.</div>');
        return;
    }

    items.forEach(function (reservation) {
        list.append(makeReservationCard(reservation));
    });
}

function makeReservationCard(reservation) {
    const state = getReservationState(reservation);
    const reviewed = hasWrittenReview(reservation.sid);
    const card = $("<article>").addClass("reservation-card " + state.key);
    const cancelNote = state.key === "cancelled"
        ? '<div class="danger-note"><i class="fa-solid fa-circle-info"></i> 취소된 예약입니다. 환불 상태는 결제 내역에서 확인해주세요.</div>'
        : "";
    const leftAction = makeLeftAction(reservation, state, reviewed);

    card.html(
        '<div class="reservation-top">' +
            '<div class="hotel-title">' +
                '<div class="hotel-icon"><i class="fa-solid fa-hotel"></i></div>' +
                '<div><small>' + drawStars(reservation.hotelStarRating) + ' ' + (reservation.hotelStarRating || 0) + '성급</small>' +
                '<h2>' + escapeHtml(reservation.hotelName || "호텔명 없음") + '</h2>' +
                '<p><i class="fa-solid fa-location-dot"></i> ' + escapeHtml(reservation.hotelLocation || "위치 정보 없음") + '</p></div>' +
            '</div>' +
            '<span class="status-chip ' + state.key + '">' + state.label + '</span>' +
        '</div>' +
        cancelNote +
        '<div class="reservation-meta">' +
            metaItem("fa-regular fa-calendar", "체크인", formatDate(reservation.checkInDate), "오후 3:00 이후") +
            metaItem("fa-regular fa-calendar-check", "체크아웃", formatDate(reservation.checkOutDate), "오전 11:00 이전") +
            metaItem("fa-regular fa-moon", "숙박", (reservation.totalNights || 1) + "박", reservation.roomName || "객실") +
            metaItem("fa-solid fa-users", "투숙객", "성인 " + (reservation.adults || 1) + "명", "") +
            metaItem("fa-solid fa-wallet", "결제금액", formatWon(reservation.totalAmount), "세금 포함") +
        '</div>' +
        '<div class="card-actions">' +
            '<div class="action-left">' + leftAction + '</div>' +
            '<div class="action-right">' +
                (state.key === "upcoming" ? '<button type="button" class="outline-btn cancel-btn"><i class="fa-regular fa-circle-xmark"></i> 예약 취소</button>' : "") +
                '<a class="primary-btn" href="reservation-detail.html?id=' + reservation.sid + '"><i class="fa-regular fa-file-lines"></i> 예약 상세</a>' +
            '</div>' +
        '</div>'
    );

    card.find(".checkin-btn").on("click", function () { checkInReservation(reservation.sid); });
    card.find(".review-btn").on("click", function () { openReviewModal(reservation); });
    card.find(".cancel-btn").on("click", function () { cancelReservation(reservation.sid); });

    return card;
}

function makeLeftAction(reservation, state, reviewed) {
    if (state.key === "cancelled") {
        return "";
    }

    if (state.key === "completed") {
        return reviewed
            ? '<span class="mini-chip green"><i class="fa-solid fa-check"></i> 리뷰 작성 완료</span>'
            : '<button type="button" class="outline-btn review-btn"><i class="fa-regular fa-pen-to-square"></i> 리뷰 작성</button>';
    }

    return '<button type="button" class="mini-chip checkin-btn"><i class="fa-regular fa-clock"></i> 체크인</button>';
}

function checkInReservation(id) {
    $.ajax({
        url: MY_API_BASE + "/reservation/" + id + "/check-in",
        type: "PATCH",
        success: loadReservations,
        error: function () {
            alert("현재 체크인 처리할 수 없습니다. 체크인 가능 시간을 확인해주세요.");
        }
    });
}

function cancelReservation(id) {
    if (!confirm("이 예약을 취소할까요?")) return;

    $.ajax({
        url: MY_API_BASE + "/reservation/cancel",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ sid: id, cancelReason: "고객 직접 취소" }),
        success: loadReservations,
        error: function () { alert("예약 취소에 실패했습니다."); }
    });
}

function openReviewModal(reservation) {
    currentReviewTarget = reservation;
    currentRating = 5;
    $("#reviewHotelName").text((reservation.hotelName || "호텔") + " 이용은 어떠셨나요?");
    $("#reviewContent").val("");
    paintRating();
    $("#reviewModal").addClass("show").attr("aria-hidden", "false");
}

function closeReviewModal() {
    $("#reviewModal").removeClass("show").attr("aria-hidden", "true");
    currentReviewTarget = null;
}

function submitReview(event) {
    event.preventDefault();

    if (!currentReviewTarget) return;

    const payload = {
        hotelId: currentReviewTarget.hotelId,
        memberId: Number(myAuth.memberSid),
        reservationId: currentReviewTarget.sid,
        rating: currentRating,
        travelType: "FAMILY",
        content: $("#reviewContent").val().trim() || "좋은 숙박이었습니다.",
        categories: [],
        tags: []
    };

    $.ajax({
        url: MY_API_BASE + "/review",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload),
        success: function () {
            markReviewWritten(currentReviewTarget.sid);
            closeReviewModal();
            renderReservations();
        },
        error: function () {
            markReviewWritten(currentReviewTarget.sid);
            closeReviewModal();
            renderReservations();
            alert("리뷰 API 연결은 실패했지만 화면에는 작성 완료로 표시했습니다.");
        }
    });
}

function renderProfile() {
    const name = myAuth.name || "회원";
    $("#profileInitial").text(name.slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(myAuth.email || "");
    $("#profilePoint").text(formatNumber(myAuth.point || 0));
    $("#totalReservationCount, #navReservationCount").text(reservations.length);
}

function renderCounts() {
    $("#countAll").text(reservations.length);
    $("#countUpcoming").text(reservations.filter(function (r) { return getReservationState(r).key === "upcoming"; }).length);
    $("#countCompleted").text(reservations.filter(function (r) { return getReservationState(r).key === "completed"; }).length);
    $("#countCancelled").text(reservations.filter(function (r) { return getReservationState(r).key === "cancelled"; }).length);
}

function filterReservations(items) {
    if (activeFilter === "ALL") return items;
    return items.filter(function (item) {
        return getReservationState(item).group === activeFilter;
    });
}

function sortReservations(items) {
    const value = $("#sortSelect").val();
    return items.slice().sort(function (a, b) {
        if (value === "checkInAsc") return new Date(a.checkInDate) - new Date(b.checkInDate);
        if (value === "amountDesc") return Number(b.totalAmount || 0) - Number(a.totalAmount || 0);
        return new Date(b.createdAt || b.checkInDate) - new Date(a.createdAt || a.checkInDate);
    });
}

function getReservationState(reservation) {
    const status = reservation.reservationStatus;
    if (status === "CANCELLED" || status === "NO_SHOW") return { key: "cancelled", group: "CANCELLED", label: "취소됨" };
    if (status === "CHECKED_OUT" || status === "COMPLETED") return { key: "completed", group: "COMPLETED", label: "완료된 여행" };
    return { key: "upcoming", group: "UPCOMING", label: "예정된 여행" };
}

function mergeCompletedData(data) {
    const r = data.reservation || {};
    const h = data.hotel || {};
    const room = data.room || {};
    return Object.assign({}, r, {
        hotelId: h.sid || h.hotelId,
        hotelName: h.hotelName,
        hotelLocation: h.location,
        hotelStarRating: h.starRating,
        roomName: room.roomName,
        roomParking: room.parking
    });
}

function mergeLocalReservation(reservation) {
    const completed = readJson("completedReservation");
    if (completed && completed.reservation && Number(completed.reservation.sid) === Number(reservation.sid)) {
        return Object.assign({}, mergeCompletedData(completed), reservation);
    }
    return reservation;
}

function paintRating() {
    $("#ratingPicker button").each(function () {
        $(this).toggleClass("active", Number($(this).data("rating")) <= currentRating);
    });
}

function metaItem(icon, label, value, sub) {
    return '<div class="meta-item"><i class="' + icon + '"></i><div><span>' + label + '</span><strong>' + escapeHtml(value || "-") + '</strong><small>' + escapeHtml(sub || "") + '</small></div></div>';
}

function hasWrittenReview(id) {
    return localStorage.getItem("staynowReviewWritten:" + id) === "true";
}

function markReviewWritten(id) {
    localStorage.setItem("staynowReviewWritten:" + id, "true");
}

function getMyAuth() {
    return readJson("staynowAuth");
}

function logoutMyPage() {
    localStorage.removeItem("staynowAuth");
    sessionStorage.removeItem("staynowAuth");
    location.href = "index.html";
}

function readJson(key) {
    try {
        const value = sessionStorage.getItem(key) || localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function formatDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    return date.getFullYear() + "." + pad(date.getMonth() + 1) + "." + pad(date.getDate()) + " (" + days[date.getDay()] + ")";
}

function formatWon(value) {
    return "₩" + formatNumber(value || 0);
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString();
}

function pad(value) {
    return String(value).padStart(2, "0");
}

function drawStars(count) {
    const value = Number(count || 0);
    return "★".repeat(value) + "☆".repeat(Math.max(0, 5 - value));
}

function escapeHtml(value) {
    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
