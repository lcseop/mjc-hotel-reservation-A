const DETAIL_API_BASE = "http://localhost:33000/api";
let detailAuth = null;
let detailReservation = null;

$(function () {
    detailAuth = readDetailJson("staynowAuth");

    if (!detailAuth || !detailAuth.memberSid) {
        sessionStorage.setItem("afterLoginRedirect", location.pathname.split("/").pop() + location.search);
        alert("예약 상세는 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    renderDetailProfile();
    loadLatestDetailProfile();
    loadDetailReservationCount();
    $("#sideLogoutBtn").on("click", logoutDetail);
    $("#cancelReservationBtn").on("click", cancelDetailReservation);
    $("#detailHotelName, #infoHotelName").on("click", goDetailHotelPage);
    $("#detailHotelName, #infoHotelName").on("keydown", function (event) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            goDetailHotelPage();
        }
    });
    loadReservationDetail();
});

function loadReservationDetail() {
    const id = new URLSearchParams(location.search).get("id");

    if (!id) {
        alert("예약 번호가 없습니다.");
        location.href = "my-reservations.html";
        return;
    }

    $.ajax({
        url: DETAIL_API_BASE + "/reservation/" + id,
        success: function (reservation) {
            detailReservation = mergeDetailLocal(reservation);
            renderReservationDetail(detailReservation);
            loadHotelExtras(detailReservation.hotelId);
        },
        error: function () {
            const cached = readDetailJson("completedReservation");
            if (cached && cached.reservation && Number(cached.reservation.sid) === Number(id)) {
                detailReservation = mergeDetailCompleted(cached);
                renderReservationDetail(detailReservation);
                loadHotelExtras(detailReservation.hotelId);
                return;
            }
            alert("예약 상세 정보를 불러오지 못했습니다.");
            location.href = "my-reservations.html";
        }
    });
}

function renderReservationDetail(reservation) {
    const status = getDetailState(reservation);
    const reservationNumber = reservation.reservationNumber || "-";
    const hotelName = reservation.hotelName || "호텔명 없음";
    const locationText = reservation.hotelLocation || "위치 정보 없음";

    $("#detailHotelName, #infoHotelName")
        .text(hotelName)
        .attr({ role: "link", tabindex: "0", title: "호텔 상세 보기" });
    $("#detailLocation, #infoLocation, #guideAddress").text(locationText);
    $("#detailReservationNumber, #infoReservationNumber").text(reservationNumber);
    $("#detailStatus").text(status.label);
    $("#detailStars").text(drawDetailStars(reservation.hotelStarRating) + " " + (reservation.hotelStarRating || 0) + "성급");
    $("#detailScore").text((reservation.hotelStarRating ? (Number(reservation.hotelStarRating) * 1.8 + 0.4).toFixed(1) : "0.0") + " 매우 좋음");

    $("#infoCheckIn").text(formatDetailDate(reservation.checkInDate) + " 오후 3:00 이후");
    $("#infoCheckOut").text(formatDetailDate(reservation.checkOutDate) + " 오전 11:00 이전");
    $("#infoNights").text((reservation.totalNights || 1) + "박");
    $("#infoRoom").text((reservation.roomName || "객실") + (reservation.roomTypeTitle ? " · " + reservation.roomTypeTitle : ""));
    $("#infoGuest").text((reservation.guestName || detailAuth.name || "투숙객") + " · 성인 " + (reservation.adults || 1) + "명" + ((reservation.children || 0) > 0 ? " · 어린이 " + reservation.children + "명" : ""));
    $("#infoRequest").text(reservation.specialRequests || "요청 사항 없음");
    $("#guideParking").text(formatParking(reservation.roomParking));

    $("#payOriginal").text(formatDetailWon(reservation.originalAmount));
    $("#payDiscount").text("-" + formatDetailWon(reservation.discountAmount || reservation.couponDiscount || 0));
    $("#payPoint").text("-" + formatDetailWon(reservation.pointDiscount || 0));
    $("#payTotal").text(formatDetailWon(reservation.totalAmount));

    $("#detailQrText").text(reservation.checkInQr || reservationNumber);
    renderDetailQr(reservation.checkInQr || reservationNumber);
    renderPolicies(reservation);
    $("#cancelReservationBtn").toggle(status.key === "upcoming");
}

function loadHotelExtras(hotelId) {
    if (!hotelId) {
        renderAmenities([]);
        return;
    }

    $.ajax({
        url: DETAIL_API_BASE + "/hotel/iname/" + hotelId,
        success: function (response) {
            renderAmenities(response.data || []);
        },
        error: function () {
            renderAmenities([]);
        }
    });
}

function goDetailHotelPage() {
    if (!detailReservation || !detailReservation.hotelId) {
        alert("호텔 상세 정보를 찾을 수 없습니다.");
        return;
    }

    location.href = "hotel-detail.html?id=" + encodeURIComponent(detailReservation.hotelId);
}

function renderAmenities(items) {
    const fallback = [
        { title: "무료 Wi-Fi" },
        { title: "발렛파킹" },
        { title: "조식 포함" }
    ];
    const target = $("#amenityTags").empty();
    (items.length ? items : fallback).slice(0, 8).forEach(function (item) {
        target.append("<span>" + escapeDetailHtml(item.title || "편의시설") + "</span>");
    });
}

function renderPolicies(reservation) {
    const list = $("#policyList").empty();
    const policies = reservation.cancellationPolicyDto || [];

    if (!policies.length) {
        list.append('<div class="policy-row active"><strong>무료 취소 가능</strong><span>체크인 3일 전까지 전액 환불 가능</span></div>');
        list.append('<div class="policy-row"><strong>부분 환불</strong><span>체크인 1~2일 전 취소 시 50% 환불</span></div>');
        list.append('<div class="policy-row"><strong>환불 불가</strong><span>체크인 당일 취소 또는 노쇼</span></div>');
        return;
    }

    policies.forEach(function (policy) {
        list.append(
            '<div class="policy-row ' + (policy.applicable ? "active" : "") + '">' +
            '<strong>' + escapeDetailHtml(policy.periodDescription || "취소 정책") + '</strong>' +
            '<span>' + (policy.refundPercentage || 0) + '% 환불 · 예상 ' + formatDetailWon(policy.expectedRefundAmount || 0) + '</span>' +
            '</div>'
        );
    });
}

function cancelDetailReservation() {
    if (!detailReservation || !confirm("이 예약을 취소할까요?")) return;

    $.ajax({
        url: DETAIL_API_BASE + "/reservation/cancel",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ sid: detailReservation.sid, cancelReason: "고객 직접 취소" }),
        success: function () {
            location.href = "my-reservations.html";
        },
        error: function () { alert("예약 취소에 실패했습니다."); }
    });
}

function renderDetailQr(value) {
    const target = document.getElementById("detailQrBox");
    if (!target || !value || typeof QRCode === "undefined") return;
    target.innerHTML = "";
    new QRCode(target, {
        text: value,
        width: 154,
        height: 154,
        colorDark: "#111827",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.M
    });
    target.style.background = "#fff";
}

function renderDetailProfile() {
    const name = detailAuth.name || "회원";
    $("#profileInitial").text(name.slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(detailAuth.email || "");
    $("#profilePoint").text(formatDetailNumber(detailAuth.point || 0));
}

function loadLatestDetailProfile() {
    $.ajax({
        url: DETAIL_API_BASE + "/member/" + detailAuth.memberSid,
        type: "GET",
        success: function (result) {
            const member = result.data || {};
            detailAuth = Object.assign({}, detailAuth, {
                name: member.name || detailAuth.name,
                email: member.email || detailAuth.email,
                point: Number(member.point || 0)
            });
            saveDetailAuth(detailAuth);
            renderDetailProfile();
        }
    });
}

function loadDetailReservationCount() {
    $.ajax({
        url: DETAIL_API_BASE + "/reservation/search",
        type: "GET",
        data: {
            memberId: detailAuth.memberSid,
            page: 0,
            size: 1
        },
        success: function (page) {
            $("#totalReservationCount").text(formatDetailNumber(page.totalElements || 0));
        },
        error: function () {
            $("#totalReservationCount").text("0");
        }
    });
}

function mergeDetailCompleted(data) {
    const r = data.reservation || {};
    const h = data.hotel || {};
    const room = data.room || {};
    return Object.assign({}, r, {
        hotelId: h.sid || h.hotelId,
        hotelName: h.hotelName,
        hotelLocation: h.location,
        hotelStarRating: h.starRating,
        roomName: room.roomName,
        roomTypeTitle: room.roomTypeTitle,
        roomParking: room.parking
    });
}

function mergeDetailLocal(reservation) {
    const completed = readDetailJson("completedReservation");
    if (completed && completed.reservation && Number(completed.reservation.sid) === Number(reservation.sid)) {
        return Object.assign({}, mergeDetailCompleted(completed), reservation);
    }
    return reservation;
}

function getDetailState(reservation) {
    const status = reservation.reservationStatus;
    if (status === "CANCELLED" || status === "NO_SHOW") return { key: "cancelled", label: "취소됨" };
    if (status === "CHECKED_OUT" || status === "COMPLETED") return { key: "completed", label: "완료된 여행" };
    return { key: "upcoming", label: "예정된 여행" };
}

function formatParking(value) {
    if (value === true || value === "true" || value === "Y" || value === "YES" || value === "AVAILABLE") return "주차 가능";
    if (value === false || value === "false" || value === "N" || value === "NO" || value === "UNAVAILABLE") return "주차 불가";
    return value || "확인 필요";
}

function readDetailJson(key) {
    try {
        const value = sessionStorage.getItem(key) || localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function saveDetailAuth(auth) {
    const storage = localStorage.getItem("staynowAuth") ? localStorage : sessionStorage;
    storage.setItem("staynowAuth", JSON.stringify(auth));
}

function logoutDetail() {
    localStorage.removeItem("staynowAuth");
    sessionStorage.removeItem("staynowAuth");
    location.href = "index.html";
}

function formatDetailDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    return date.getFullYear() + "년 " + (date.getMonth() + 1) + "월 " + date.getDate() + "일 (" + days[date.getDay()] + ")";
}

function formatDetailWon(value) {
    return "₩" + formatDetailNumber(value || 0);
}

function formatDetailNumber(value) {
    return Number(value || 0).toLocaleString();
}

function drawDetailStars(count) {
    const value = Number(count || 0);
    return "★".repeat(value) + "☆".repeat(Math.max(0, 5 - value));
}

function escapeDetailHtml(value) {
    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
