const MY_API_BASE = window.StayNowConfig.apiBase;
let myAuth = null;
let reservations = [];
let activeFilter = "ALL";
let currentReviewTarget = null;
let currentRating = 5;
let reviewedReservationIds = new Set();
let reviewTagMasters = [];
let selectedReviewFiles = [];

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
    loadLatestMyProfile();
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
    $("#cancelReviewBtn").on("click", closeReviewModal);
    $("#reviewModal").on("click", function (event) {
        if (event.target === this) closeReviewModal();
    });

    $("#writeTravelType button").on("click", function () {
        $("#writeTravelType button").removeClass("active");
        $(this).addClass("active");
    });
    $(document).on("click", ".write-tag-chip", function () {
        $(this).toggleClass("active");
    });
    $(document).on("click", ".write-star-btn", function () {
        currentRating = Number($(this).data("rating"));
        paintRating();
    });
    $(document).on("click", ".mini-star-btn", function () {
        const wrap = $(this).closest(".mini-stars");
        wrap.attr("data-rating", $(this).data("rating"));
        paintMiniStars(wrap);
    });
    $("#writeReviewPhotos").on("change", drawPhotoPreview);
    $(document).on("click", ".write-photo-preview-item", function () {
        selectedReviewFiles.splice(Number($(this).data("index")), 1);
        redrawSelectedReviewFiles();
    });
    $("#reviewContent").on("input", updateReviewContentCount);
    $("#myReviewForm").on("submit", submitReview);
    loadReviewTagMasters();
}

function loadReservations() {
    $.ajax({
        url: MY_API_BASE + "/reservation/search",
        headers: myAuthHeaders(),
        data: { memberId: myAuth.memberSid, page: 0, size: 100, sort: "createdAt,desc" },
        success: function (page) {
            reservations = (page.content || []).map(mergeLocalReservation);
            renderProfile();
            renderCounts();
            syncWrittenReviews(renderReservations);
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

function loadLatestMyProfile() {
    $.ajax({
        url: MY_API_BASE + "/member/" + myAuth.memberSid,
        type: "GET",
        headers: myAuthHeaders(),
        success: function (result) {
            const member = result.data || {};
            myAuth = Object.assign({}, myAuth, {
                name: member.name || myAuth.name,
                email: member.email || myAuth.email,
                point: Number(member.point || 0)
            });
            saveMyAuth(myAuth);
            renderProfile();
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
            '<div class="hotel-title hotel-link" role="link" tabindex="0" title="호텔 상세 보기">' +
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
    card.find(".hotel-link").on("click", function () { goHotelDetail(reservation.hotelId); });
    card.find(".hotel-link").on("keydown", function (event) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            goHotelDetail(reservation.hotelId);
        }
    });

    return card;
}

function goHotelDetail(hotelId) {
    if (!hotelId) {
        alert("호텔 상세 정보를 찾을 수 없습니다.");
        return;
    }

    location.href = "hotel-detail.html?id=" + encodeURIComponent(hotelId);
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

    if (state.key === "checked-in") {
        return '<span class="mini-chip green"><i class="fa-solid fa-circle-check"></i> 체크인 완료</span>';
    }

    return '<button type="button" class="mini-chip checkin-btn"><i class="fa-regular fa-clock"></i> 체크인</button>';
}

function checkInReservation(id) {
    $.ajax({
        url: MY_API_BASE + "/reservation/" + id + "/check-in",
        type: "PATCH",
        headers: myAuthHeaders(),
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
        headers: myAuthHeaders(),
        data: JSON.stringify({ sid: id, cancelReason: "고객 직접 취소" }),
        success: loadReservations,
        error: function (xhr) { alert(getMyErrorMessage(xhr, "예약 취소에 실패했습니다.")); }
    });
}

function openReviewModal(reservation) {
    if (hasWrittenReview(reservation.sid)) {
        alert("이미 리뷰를 작성한 예약입니다.");
        renderReservations();
        return;
    }

    currentReviewTarget = reservation;
    currentRating = 5;
    selectedReviewFiles = [];
    $("#reviewHotelName").text(reservation.hotelName || "호텔명 없음");
    $("#reviewHotelSub").text((reservation.hotelName || "호텔") + " 이용은 어떠셨나요?");
    $("#reviewHotelMeta").text((reservation.roomName || "이용 객실") + " · " + formatDate(reservation.checkInDate) + " ~ " + formatDate(reservation.checkOutDate));
    $("#reviewContent").val("");
    $("#writeReviewPhotos").val("");
    $("#writePhotoPreview").empty();
    $("#writeTravelType button").removeClass("active").first().addClass("active");
    $(".write-tag-chip").removeClass("active");
    $(".mini-stars").attr("data-rating", "5").each(function () { paintMiniStars($(this)); });
    paintRating();
    updateReviewContentCount();
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
        travelType: $("#writeTravelType button.active").data("value") || "FAMILY",
        content: $("#reviewContent").val().trim(),
        categories: collectReviewCategories(),
        tags: collectReviewTags()
    };

    if (!payload.content) {
        alert("리뷰 내용을 입력해주세요.");
        return;
    }

    $("#submitReviewBtn").prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 등록 중');

    $.ajax({
        url: MY_API_BASE + "/review",
        type: "POST",
        contentType: "application/json",
        headers: myAuthHeaders(),
        data: JSON.stringify(payload),
        success: function (result) {
            markReviewWritten(currentReviewTarget.sid);
            uploadReviewPhotos(result?.data?.sid, function () {
                closeReviewModal();
                loadLatestMyProfile();
                syncWrittenReviews(renderReservations);
            });
        },
        error: function () {
            alert("리뷰 등록에 실패했습니다. 이미 작성한 예약인지 확인해주세요.");
        },
        complete: function () {
            $("#submitReviewBtn").prop("disabled", false).html('<i class="fa-regular fa-paper-plane"></i> 리뷰 등록');
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

function syncWrittenReviews(done) {
    reviewedReservationIds = new Set();
    const hotelIds = Array.from(new Set(reservations.map(function (reservation) {
        return reservation.hotelId;
    }).filter(Boolean)));

    if (hotelIds.length === 0) {
        done();
        return;
    }

    const requests = hotelIds.map(function (hotelId) {
        return $.ajax({
            url: MY_API_BASE + "/review/search?hotelId=" + hotelId + "&page=0&size=200&sort=createdAt,desc",
            type: "GET"
        }).then(function (result) {
            const reviews = result?.data?.content || [];
            reviews.forEach(function (review) {
                if (String(review.memberId) === String(myAuth.memberSid) && review.reservationId) {
                    reviewedReservationIds.add(String(review.reservationId));
                    localStorage.setItem("staynowReviewWritten:" + review.reservationId, "true");
                }
            });
        }, function () {});
    });

    $.when.apply($, requests).always(done);
}

function loadReviewTagMasters() {
    $.ajax({
        url: MY_API_BASE + "/review-tag-master",
        type: "GET",
        success: function (result) {
            reviewTagMasters = result.data || [];
            drawWriteTagButtons();
        },
        error: function () {
            reviewTagMasters = [];
            drawWriteTagButtons();
        }
    });
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
    if (status === "CHECKED_IN") return { key: "checked-in", group: "UPCOMING", label: "투숙 중" };
    if (status === "CHECKED_OUT" || status === "COMPLETED") return { key: "completed", group: "COMPLETED", label: "완료된 여행" };
    return { key: "upcoming", group: "UPCOMING", label: "예정된 여행" };
}

function getMyErrorMessage(xhr, fallback) {
    if (xhr && xhr.responseJSON) {
        const json = xhr.responseJSON;
        if (typeof json.data === "string") return json.data;
        return json.message || json.error || json.detail || fallback;
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
    const wrap = $("#writeMainRating");
    wrap.empty();

    for (let i = 1; i <= 5; i++) {
        wrap.append('<button type="button" class="write-star-btn ' + (i <= currentRating ? "active" : "") + '" data-rating="' + i + '"><i class="fa-regular fa-star"></i></button>');
    }

    $("#writeScoreText").text((currentRating * 2).toFixed(1));
    $("#writeScoreLabel").text(currentRating >= 5 ? "5점 최고" : currentRating >= 4 ? "좋음" : currentRating >= 3 ? "보통" : "아쉬움");
}

function paintMiniStars(wrap) {
    const rating = Number(wrap.attr("data-rating") || 5);
    wrap.empty();

    for (let i = 1; i <= 5; i++) {
        wrap.append('<button type="button" class="mini-star-btn ' + (i <= rating ? "active" : "") + '" data-rating="' + i + '"><i class="fa-regular fa-star"></i></button>');
    }
}

function updateReviewContentCount() {
    const length = $("#reviewContent").val().length;
    $("#writeContentCount").text(length.toLocaleString() + " / 1,000자");
}

function collectReviewCategories() {
    return $(".mini-stars").map(function () {
        return {
            categoryId: Number($(this).data("category-id")),
            rating: Number($(this).attr("data-rating") || 5)
        };
    }).get();
}

function collectReviewTags() {
    return $(".write-tag-chip.active").map(function () {
        return { tagId: Number($(this).data("tag-id")) };
    }).get();
}

function drawWriteTagButtons() {
    const pros = reviewTagMasters.filter(function (tag) { return getReviewTagCategory(tag) === "PROS"; });
    const cons = reviewTagMasters.filter(function (tag) { return getReviewTagCategory(tag) === "CONS"; });

    $(".write-tag-area").html(
        makeWriteTagGroup("pros", "fa-regular fa-thumbs-up", "좋았던 점", pros) +
        makeWriteTagGroup("cons", "fa-regular fa-thumbs-down", "아쉬운 점", cons)
    );
}

function makeWriteTagGroup(type, icon, title, tags) {
    const buttons = tags.map(function (tag) {
        return '<button type="button" class="write-tag-chip" data-tag-id="' + tag.sid + '">' + escapeHtml(tag.reviewTagName || "태그") + '</button>';
    }).join("");

    return '<div>' +
        '<b class="tag-title ' + type + '"><i class="' + icon + '"></i> ' + title + '</b>' +
        (buttons || '<span class="empty-write-tag">등록된 태그 없음</span>') +
        '</div>';
}

function getReviewTagCategory(tag) {
    if (!tag || !tag.reviewTagCategory) return "";
    if (typeof tag.reviewTagCategory === "string") return tag.reviewTagCategory;
    return tag.reviewTagCategory.name || tag.reviewTagCategory.value || "";
}

function drawPhotoPreview() {
    const incomingFiles = Array.from(this.files || []);
    incomingFiles.forEach(function (file) {
        const duplicated = selectedReviewFiles.some(function (selected) {
            return selected.name === file.name && selected.size === file.size && selected.lastModified === file.lastModified;
        });

        if (!duplicated && selectedReviewFiles.length < 10) {
            selectedReviewFiles.push(file);
        }
    });

    redrawSelectedReviewFiles();
    $("#writeReviewPhotos").val("");
}

function redrawSelectedReviewFiles() {
    const preview = $("#writePhotoPreview");
    preview.empty();

    selectedReviewFiles.forEach(function (file, index) {
        const url = URL.createObjectURL(file);
        preview.append(
            '<button type="button" class="write-photo-preview-item" data-index="' + index + '">' +
                '<img src="' + url + '" alt="첨부 사진 미리보기">' +
                '<span><i class="fa-solid fa-xmark"></i></span>' +
            '</button>'
        );
    });
}

function uploadReviewPhotos(reviewId, done) {
    const files = selectedReviewFiles.slice(0, 10);
    if (!reviewId || files.length === 0) {
        done();
        return;
    }

    const formData = new FormData();
    formData.append("reviewId", reviewId);
    files.forEach(function (file) {
        formData.append("photos", file);
    });

    $.ajax({
        url: MY_API_BASE + "/review-photo",
        type: "POST",
        headers: myAuthHeaders(),
        data: formData,
        processData: false,
        contentType: false,
        complete: done
    });
}

function metaItem(icon, label, value, sub) {
    return '<div class="meta-item"><i class="' + icon + '"></i><div><span>' + label + '</span><strong>' + escapeHtml(value || "-") + '</strong><small>' + escapeHtml(sub || "") + '</small></div></div>';
}

function hasWrittenReview(id) {
    return reviewedReservationIds.has(String(id)) || localStorage.getItem("staynowReviewWritten:" + id) === "true";
}

function markReviewWritten(id) {
    reviewedReservationIds.add(String(id));
    localStorage.setItem("staynowReviewWritten:" + id, "true");
}

function getMyAuth() {
    return readJson("staynowAuth");
}

function myAuthHeaders() {
    return myAuth && myAuth.token ? { Authorization: myAuth.token } : {};
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

function saveMyAuth(auth) {
    const storage = localStorage.getItem("staynowAuth") ? localStorage : sessionStorage;
    storage.setItem("staynowAuth", JSON.stringify(auth));
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
