$(function () {
    const data = readJson("completedReservation");

    if (!data || !data.reservation) {
        alert("완료된 예약 정보가 없습니다.");
        location.href = "index.html";
        return;
    }

    renderComplete(data);

    $("#printBtn").on("click", function () {
        window.print();
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
    $("#qrNumber").text(reservation.checkInQr || reservationNumber);

    $("#completeStars").text(drawStars(hotel.starRating) + " " + (hotel.starRating || 0) + "성급");
    $("#completeHotel").text(hotel.hotelName || "호텔명 없음");
    $("#completeLocation, #guideAddress").text(hotel.location || "위치 정보 없음");

    $("#detailCheckIn").text(formatDateTime(reservation.checkInDate || stay.checkIn));
    $("#detailCheckOut").text(formatDateTime(reservation.checkOutDate || stay.checkOut));
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
}

function drawStars(count) {
    const value = Number(count || 0);
    return "★".repeat(value) + "☆".repeat(Math.max(0, 5 - value));
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
    return date.getFullYear() + "년 " +
        pad(date.getMonth() + 1) + "월 " +
        pad(date.getDate()) + "일 (" + days[date.getDay()] + ")";
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
