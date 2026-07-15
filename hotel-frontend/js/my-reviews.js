const MY_REVIEW_API_BASE = window.StayNowConfig.apiBase;

let myReviewAuth = null;
let myReviews = [];

$(function () {
    myReviewAuth = readMyReviewJson("staynowAuth");

    if (!myReviewAuth || !myReviewAuth.memberSid) {
        sessionStorage.setItem("afterLoginRedirect", "my-reviews.html");
        alert("마이페이지는 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    bindMyReviewEvents();
    renderMyReviewProfile();
    loadMyReviewProfile();
    loadMyReviews();
});

function bindMyReviewEvents() {
    $("#myReviewSort").on("change", renderMyReviews);
    $("#sideLogoutBtn").on("click", function () {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
        location.href = "index.html";
    });
    $(document).on("click", ".my-review-hotel-link", function () {
        const hotelId = $(this).data("hotelId");
        if (hotelId) location.href = "hotel-detail.html?id=" + encodeURIComponent(hotelId);
    });
    $(document).on("click", ".my-review-photo-btn", function () {
        openMyReviewPhoto($(this).data("src"));
    });
    $("#myReviewPhotoClose, #myReviewPhotoViewer").on("click", function (event) {
        if (event.target === this || event.currentTarget.id === "myReviewPhotoClose") {
            closeMyReviewPhoto();
        }
    });
}

function loadMyReviewProfile() {
    $.ajax({
        url: MY_REVIEW_API_BASE + "/member/" + myReviewAuth.memberSid,
        type: "GET",
        headers: myReviewAuthHeaders(),
        success: function (result) {
            const member = result && result.data ? result.data : result;
            if (!member || typeof member !== "object") return;
            myReviewAuth = Object.assign({}, myReviewAuth, {
                name: member.name || myReviewAuth.name,
                email: member.email || myReviewAuth.email,
                point: Number(member.point || 0)
            });
            localStorage.setItem("staynowAuth", JSON.stringify(myReviewAuth));
            renderMyReviewProfile();
        }
    });
}

function loadMyReviews() {
    $.ajax({
        url: MY_REVIEW_API_BASE + "/review/member",
        type: "GET",
        headers: myReviewAuthHeaders(),
        data: {
            memberId: myReviewAuth.memberSid,
            page: 0,
            size: 100,
            sort: "createdAt,desc"
        },
        success: function (result) {
            const page = unwrapMyReview(result);
            myReviews = Array.isArray(page.content) ? page.content : [];
            renderMyReviewProfile();
            renderMyReviews();
        },
        error: function () {
            $("#myReviewList").html('<div class="empty-state">나의 리뷰를 불러오지 못했습니다.</div>');
        }
    });
}

function renderMyReviewProfile() {
    const name = myReviewAuth.name || myReviewAuth.email || "회원";
    $("#profileInitial").text(String(name).slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(myReviewAuth.email || "");
    $("#profilePoint").text(formatMyReviewNumber(myReviewAuth.point || 0));
    $("#myReviewCountSide, #navReviewCount").text(myReviews.length);
}

function renderMyReviews() {
    const reviews = sortMyReviews(myReviews.slice());
    $("#myReviewCountSide, #navReviewCount").text(reviews.length);

    if (!reviews.length) {
        $("#myReviewList").html('<div class="empty-state">아직 작성한 리뷰가 없습니다.</div>');
        return;
    }

    $("#myReviewList").html(reviews.map(renderMyReviewCard).join(""));
    reviews.forEach(function (review) {
        loadMyReviewPhotos(review.sid);
        loadMyReviewAnswer(review.sid);
    });
}

function sortMyReviews(reviews) {
    const sort = $("#myReviewSort").val();
    return reviews.sort(function (a, b) {
        if (sort === "ratingDesc") return Number(b.rating || 0) - Number(a.rating || 0);
        if (sort === "ratingAsc") return Number(a.rating || 0) - Number(b.rating || 0);
        return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
    });
}

function renderMyReviewCard(review) {
    const goodTags = getReviewTags(review, "POSITIVE");
    const badTags = getReviewTags(review, "NEGATIVE");
    return `<article class="my-review-card">
        <div class="my-review-head">
            <button class="my-review-hotel-link" type="button" data-hotel-id="${escapeMyReview(review.hotelId || "")}">
                <span class="hotel-icon"><i class="fa-solid fa-hotel"></i></span>
                <span>
                    <small>${drawMyReviewStars(review.hotelStarRating || 0)} ${escapeMyReview(review.hotelStarRating || 0)}성급</small>
                    <strong>${escapeMyReview(review.hotelName || "호텔명 없음")}</strong>
                    <em><i class="fa-solid fa-location-dot"></i> ${escapeMyReview(review.hotelLocation || "위치 정보 없음")}</em>
                </span>
            </button>
            <div class="my-review-score">
                <b>${formatMyReviewScore(review.rating)}</b>
                <span>${drawMyReviewStars(review.rating || 0)}</span>
            </div>
        </div>
        <div class="my-review-meta">
            <span><i class="fa-regular fa-calendar"></i> ${formatMyReviewDate(review.createdAt)}</span>
            <span><i class="fa-solid fa-bed"></i> ${escapeMyReview(review.roomName || "객실")}</span>
            <span><i class="fa-regular fa-moon"></i> ${escapeMyReview(review.totalNights || 1)}박</span>
        </div>
        <p class="my-review-content">${escapeMyReview(review.content || "")}</p>
        ${goodTags.length || badTags.length ? `<div class="my-review-tags">
            ${goodTags.map(function (tag) { return `<span class="good"><i class="fa-regular fa-thumbs-up"></i>${escapeMyReview(tag)}</span>`; }).join("")}
            ${badTags.map(function (tag) { return `<span class="bad"><i class="fa-regular fa-thumbs-down"></i>${escapeMyReview(tag)}</span>`; }).join("")}
        </div>` : ""}
        <div class="my-review-photos" data-my-review-photos="${escapeMyReview(review.sid)}"></div>
        <div class="my-review-answer-wrap" data-my-review-answer="${escapeMyReview(review.sid)}"></div>
        <div class="my-review-foot">
            <span><i class="fa-regular fa-thumbs-up"></i> 도움됨 ${formatMyReviewNumber(review.likeCount || 0)}</span>
            <span><i class="fa-regular fa-thumbs-down"></i> 아쉬워요 ${formatMyReviewNumber(review.dislikeCount || 0)}</span>
        </div>
    </article>`;
}

function loadMyReviewPhotos(reviewId) {
    if (!reviewId) return;
    $.ajax({
        url: MY_REVIEW_API_BASE + "/review-photo/search",
        type: "GET",
        headers: myReviewAuthHeaders(),
        data: { reviewId, page: 0, size: 10 },
        success: function (result) {
            const page = unwrapMyReview(result) || {};
            const photos = Array.isArray(page.content) ? page.content : [];
            const target = $(".my-review-photos[data-my-review-photos='" + reviewId + "']");
            if (!photos.length) {
                target.empty();
                return;
            }

            target.html(photos.filter(function (photo) {
                return photo && photo.imagePath;
            }).map(function (photo) {
                const src = normalizeMyReviewImagePath(photo.imagePath);
                return `<button type="button" class="my-review-photo-btn" data-src="${escapeMyReview(src)}">
                    <img src="${escapeMyReview(src)}" alt="${escapeMyReview(photo.originalFileName || "리뷰 사진")}">
                </button>`;
            }).join(""));
        }
    });
}

function loadMyReviewAnswer(reviewId) {
    if (!reviewId) return;
    $.ajax({
        url: MY_REVIEW_API_BASE + "/review-answer/review-search",
        type: "GET",
        headers: myReviewAuthHeaders(),
        data: { reviewId },
        success: function (result) {
            const answer = unwrapMyReview(result) || {};
            if (!answer.reviewAnswer) return;
            $(".my-review-answer-wrap[data-my-review-answer='" + reviewId + "']").html(`
                <div class="my-review-answer">
                    <strong><i class="fa-solid fa-reply"></i> 호텔 답변</strong>
                    <p>${escapeMyReview(answer.reviewAnswer)}</p>
                    <small>${formatMyReviewDate(answer.createdAt)}</small>
                </div>
            `);
        },
        error: function () {
            $(".my-review-answer-wrap[data-my-review-answer='" + reviewId + "']").empty();
        }
    });
}

function getReviewTags(review, category) {
    return (review.tags || [])
        .filter(function (tag) {
            return String(tag.reviewTagCategory || "").toUpperCase() === category;
        })
        .map(function (tag) {
            return tag.reviewTagName;
        })
        .filter(Boolean);
}

function drawMyReviewStars(score) {
    const normalized = Math.max(0, Math.min(5, Number(score || 0)));
    return Array.from({ length: 5 }).map(function (_, index) {
        return `<i class="fa-${index < Math.round(normalized) ? "solid" : "regular"} fa-star"></i>`;
    }).join("");
}

function formatMyReviewScore(score) {
    return Number(score || 0).toFixed(1);
}

function formatMyReviewDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value).slice(0, 10).replaceAll("-", ".");
    return date.getFullYear() + "." + String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0");
}

function normalizeMyReviewImagePath(imagePath) {
    if (!imagePath) return "";
    const normalizedPath = String(imagePath).replaceAll("\\", "/");
    if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://") || normalizedPath.startsWith("data:")) {
        return normalizedPath;
    }
    if (normalizedPath.startsWith("/")) {
        return MY_REVIEW_API_BASE.replace(/\/api$/, "") + normalizedPath;
    }
    return MY_REVIEW_API_BASE.replace(/\/api$/, "") + "/" + normalizedPath;
}

function openMyReviewPhoto(src) {
    if (!src) return;
    $("#myReviewPhotoLarge").attr("src", src);
    $("#myReviewPhotoViewer").addClass("show").attr("aria-hidden", "false");
}

function closeMyReviewPhoto() {
    $("#myReviewPhotoViewer").removeClass("show").attr("aria-hidden", "true");
    $("#myReviewPhotoLarge").attr("src", "");
}

function unwrapMyReview(result) {
    return result && Object.prototype.hasOwnProperty.call(result, "data") ? result.data : result;
}

function myReviewAuthHeaders() {
    return myReviewAuth && myReviewAuth.token ? { Authorization: myReviewAuth.token } : {};
}

function readMyReviewJson(key) {
    try {
        return JSON.parse(localStorage.getItem(key) || sessionStorage.getItem(key) || "null");
    } catch (e) {
        return null;
    }
}

function formatMyReviewNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function escapeMyReview(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
