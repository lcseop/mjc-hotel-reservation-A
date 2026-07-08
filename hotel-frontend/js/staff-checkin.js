const STAFF_API_BASE = "http://localhost:33000/api";
let scannerStream = null;
let scannerTimer = null;
let barcodeDetector = null;
let processingQr = false;

$(function () {
    const auth = getStaffAuth();

    if (!auth || !auth.token) {
        sessionStorage.setItem("afterLoginRedirect", "staff-checkin.html");
        alert("직원 체크인은 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    if (auth.role !== "ADMIN") {
        alert("관리자만 접근할 수 있는 페이지입니다.");
        location.href = "index.html";
        return;
    }

    $("#adminName").text((auth.name || auth.email || "관리자") + "님, 체크인 권한이 확인되었습니다.");
    initScannerSupport();
    bindStaffEvents();
});

function bindStaffEvents() {
    $("#startScanBtn").on("click", startScanner);
    $("#stopScanBtn").on("click", stopScanner);
    $("#submitQrBtn").on("click", function () {
        submitQrValue($("#qrValueInput").val());
    });

    $(window).on("beforeunload", stopScanner);
}

function initScannerSupport() {
    if ("BarcodeDetector" in window) {
        barcodeDetector = new BarcodeDetector({ formats: ["qr_code"] });
        $("#scannerSupport").text("카메라 스캔 가능");
    } else {
        $("#scannerSupport").text("수동 입력 권장");
        $("#startScanBtn").prop("disabled", true).css("opacity", ".6");
    }
}

function startScanner() {
    if (!barcodeDetector) {
        showResult("이 브라우저는 QR 자동 인식을 지원하지 않습니다. QR 문자열을 직접 입력해주세요.", true);
        return;
    }

    navigator.mediaDevices.getUserMedia({
        video: { facingMode: "environment" },
        audio: false
    }).then(function (stream) {
        scannerStream = stream;
        const video = document.getElementById("scannerVideo");
        video.srcObject = stream;
        video.play();
        $(".camera-box").addClass("active");
        scannerTimer = window.setInterval(scanFrame, 700);
    }).catch(function () {
        showResult("카메라를 시작하지 못했습니다. 브라우저 권한을 확인하거나 수동 입력을 사용해주세요.", true);
    });
}

function stopScanner() {
    if (scannerTimer) {
        window.clearInterval(scannerTimer);
        scannerTimer = null;
    }

    if (scannerStream) {
        scannerStream.getTracks().forEach(function (track) {
            track.stop();
        });
        scannerStream = null;
    }

    $(".camera-box").removeClass("active");
}

function scanFrame() {
    if (processingQr || !barcodeDetector) {
        return;
    }

    const video = document.getElementById("scannerVideo");

    if (!video || video.readyState < 2) {
        return;
    }

    barcodeDetector.detect(video).then(function (codes) {
        if (!codes.length) {
            return;
        }

        const value = codes[0].rawValue;
        $("#qrValueInput").val(value);
        submitQrValue(value);
    }).catch(function () {
        showResult("QR 인식 중 오류가 발생했습니다. 수동 입력을 사용해주세요.", true);
    });
}

function submitQrValue(rawValue) {
    const qrValue = String(rawValue || "").trim();

    if (!qrValue) {
        showResult("QR 값 또는 예약번호를 입력해주세요.", true);
        $("#qrValueInput").focus();
        return;
    }

    processingQr = true;
    $("#submitQrBtn").prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 처리 중');

    $.ajax({
        url: STAFF_API_BASE + "/reservation/check-in/qr",
        type: "PATCH",
        contentType: "application/json",
        headers: authHeaders(),
        data: JSON.stringify({ qrValue }),
        success: function (reservation) {
            stopScanner();
            showSuccess(reservation);
        },
        error: function (xhr) {
            showResult(getErrorMessage(xhr, "체크인 처리에 실패했습니다."), true);
        },
        complete: function () {
            processingQr = false;
            $("#submitQrBtn").prop("disabled", false).html('<i class="fa-solid fa-right-to-bracket"></i> 체크인 처리');
        }
    });
}

function showSuccess(reservation) {
    showResult(
        "<strong>체크인이 완료되었습니다.</strong><br>" +
        "예약번호: " + escapeHtml(reservation.reservationNumber || "-") + "<br>" +
        "호텔: " + escapeHtml(reservation.hotelName || "호텔명 없음") + "<br>" +
        "투숙객: " + escapeHtml(reservation.guestName || reservation.memberName || "투숙객") + "<br>" +
        "상태: " + escapeHtml(reservation.reservationStatus || "CHECKED_IN"),
        false
    );
}

function showResult(message, isError) {
    $("#checkinResult")
        .removeClass("empty error")
        .toggleClass("error", Boolean(isError))
        .html(message);
}

function authHeaders() {
    const auth = getStaffAuth();
    return auth && auth.token ? { Authorization: auth.token } : {};
}

function getStaffAuth() {
    try {
        const value = localStorage.getItem("staynowAuth") || sessionStorage.getItem("staynowAuth");
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function getErrorMessage(xhr, fallback) {
    if (xhr && xhr.responseJSON) {
        return xhr.responseJSON.message || xhr.responseJSON.error || fallback;
    }

    if (xhr && xhr.responseText) {
        return xhr.responseText;
    }

    return fallback;
}

function escapeHtml(value) {
    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
