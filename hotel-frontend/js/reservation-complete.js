$(function () {
    const data = readJson("completedReservation");

    if (!data || !data.reservation) {
        alert("완료된 예약 정보가 없습니다.");
        location.href = "index.html";
        return;
    }

    renderComplete(data);
    launchConfetti();

    $("#printBtn").on("click", function () {
        window.print();
    });

    $("#copyReservationBtn").on("click", function () {
        copyText($("#detailReservationNumber").text());
    });
});

function renderComplete(data) {
    const hotel = data.hotel || {};
    const room = data.room || {};
    const reservation = data.reservation || {};
    const guest = data.guest || {};
    const stay = data.stay || {};
    const reservationNumber = reservation.reservationNumber || "예약번호 확인 중";
    const totalAmount = reservation.totalAmount || (data.price && data.price.totalAmount) || 0;

    $("#completeMessage").text(
        (guest.name || reservation.guestName || "고객") + "님, " +
        (hotel.hotelName || "선택하신 호텔") + " 예약이 성공적으로 완료되었습니다."
    );
    $("#completeReservationNumber, #detailReservationNumber").text(reservationNumber);
    const qrValue = reservation.checkInQr || reservationNumber;
    $("#qrNumber").text(qrValue);
    renderQrCode(qrValue);

    $("#completeStars").text(drawStars(hotel.starRating) + " " + (hotel.starRating || 0) + "성급");
    $("#completeHotel").text(hotel.hotelName || "호텔명 없음");
    $("#completeLocation, #guideAddress").text(hotel.location || "위치 정보 없음");

    const checkIn = reservation.checkInDate || stay.checkIn;
    const checkOut = reservation.checkOutDate || stay.checkOut;

    $("#detailCheckIn").text(formatDate(checkIn));
    $("#detailCheckOut").text(formatDate(checkOut));
    $("#detailCheckInTime").text(formatTime(checkIn, "오후 3:00 이후"));
    $("#detailCheckOutTime").text(formatTime(checkOut, "오전 11:00 이전"));
    $("#guideTime").text(formatTime(checkIn, "오후 3:00") + " / " + formatTime(checkOut, "오전 11:00"));
    $("#detailNights").text((reservation.totalNights || stay.nights || 1) + "박");
    $("#detailRoom").text(room.roomName || "객실명 없음");
    $("#detailGuest").text(
        (guest.name || reservation.guestName || "투숙객") +
        " · 성인 " + (reservation.adults || stay.adults || 1) +
        "명" +
        ((reservation.children || stay.children || 0) > 0 ? " · 어린이 " + (reservation.children || stay.children) + "명" : "")
    );
    $("#detailAmount").text(formatWon(totalAmount));

    if (data.payment && data.payment.transactionNo) {
        $("#detailPayment").text("카드 테스트 결제 · " + data.payment.transactionNo);
    }

    $("#guideParking").text(formatParking(room.parking));
}

function launchConfetti() {
    const layer = document.getElementById("confettiLayer");

    if (!layer) {
        return;
    }

    const colors = ["#38bdf8", "#6366f1", "#f59e0b", "#fb7185", "#34d399", "#a78bfa"];
    const shapes = ["", "round", "ribbon"];
    const count = 34;

    layer.innerHTML = "";

    for (let i = 0; i < count; i++) {
        const piece = document.createElement("span");
        const angle = (Math.PI * 2 * i) / count;
        const distance = 95 + Math.random() * 180;
        const x = Math.cos(angle) * distance;
        const y = Math.sin(angle) * distance - 80 - Math.random() * 70;
        const shape = shapes[i % shapes.length];

        piece.className = "confetti-piece" + (shape ? " " + shape : "");
        piece.style.setProperty("--confetti-color", colors[i % colors.length]);
        piece.style.setProperty("--confetti-x", x.toFixed(0) + "px");
        piece.style.setProperty("--confetti-y", y.toFixed(0) + "px");
        piece.style.setProperty("--confetti-rotate", (180 + Math.random() * 540).toFixed(0) + "deg");
        piece.style.setProperty("--confetti-delay", (Math.random() * 120).toFixed(0) + "ms");
        layer.appendChild(piece);
    }

    setTimeout(function () {
        layer.innerHTML = "";
    }, 1700);
}

function renderQrCode(value) {
    const target = document.getElementById("qrBox");

    if (!target || !value || typeof QRCode === "undefined") {
        return;
    }

    target.innerHTML = "";

    new QRCode(target, {
        text: value,
        width: 148,
        height: 148,
        colorDark: "#111827",
        colorLight: "#ffffff",
        correctLevel: QRCode.CorrectLevel.M
    });

    target.classList.add("ready");
    $("#qrFallback").addClass("hidden");
}

function drawStars(count) {
    const value = Number(count || 0);
    return "★".repeat(value) + "☆".repeat(Math.max(0, 5 - value));
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const days = ["일", "월", "화", "수", "목", "금", "토"];
    return date.getFullYear() + "년 " +
        pad(date.getMonth() + 1) + "월 " +
        pad(date.getDate()) + "일 (" + days[date.getDay()] + ")";
}

function formatTime(value, fallback) {
    if (!value) {
        return fallback;
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return fallback;
    }

    const hours = date.getHours();
    const minutes = date.getMinutes();
    const period = hours >= 12 ? "오후" : "오전";
    const displayHours = hours % 12 || 12;

    return period + " " + displayHours + ":" + pad(minutes);
}

function formatParking(value) {
    if (value === true || value === "true" || value === "Y" || value === "YES" || value === "AVAILABLE" || value === "가능") {
        return "주차 가능";
    }

    if (value === false || value === "false" || value === "N" || value === "NO" || value === "UNAVAILABLE" || value === "불가") {
        return "주차 불가";
    }

    return value || "호텔 정책 확인 필요";
}

function copyText(value) {
    if (!value || value === "-") {
        return;
    }

    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(value).then(showCopyDone);
        return;
    }

    const textarea = document.createElement("textarea");
    textarea.value = value;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
    showCopyDone();
}

function showCopyDone() {
    const button = $("#copyReservationBtn");
    const original = button.html();

    button.html('<i class="fa-solid fa-check"></i> 복사됨');

    setTimeout(function () {
        button.html(original);
    }, 1400);
}

function formatWon(value) {
    return "₩" + Number(value || 0).toLocaleString();
}

function pad(value) {
    return String(value).padStart(2, "0");
}

function readJson(key) {
    try {
        const value = sessionStorage.getItem(key) || localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}
