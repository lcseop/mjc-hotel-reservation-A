const API_BASE = "http://localhost:33000/api";
const FALLBACK_IMAGE = "https://gunsancci.korcham.net/images/no-image01.gif";

let reservationState = null;

$(function () {
    reservationState = loadReservationState();

    if (!reservationState) {
        alert("선택한 객실 정보가 없습니다.");
        location.href = "hotel-search.html";
        return;
    }

    if (!reservationState.auth || !reservationState.auth.memberSid) {
        alert("예약을 진행하려면 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    bindReservationEvents();
    renderReservationPage();
});

function bindReservationEvents() {
    $("#changeRoomBtn").on("click", function (e) {
        e.preventDefault();
        location.href = "hotel-detail.html?id=" + reservationState.hotelId + "#rooms";
    });

    $("#sameAsMember").on("change", fillMemberInfo);

    $("#specialRequests").on("input", function () {
        $("#requestCount").text($(this).val().length);
    });

    $("#payBtn").on("click", submitReservation);
}

function loadReservationState() {
    const selected = readJson("selectedReservationRoom");
    const auth = getAuthData();

    if (!selected || !selected.roomId || !selected.hotelId) {
        return null;
    }

    return {
        auth,
        hotelId: selected.hotelId,
        roomId: selected.roomId,
        hotel: selected.hotel || {},
        room: selected.room || {},
        searchRequest: selected.searchRequest || readJson("hotelSearchRequest") || {}
    };
}

function renderReservationPage() {
    const hotel = reservationState.hotel;
    const room = reservationState.room;
    const stay = getStayInfo();
    const price = getPriceInfo();

    $("#roomImage").attr("src", normalizeImagePath(room.roomPhotoPath) || FALLBACK_IMAGE);
    $("#roomType").text(room.roomTypeTitle || "객실");
    $("#roomName, #summaryRoom").text(room.roomName || "객실명 없음");
    $("#roomSpecs").text(makeRoomSpec(room));
    $("#parkingText").text(formatPolicy(room.parking));
    $("#smokeText").text(formatPolicy(room.smoke));
    $("#idCardText").text(formatPolicy(room.idCard) || "체크인 시 확인");

    $("#checkInText, #summaryCheckIn").text(formatDateTime(stay.checkIn));
    $("#checkOutText, #summaryCheckOut").text(formatDateTime(stay.checkOut));
    $("#nightText, #summaryNights").text(stay.nights + "박");
    $("#summaryGuests").text("성인 " + stay.adults + "명 · 어린이 " + stay.children + "명");

    $("#summaryHotel").text(hotel.hotelName || "호텔명 없음");
    $("#summaryLocation").text(hotel.location || "위치 정보 없음");
    $("#originalAmount").text(formatWon(price.originalAmount));
    $("#taxAmount").text(formatWon(price.taxAmount));
    $("#totalAmount").text(formatWon(price.totalAmount));

    fillMemberInfo();
}

function fillMemberInfo() {
    if (!$("#sameAsMember").is(":checked")) {
        return;
    }

    const auth = reservationState.auth || {};
    $("#guestName").val(auth.name || "");
    $("#guestEmail").val(auth.email || "");
}

function submitReservation() {
    const validationMessage = validateReservationForm();

    if (validationMessage) {
        alert(validationMessage);
        return;
    }

    const stay = getStayInfo();
    const price = getPriceInfo();
    const auth = reservationState.auth;
    const reservationPayload = {
        sid: Date.now(),
        memberId: Number(auth.memberSid),
        roomId: Number(reservationState.roomId),
        couponIssueId: null,
        checkInDate: stay.checkIn,
        checkOutDate: stay.checkOut,
        adults: stay.adults,
        children: stay.children,
        specialRequests: $("#specialRequests").val().trim(),
        guestName: $("#guestName").val().trim(),
        usePoint: 0,
        reservationChannel: "DIRECT"
    };

    setPaymentLoading(true);

    setTimeout(function () {
        createReservation(reservationPayload)
            .then(function (reservation) {
                return createPayment(reservation, reservation.totalAmount || price.totalAmount)
                    .then(function (payment) {
                        return {
                            reservation,
                            payment
                        };
                    })
                    .catch(function () {
                        return {
                            reservation,
                            payment: null
                        };
                    });
            })
            .then(function (result) {
                const completeData = {
                    hotel: reservationState.hotel,
                    room: reservationState.room,
                    guest: {
                        name: $("#guestName").val().trim(),
                        phone: $("#guestPhone").val().trim(),
                        email: $("#guestEmail").val().trim(),
                        nationality: $("#nationality").val()
                    },
                    reservation: result.reservation,
                    payment: result.payment,
                    price,
                    stay
                };

                sessionStorage.setItem("completedReservation", JSON.stringify(completeData));
                location.href = "reservation-complete.html";
            })
            .catch(function (xhr) {
                setPaymentLoading(false);
                alert(getErrorMessage(xhr, "예약 처리 중 오류가 발생했습니다."));
            });
    }, 900);
}

function createReservation(payload) {
    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_BASE + "/reservation",
            type: "POST",
            contentType: "application/json",
            headers: authHeaders(),
            data: JSON.stringify(payload),
            success: resolve,
            error: reject
        });
    });
}

function createPayment(reservation, totalAmount) {
    const auth = reservationState.auth;
    const payload = {
        sid: Number(auth.memberSid),
        reservationId: reservation.sid,
        paymentAmount: totalAmount,
        paymentMethod: "CARD",
        paymentStatus: "COMPLETED",
        transactionNo: "MOCK-" + Date.now(),
        paidAt: new Date().toISOString().slice(0, 19),
        point: Math.floor(totalAmount * 0.01)
    };

    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_BASE + "/payments/add",
            type: "POST",
            contentType: "application/json",
            headers: authHeaders(),
            data: JSON.stringify(payload),
            success: function (result) {
                resolve(result.data || result);
            },
            error: reject
        });
    });
}

function validateReservationForm() {
    if (!$("#guestName").val().trim()) {
        $("#guestName").focus();
        return "투숙객 이름을 입력해주세요.";
    }

    if (!$("#guestPhone").val().trim()) {
        $("#guestPhone").focus();
        return "연락처를 입력해주세요.";
    }

    if (!$("#guestEmail").val().trim()) {
        $("#guestEmail").focus();
        return "이메일을 입력해주세요.";
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test($("#guestEmail").val().trim())) {
        $("#guestEmail").focus();
        return "올바른 이메일 형식이 아닙니다.";
    }

    return "";
}

function setPaymentLoading(loading) {
    $("#paymentOverlay").toggleClass("show", loading).attr("aria-hidden", String(!loading));
    $("#payBtn").prop("disabled", loading).html(
        loading
            ? '<i class="fa-solid fa-spinner fa-spin"></i> 결제 승인 중'
            : '<i class="fa-regular fa-credit-card"></i> 결제하기'
    );
}

function getStayInfo() {
    const request = reservationState.searchRequest || {};
    const checkIn = request.checkIn || makeDefaultDateTime(1, "15:00:00");
    const checkOut = request.checkOut || makeDefaultDateTime(2, "11:00:00");
    const nights = Math.max(1, Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000));

    return {
        checkIn,
        checkOut,
        nights,
        adults: Number(request.adult || 2),
        children: Number(request.child || 0)
    };
}

function getPriceInfo() {
    const stay = getStayInfo();
    const roomPrice = Number(reservationState.room.roomPrice || reservationState.hotel.hotelPrice || 0);
    const originalAmount = roomPrice * stay.nights;
    const taxAmount = Math.round(originalAmount * 0.1);

    return {
        roomPrice,
        originalAmount,
        taxAmount,
        totalAmount: originalAmount + taxAmount
    };
}

function makeDefaultDateTime(offset, time) {
    const date = new Date();
    date.setDate(date.getDate() + offset);
    return date.toISOString().slice(0, 10) + "T" + time;
}

function makeRoomSpec(room) {
    const values = [
        room.area ? room.area + "㎡" : null,
        room.maximumPeople ? "최대 " + room.maximumPeople + "명" : null,
        room.roomNumber ? "객실 " + room.roomNumber + "호" : null,
        room.floor ? room.floor + "층" : null
    ].filter(Boolean);

    return values.length ? values.join(" · ") : "객실 상세 정보 없음";
}

function formatPolicy(value) {
    if (value === true || value === "true" || value === "Y" || value === "YES" || value === "AVAILABLE") {
        return "가능";
    }

    if (value === false || value === "false" || value === "N" || value === "NO" || value === "UNAVAILABLE") {
        return "불가";
    }

    return value || "확인 필요";
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const days = ["일", "월", "화", "수", "목", "금", "토"];
    return date.getFullYear() + "." +
        pad(date.getMonth() + 1) + "." +
        pad(date.getDate()) + " (" + days[date.getDay()] + ")";
}

function normalizeImagePath(path) {
    if (!path) {
        return "";
    }

    if (/^https?:\/\//.test(path)) {
        return path;
    }

    return API_BASE.replace("/api", "") + "/" + String(path).replace(/^\/+/, "");
}

function formatWon(value) {
    return "₩" + Number(value || 0).toLocaleString();
}

function pad(value) {
    return String(value).padStart(2, "0");
}

function authHeaders() {
    const auth = reservationState && reservationState.auth;
    return auth && auth.token ? { Authorization: auth.token } : {};
}

function getAuthData() {
    return readJson("staynowAuth");
}

function readJson(key) {
    try {
        const value = sessionStorage.getItem(key) || localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function getErrorMessage(xhr, fallback) {
    if (xhr && xhr.responseJSON) {
        return xhr.responseJSON.message || xhr.responseJSON.data || fallback;
    }

    if (xhr && xhr.responseText) {
        return xhr.responseText;
    }

    return fallback;
}
