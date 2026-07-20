const API_BASE = window.StayNowConfig.apiBase;
const FALLBACK_IMAGE = "https://gunsancci.korcham.net/images/no-image01.gif";

let reservationState = null;
let availableCoupons = [];
let selectedCouponIssueId = null;
let availablePoint = 0;
let couponDragMoved = false;
let tossWidgets = null;
let tossWidgetAmount = null;
let tossAmountPromise = Promise.resolve();
let tossPaymentWindow = null;

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

    $("#guestPhone").on("input", function () {
        $(this).val(formatPhoneNumber($(this).val()));
    });

    $("#specialRequests").on("input", function () {
        $("#requestCount").text($(this).val().length);
    });

    $("#agreeAll").on("change", function () {
        $(".required-agreement").prop("checked", this.checked);
    });

    $(".required-agreement").on("change", syncAgreementAll);

    $("#payBtn").on("click", submitReservation);

    $("#clearCouponBtn").on("click", function () {
        selectedCouponIssueId = null;
        renderCoupons();
        renderPriceSummary();
    });

    $(document).on("click", ".coupon-card:not(.disabled)", function (event) {
        if (couponDragMoved) {
            event.preventDefault();
            couponDragMoved = false;
            return;
        }
        selectedCouponIssueId = Number($(this).data("couponIssueId")) || null;
        renderCoupons();
        renderPriceSummary();
    });

    $("#usePointInput").on("input", function () {
        const price = getPriceInfo();
        const maxPoint = getMaxUsablePoint(price);
        const value = Math.min(maxPoint, Math.max(0, Number($(this).val()) || 0));
        $(this).val(value);
        renderPriceSummary();
    });

    $("#useAllPointBtn").on("click", function () {
        if ($(this).prop("disabled")) return;
        const price = getPriceInfo();
        $("#usePointInput").val(getMaxUsablePoint(price));
        renderPriceSummary();
    });

    bindCouponDragScroll();
}

function bindCouponDragScroll() {
    let dragging = false;
    let startX = 0;
    let startScrollLeft = 0;

    $(document).on("mousedown", ".coupon-list", function (event) {
        dragging = true;
        couponDragMoved = false;
        startX = event.pageX;
        startScrollLeft = this.scrollLeft;
        $(this).addClass("dragging");
    });

    $(document).on("mousemove", function (event) {
        if (!dragging) return;
        const $list = $(".coupon-list.dragging");
        if (!$list.length) return;
        const distance = event.pageX - startX;
        if (Math.abs(distance) > 5) {
            couponDragMoved = true;
        }
        $list[0].scrollLeft = startScrollLeft - distance;
    });

    $(document).on("mouseup mouseleave", function () {
        if (!dragging) return;
        dragging = false;
        $(".coupon-list.dragging").removeClass("dragging");
        setTimeout(function () {
            couponDragMoved = false;
        }, 0);
    });
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
        amenities: selected.amenities || [],
        searchRequest: selected.searchRequest || readJson("hotelSearchRequest") || {}
    };
}

function renderReservationPage() {
    const hotel = reservationState.hotel;
    const room = reservationState.room;
    const stay = getStayInfo();
    const price = getPriceInfo();

    $("#roomImage").attr("src", normalizeImagePath(room.roomPhotoPath) || FALLBACK_IMAGE);
    $("#roomName, #summaryRoom").text(room.roomName || "객실명 없음");
    $("#roomSpecs").text(makeRoomSpec(room));
    drawRoomPills(room);
    $("#parkingText").text(formatPolicy(room.parking));
    $("#smokeText").text(formatSmokePolicy(room.smoke));
    $("#idCardText").text(formatIdCardPolicy(room.idCard));

    $("#checkInText, #summaryCheckIn").text(formatDateTime(stay.checkIn));
    $("#checkOutText, #summaryCheckOut").text(formatDateTime(stay.checkOut));
    $("#nightText, #summaryNights").text(stay.nights + "박");
    $("#summaryGuests").text("성인 " + stay.adults + "명 · 어린이 " + stay.children + "명");

    $("#summaryHotel").text(hotel.hotelName || "호텔명 없음");
    $("#summaryLocation").text(hotel.location || "위치 정보 없음");
    $("#availablePoint").text("조회 중");
    $("#usePointInput, #useAllPointBtn").prop("disabled", true);
    renderPriceSummary();

    fillMemberInfo();
    loadAvailableCoupons();
}

function fillMemberInfo() {
    const auth = reservationState.auth || {};
    $("#guestName").val(auth.name || "");
    $("#guestEmail").val(auth.email || "");

    if (auth.phone) $("#guestPhone").val(formatPhoneNumber(auth.phone));

    if (auth.memberSid) {
        $.ajax({
            url: API_BASE + "/member/" + auth.memberSid,
            type: "GET",
            headers: authHeaders(),
            success: function (member) {
                const data = member && (member.data || member);

                if (!data) {
                    return;
                }

                $("#guestName").val($("#guestName").val() || data.name || "");
                $("#guestEmail").val($("#guestEmail").val() || data.email || "");
                $("#guestPhone").val(formatPhoneNumber(data.phone || ""));
                availablePoint = Number(data.point || 0);
                $("#availablePoint").text(formatNumber(availablePoint) + "P");
                $("#usePointInput, #useAllPointBtn").prop("disabled", false);
                updateStoredAuthPhone(data.phone);
                renderPriceSummary();
            },
            error: function (xhr) {
                $("#availablePoint").text("조회 실패");
                availablePoint = 0;
                $("#usePointInput").val(0);
                $("#usePointInput, #useAllPointBtn").prop("disabled", true);
                renderPriceSummary();
                console.warn("Member point load failed", xhr && (xhr.responseJSON || xhr.responseText || xhr.statusText));
            }
        });
    }
}

function loadAvailableCoupons() {
    const auth = reservationState.auth || {};
    if (!auth.memberSid) {
        availableCoupons = [];
        renderCoupons();
        return;
    }

    $.ajax({
        url: API_BASE + "/cou/member/" + auth.memberSid,
        type: "GET",
        headers: authHeaders(),
        success: function (result) {
            availableCoupons = Array.isArray(result && result.data) ? result.data : [];
            renderCoupons();
            renderPriceSummary();
        },
        error: function (xhr) {
            availableCoupons = [];
            $("#couponList").html('<div class="coupon-empty">' + escapeHtml(getErrorMessage(xhr, "쿠폰을 불러오지 못했습니다.")) + '</div>');
        }
    });
}

function renderCoupons() {
    const price = getPriceInfo();

    if (!availableCoupons.length) {
        $("#couponList").html('<div class="coupon-empty">사용 가능한 쿠폰이 없습니다.</div>');
        $("#clearCouponBtn").prop("disabled", true);
        return;
    }

    $("#clearCouponBtn").prop("disabled", !selectedCouponIssueId);
    $("#couponList").html(availableCoupons.map(function (coupon) {
        const discount = calculateCouponDiscount(coupon, price.beforeCouponAmount);
        const minOrder = Number(coupon.minOrderAmount || 0);
        const disabled = minOrder > price.beforeCouponAmount;
        const selected = String(selectedCouponIssueId) === String(coupon.sid);
        return `<button type="button" class="coupon-card ${selected ? "active" : ""} ${disabled ? "disabled" : ""}" data-coupon-issue-id="${coupon.sid}">
            <strong>${escapeHtml(coupon.couponName || "쿠폰")}</strong>
            <span>${formatCouponDiscount(coupon)} 할인</span>
            <small>${disabled ? formatWon(minOrder) + " 이상 사용 가능" : "예상 할인 " + formatWon(discount)}</small>
        </button>`;
    }).join(""));
}

function renderPriceSummary() {
    const price = getPriceInfo();
    const maxPoint = getMaxUsablePoint(price);
    const pointValue = Math.min(maxPoint, Math.max(0, Number($("#usePointInput").val()) || 0));

    if (String($("#usePointInput").val()) !== String(pointValue)) {
        $("#usePointInput").val(pointValue);
    }

    $("#originalAmount").text(formatWon(price.originalAmount));
    if (price.promotionDiscountAmount > 0) {
        $("#promotionDiscountRow").show();
        $("#promotionDiscountAmount").text("-" + formatWon(price.promotionDiscountAmount));
    } else {
        $("#promotionDiscountRow").hide();
    }

    if (price.couponDiscountAmount > 0) {
        $("#couponDiscountRow").show();
        $("#couponDiscountAmount").text("-" + formatWon(price.couponDiscountAmount));
    } else {
        $("#couponDiscountRow").hide();
    }

    if (pointValue > 0) {
        $("#pointDiscountRow").show();
        $("#pointDiscountAmount").text("-" + formatWon(pointValue));
    } else {
        $("#pointDiscountRow").hide();
    }

    $("#taxAmount").text(price.taxAmount > 0 ? formatWon(price.taxAmount) : "포함");
    $("#totalAmount").text(formatWon(Math.max(0, price.totalAmount - pointValue)));
}

function drawRoomPills(room) {
    const pills = [];
    const amenities = reservationState.amenities || [];

    pills.push(room.roomTypeTitle || "객실");

    if (hasAmenity(amenities, ["무료취소", "무료 취소", "free cancel", "free cancellation"])) {
        pills.push("무료취소");
    }

    if (hasAmenity(amenities, ["조식", "아침", "breakfast"])) {
        pills.push("조식포함");
    }

    $("#roomPills").empty();

    pills.forEach(function (pill) {
        $("#roomPills").append($("<span>").text(pill));
    });

    $("#summaryRoomTags").text(pills.slice(1).length > 0 ? pills.slice(1).join(" · ") : "추가 혜택 없음");
}

function hasAmenity(amenities, keywords) {
    return amenities.some(function (amenity) {
        const title = String(amenity.title || "").toLowerCase().replaceAll(" ", "");
        const description = String(amenity.description || "").toLowerCase().replaceAll(" ", "");

        return keywords.some(function (keyword) {
            const normalized = keyword.toLowerCase().replaceAll(" ", "");
            return title.includes(normalized) || description.includes(normalized);
        });
    });
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
        couponIssueId: selectedCouponIssueId,
        checkInDate: stay.checkIn,
        checkOutDate: stay.checkOut,
        adults: stay.adults,
        children: stay.children,
        specialRequests: $("#specialRequests").val().trim(),
        guestName: $("#guestName").val().trim(),
        usePoint: Math.min(getMaxUsablePoint(price), Math.max(0, Number($("#usePointInput").val()) || 0)),
        reservationChannel: "DIRECT"
    };

    setPaymentLoading(true);

    setTimeout(function () {
        createReservation(reservationPayload)
            .then(function (reservation) {
                const paymentAmount = firstReservationAmount(reservation.totalAmount, price.totalAmount);
                const orderName = makeOrderName(reservationState.hotel, reservationState.room);
                const completeData = {
                    hotel: reservationState.hotel,
                    room: reservationState.room,
                    guest: {
                        name: $("#guestName").val().trim(),
                        phone: $("#guestPhone").val().trim(),
                        email: $("#guestEmail").val().trim()
                    },
                    reservation,
                    payment: null,
                    price,
                    stay
                };

                return createTossPaymentReady(reservation, paymentAmount, orderName)
                    .then(function (paymentReady) {
                        completeData.paymentReady = paymentReady;
                        sessionStorage.setItem("pendingTossReservation", JSON.stringify(completeData));
                        return requestTossPayment(paymentReady, completeData)
                            .catch(function (error) {
                                return notifyTossPaymentFailure(
                                    paymentReady.orderId,
                                    completeData.reservation.sid,
                                    "PAYMENT_WIDGET_ERROR",
                                    getErrorMessage(error, "결제창을 열지 못했습니다.")
                                ).then(function () {
                                    throw error;
                                });
                            });
                    });
            })
            .catch(function (xhr) {
                setPaymentLoading(false);
                alert(getErrorMessage(xhr, "예약 처리 중 오류가 발생했습니다."));
            });
    }, 900);
}

function firstReservationAmount() {
    for (let i = 0; i < arguments.length; i++) {
        const value = arguments[i];
        if (value !== null && value !== undefined && value !== "" && Number.isFinite(Number(value))) {
            return Number(value);
        }
    }
    return 0;
}

function createReservation(payload) {
    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_BASE + "/reservation",
            type: "POST",
            contentType: "application/json",
            headers: authHeaders(),
            data: JSON.stringify(payload),
            success: function (result) {
                resolve(result && Object.prototype.hasOwnProperty.call(result, "data") ? result.data : result);
            },
            error: reject
        });
    });
}

function createTossPaymentReady(reservation, totalAmount, orderName) {
    const auth = reservationState.auth;
    const payload = {
        memberId: Number(auth.memberSid),
        reservationId: reservation.sid,
        amount: totalAmount,
        orderName
    };

    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_BASE + "/payments/toss/ready",
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

function notifyTossPaymentFailure(orderId, reservationId, code, message) {
    if (!orderId && !reservationId) {
        return Promise.resolve();
    }

    return new Promise(function (resolve) {
        const payload = {
            code: code || "PAYMENT_FAILED",
            message: message || "결제가 취소되었거나 실패했습니다."
        };

        if (orderId) {
            $.ajax({
                url: API_BASE + "/payments/toss/fail",
                type: "POST",
                contentType: "application/json",
                headers: authHeaders(),
                data: JSON.stringify(Object.assign({ orderId }, payload)),
                complete: resolve
            });
            return;
        }

        $.ajax({
            url: API_BASE + "/reservation/" + encodeURIComponent(reservationId) + "/payment-cancel",
            type: "PATCH",
            contentType: "application/json",
            headers: authHeaders(),
            data: JSON.stringify({ reason: payload.message }),
            complete: resolve
        });
    });
}

function requestTossPayment(paymentReady, completeData) {
    const amount = Number(paymentReady.amount || 0);
    const successUrl = makePageUrl("payment-success.html") + "?reservationId=" + encodeURIComponent(completeData.reservation.sid);
    const failUrl = makePageUrl("payment-fail.html") + "?reservationId=" + encodeURIComponent(completeData.reservation.sid);
    const paymentRequest = {
        orderId: paymentReady.orderId,
        orderName: paymentReady.orderName || makeOrderName(completeData.hotel, completeData.room),
        successUrl,
        failUrl,
        customerEmail: completeData.guest.email,
        customerName: completeData.guest.name,
        customerMobilePhone: onlyDigits(completeData.guest.phone)
    };

    return ensureTossWidgets(amount).then(function (widgets) {
        setPaymentLoading(false);
        return openTossPaymentWindow(widgets, paymentRequest);
    });
}

function ensureTossWidgets(amount) {
    if (!window.TossPayments) {
        return Promise.reject(new Error("토스페이먼츠 SDK를 불러오지 못했습니다."));
    }

    const clientKey = window.StayNowConfig.tossClientKey;
    if (!clientKey) {
        return Promise.reject(new Error("토스 클라이언트 키가 설정되지 않았습니다."));
    }

    if (!tossWidgets) {
        const tossPayments = window.TossPayments(clientKey);
        tossWidgets = tossPayments.widgets({
            customerKey: getTossCustomerKey()
        });
    }

    const nextAmount = Math.max(0, Number(amount || 0));
    tossAmountPromise = tossAmountPromise.then(function () {
        return tossWidgets.setAmount({
            currency: "KRW",
            value: nextAmount
        });
    }).then(function () {
        tossWidgetAmount = nextAmount;
        return tossWidgets;
    });

    return tossAmountPromise;
}

function openTossPaymentWindow(widgets, paymentRequest) {
    if (!widgets || typeof widgets.renderPaymentWindow !== "function") {
        return Promise.reject(new Error("토스 결제위젯 결제창형 API를 사용할 수 없습니다."));
    }

    if (tossPaymentWindow && typeof tossPaymentWindow.destroy === "function") {
        try {
            tossPaymentWindow.destroy();
        } catch (e) {
            console.warn("Toss payment window destroy skipped", e);
        }
        tossPaymentWindow = null;
    }

    return new Promise(function (resolve, reject) {
        widgets.renderPaymentWindow({
            variantKey: {
                paymentMethod: "DEFAULT",
                agreement: "AGREEMENT"
            }
        }).then(function (paymentWindow) {
            tossPaymentWindow = paymentWindow;
            paymentWindow.on("paymentRequest", function () {
                setPaymentLoading(true);
                widgets.requestPayment(paymentRequest).catch(function (error) {
                    setPaymentLoading(false);
                    reject(error);
                });
            });
            resolve(paymentWindow);
        }).catch(reject);
    });
}

function getTossCustomerKey() {
    const auth = reservationState.auth || {};
    const memberSid = String(auth.memberSid || "guest");
    const storageKey = "staynowTossCustomerKey:" + memberSid;
    let customerKey = localStorage.getItem(storageKey);

    if (!customerKey) {
        const uuid = window.crypto && typeof window.crypto.randomUUID === "function"
            ? window.crypto.randomUUID()
            : String(Date.now()) + "-" + Math.random().toString(36).slice(2, 14);
        customerKey = "SN-" + uuid;
        localStorage.setItem(storageKey, customerKey);
    }

    return customerKey;
}

function onlyDigits(value) {
    return String(value || "").replace(/\D/g, "");
}

function makeOrderName(hotel, room) {
    return [hotel && hotel.hotelName, room && room.roomName].filter(Boolean).join(" ") || "StayNow 호텔 예약";
}

function makePageUrl(pageName) {
    return location.href.replace(/[^/]*$/, pageName);
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

    if (!/^01[016789]-\d{3,4}-\d{4}$/.test($("#guestPhone").val().trim())) {
        $("#guestPhone").focus();
        return "연락처를 010-0000-0000 형식으로 입력해주세요.";
    }

    if (!$("#guestEmail").val().trim()) {
        $("#guestEmail").focus();
        return "이메일을 입력해주세요.";
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test($("#guestEmail").val().trim())) {
        $("#guestEmail").focus();
        return "올바른 이메일 형식이 아닙니다.";
    }

    if ($(".required-agreement:not(:checked)").length > 0) {
        $(".agreement-box").addClass("shake");
        setTimeout(function () {
            $(".agreement-box").removeClass("shake");
        }, 420);
        return "필수 약관에 모두 동의해야 결제할 수 있습니다.";
    }

    return "";
}

function syncAgreementAll() {
    const total = $(".required-agreement").length;
    const checked = $(".required-agreement:checked").length;
    $("#agreeAll").prop("checked", total > 0 && total === checked);
}

function formatPhoneNumber(value) {
    const numbers = String(value || "").replace(/\D/g, "").slice(0, 11);

    if (numbers.length <= 3) {
        return numbers;
    }

    if (numbers.length <= 7) {
        return numbers.slice(0, 3) + "-" + numbers.slice(3);
    }

    return numbers.slice(0, 3) + "-" + numbers.slice(3, 7) + "-" + numbers.slice(7);
}

function updateStoredAuthPhone(phone) {
    if (!phone) {
        return;
    }

    const storage = localStorage.getItem("staynowAuth") ? localStorage : sessionStorage;
    const auth = readJson("staynowAuth") || {};
    auth.phone = formatPhoneNumber(phone);
    storage.setItem("staynowAuth", JSON.stringify(auth));
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
    const discountedRoomPrice = Number(reservationState.room.discountedRoomPrice || roomPrice);
    const promotionDiscountAmount = Math.max(0, roomPrice - discountedRoomPrice) * stay.nights;
    const originalAmount = roomPrice * stay.nights;
    const subtotalAmount = Math.max(0, originalAmount - promotionDiscountAmount);
    const taxAmount = 0;
    const beforeCouponAmount = subtotalAmount;
    const selectedCoupon = availableCoupons.find(function (coupon) {
        return String(coupon.sid) === String(selectedCouponIssueId);
    });
    const couponDiscountAmount = selectedCoupon ? calculateCouponDiscount(selectedCoupon, beforeCouponAmount) : 0;

    return {
        roomPrice,
        discountedRoomPrice,
        promotionDiscountAmount,
        originalAmount,
        subtotalAmount,
        beforeCouponAmount,
        couponDiscountAmount,
        taxAmount,
        totalAmount: Math.max(0, beforeCouponAmount - couponDiscountAmount)
    };
}

function getMaxUsablePoint(price) {
    return Math.min(availablePoint, Math.max(0, price.totalAmount));
}

function calculateCouponDiscount(coupon, amount) {
    const minOrder = Number(coupon.minOrderAmount || 0);
    if (minOrder > amount) return 0;

    const value = Number(coupon.discountValue || 0);
    if (String(coupon.discountType || "").toUpperCase() === "PERCENT") {
        return Math.min(amount, Math.floor(amount * (value / 100)));
    }

    return Math.min(amount, Math.floor(value));
}

function formatCouponDiscount(coupon) {
    const value = Number(coupon.discountValue || 0);
    if (String(coupon.discountType || "").toUpperCase() === "PERCENT") {
        return value + "%";
    }
    return formatWon(value);
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

function formatSmokePolicy(value) {
    const code = String(value || "").toUpperCase();

    if (code === "LIMITED") {
        return "지정된 곳";
    }

    if (code === "BAN") {
        return "금연";
    }
    
    if (code === "POSSIBLE") {
        return "가능";
    }

    return formatPolicy(value);
}

function formatIdCardPolicy(value) {
    const code = String(value || "").toUpperCase();

    if (code === "ESSENTIAL") {
        return "필수";
    }

    if (code === "OPTICAL") {
        return "필수 아님";
    }

    return value || "체크인 시 확인";
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

    return window.StayNowConfig.assetUrl(path);
}

function formatWon(value) {
    return "₩" + Number(value || 0).toLocaleString();
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
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
    if (xhr instanceof Error && xhr.message) {
        return xhr.message;
    }

    if (xhr && xhr.status === 0) {
        return fallback + " 서버 연결 또는 CORS 설정을 확인해주세요.";
    }

    if (xhr && xhr.responseJSON) {
        const json = xhr.responseJSON;
        if (typeof json.data === "string") return json.data;
        if (json.message) return json.message;
        if (json.error) return json.error;
        if (json.detail) return json.detail;
    }

    if (xhr && xhr.responseText) {
        try {
            const parsed = JSON.parse(xhr.responseText);
            return parsed.message || parsed.data || parsed.error || parsed.detail || xhr.responseText;
        } catch (e) {
            return xhr.responseText;
        }
    }

    return fallback;
}
