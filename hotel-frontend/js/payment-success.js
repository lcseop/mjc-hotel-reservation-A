const API_BASE = window.StayNowConfig.apiBase;

$(function () {
    confirmTossPayment();
});

function confirmTossPayment() {
    const params = new URLSearchParams(location.search);
    const paymentKey = params.get("paymentKey");
    const orderId = params.get("orderId");
    const amount = Number(params.get("amount") || 0);
    const reservationId = Number(params.get("reservationId") || 0);
    const pending = readJson("pendingTossReservation");

    if (!paymentKey || !orderId || !amount || !reservationId) {
        showError("결제 승인 정보가 올바르지 않습니다.");
        return;
    }

    $.ajax({
        url: API_BASE + "/payments/toss/confirm",
        type: "POST",
        contentType: "application/json",
        headers: authHeaders(),
        data: JSON.stringify({
            reservationId,
            paymentKey,
            orderId,
            amount
        }),
        success: function (result) {
            const payment = result && Object.prototype.hasOwnProperty.call(result, "data") ? result.data : result;
            const completeData = pending || {};
            completeData.payment = payment;

            if (!completeData.reservation) {
                completeData.reservation = { sid: reservationId, totalAmount: amount };
            }

            sessionStorage.setItem("completedReservation", JSON.stringify(completeData));
            sessionStorage.removeItem("pendingTossReservation");
            location.replace("reservation-complete.html");
        },
        error: function (xhr) {
            showError(getErrorMessage(xhr, "결제 승인 처리 중 오류가 발생했습니다."));
        }
    });
}

function showError(message) {
    $(".payment-result-card").addClass("fail").html(`
        <h1>결제 승인에 실패했습니다</h1>
        <p>${escapeHtml(message)}</p>
        <div class="payment-result-actions">
            <a href="reservation.html">다시 결제하기</a>
            <a href="index.html">홈으로 이동</a>
        </div>
    `);
}

function authHeaders() {
    const auth = readJson("staynowAuth");
    return auth && auth.token ? { Authorization: auth.token } : {};
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
        const json = xhr.responseJSON;
        return json.message || json.data || json.error || json.detail || fallback;
    }

    if (xhr && xhr.responseText) {
        try {
            const parsed = JSON.parse(xhr.responseText);
            return parsed.message || parsed.data || parsed.error || parsed.detail || fallback;
        } catch (e) {
            return xhr.responseText;
        }
    }

    return fallback;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
