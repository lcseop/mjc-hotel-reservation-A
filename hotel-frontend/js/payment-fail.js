const API_BASE = window.StayNowConfig.apiBase;

$(function () {
    const params = new URLSearchParams(location.search);
    const orderId = params.get("orderId");
    const reservationId = params.get("reservationId");
    const code = params.get("code") || "PAYMENT_FAILED";
    const message = params.get("message") || "결제가 취소되었거나 실패했습니다.";

    $("#failMessage").text(message);

    if (!orderId && !reservationId) {
        return;
    }

    if (!orderId && reservationId) {
        $.ajax({
            url: API_BASE + "/reservation/" + encodeURIComponent(reservationId) + "/payment-cancel",
            type: "PATCH",
            contentType: "application/json",
            headers: authHeaders(),
            data: JSON.stringify({ reason: message })
        });
        return;
    }

    $.ajax({
        url: API_BASE + "/payments/toss/fail",
        type: "POST",
        contentType: "application/json",
        headers: authHeaders(),
        data: JSON.stringify({
            orderId,
            code,
            message
        })
    });
});

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
