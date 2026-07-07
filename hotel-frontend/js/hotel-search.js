const HOTEL_SEARCH_API = "http://localhost:33000/api/hotel/search";
const PAGE_SIZE = 5;
let currentHotels = [];

$(function () {

    init();

});

function init() {

    bindEvent();
    restoreSearchCondition();
    drawCachedResultOrSearch();

}

function bindEvent() {

    $("#researchBtn").click(function () {
        requestHotels(0);
    });

    $("#resetFilterBtn").click(resetFilters);

    $("input[name='star'], .room-type").change(function () {
        requestHotels(0);
    });

    $("#minPrice, #maxPrice").on("change", function () {
        requestHotels(0);
    });

    $("#sortSelect").change(function () {
        drawHotels(currentHotels);
    });

    $("#filterPanelToggle").click(function () {
        $(".filter-panel").toggleClass("open");
    });

}

function restoreSearchCondition() {

    const request = getStoredRequest();

    if (!request) {
        return;
    }

    $("#resultLocation").val(request.location || "");
    $("#resultCheckIn").val(toDateValue(request.checkIn));
    $("#resultCheckOut").val(toDateValue(request.checkOut));
    $("#resultAdult").val(request.adult || 2);
    $("#resultChild").val(request.child || 0);

    if (request.minPrice) {
        $("#minPrice").val(request.minPrice);
    }

    if (request.maxPrice) {
        $("#maxPrice").val(request.maxPrice);
    }

    if (request.star) {
        $("input[name='star'][value='" + request.star + "']").prop("checked", true);
    }

    if (request.roomTypeIds && request.roomTypeIds.length > 0) {
        $(".room-type").each(function () {
            $(this).prop("checked", request.roomTypeIds.includes(Number($(this).val())));
        });
    }

}

function drawCachedResultOrSearch() {

    const cached = sessionStorage.getItem("hotelSearchResult");

    if (cached) {
        sessionStorage.removeItem("hotelSearchResult");
        drawResult(JSON.parse(cached));
        return;
    }

    requestHotels(0);

}

function requestHotels(page) {

    const request = makeRequest();

    if (!validateRequest(request)) {
        return;
    }

    showLoading(true);

    $.ajax({
        url: HOTEL_SEARCH_API + "?page=" + page + "&size=" + PAGE_SIZE,
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),

        success: function (result) {

            sessionStorage.setItem("hotelSearchRequest", JSON.stringify(request));
            drawResult(result);

        },

        error: function () {

            alert("호텔 검색 결과를 불러오지 못했습니다.");

        },

        complete: function () {

            showLoading(false);

        }
    });

}

function makeRequest() {

    const checkIn = $("#resultCheckIn").val();
    const checkOut = $("#resultCheckOut").val();
    const minPrice = toNullableNumber($("#minPrice").val());
    const maxPrice = toNullableNumber($("#maxPrice").val());
    const star = toNullableNumber($("input[name='star']:checked").val());
    const roomTypeIds = $(".room-type:checked")
        .map(function () {
            return Number($(this).val());
        })
        .get();

    return {
        location: $("#resultLocation").val().trim(),
        checkIn: checkIn ? checkIn + "T15:00:00" : null,
        checkOut: checkOut ? checkOut + "T11:00:00" : null,
        adult: Number($("#resultAdult").val()),
        child: Number($("#resultChild").val()),
        minPrice: minPrice,
        maxPrice: maxPrice,
        star: star,
        roomTypeIds: roomTypeIds
    };

}

function validateRequest(request) {

    if (!request.checkIn) {
        alert("체크인 날짜를 선택하세요.");
        $("#resultCheckIn").focus();
        return false;
    }

    if (!request.checkOut) {
        alert("체크아웃 날짜를 선택하세요.");
        $("#resultCheckOut").focus();
        return false;
    }

    if (new Date($("#resultCheckIn").val()) >= new Date($("#resultCheckOut").val())) {
        alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        $("#resultCheckOut").focus();
        return false;
    }

    if (request.minPrice && request.maxPrice && request.minPrice > request.maxPrice) {
        alert("최대 금액은 최소 금액보다 커야 합니다.");
        return false;
    }

    return true;

}

function drawResult(result) {

    const page = result.data || {};
    const hotels = page.content || [];

    currentHotels = hotels;

    drawSummary(page);
    drawHotels(hotels);
    drawPagination(page);

}

function drawSummary(page) {

    const request = makeRequest();
    const total = page.totalElements || 0;
    const checkIn = $("#resultCheckIn").val();
    const checkOut = $("#resultCheckOut").val();

    $("#resultTitle").text(request.location || "호텔");
    $("#filterLocation").text((request.location || "전체") + " 호텔 검색 결과");
    $("#filterCount").text(total.toLocaleString() + "개 호텔");
    $("#resultPeriod").text(
        formatDate(checkIn) +
        " ~ " +
        formatDate(checkOut) +
        " · 성인 " +
        request.adult +
        "명 · 아이 " +
        request.child +
        "명"
    );

}

function drawHotels(hotels) {

    const list = $("#hotelResultList");
    const sortedHotels = sortHotels(hotels);

    list.empty();
    $("#emptyState").hide();

    if (sortedHotels.length === 0) {
        $("#emptyState").show();
        return;
    }

    $.each(sortedHotels, function (index, hotel) {

        const price = hotel.hotelPrice || 0;
        const discountRate = hotel.maxDiscountRate || 0;
        const saleBadge = discountRate > 0
            ? `<span class="sale-badge show">SALE ${discountRate}%</span>`
            : "";
        const card = `
            <article class="hotel-result-card">
                <div class="hotel-thumb" data-hotel-id="${hotel.sid || ""}">
                    <img src="${getFallbackImage()}" alt="${escapeHtml(hotel.hotelName || "호텔")} 썸네일">
                    ${saleBadge}
                </div>

                <div class="hotel-info">
                    <div class="hotel-rating">${drawStars(hotel.starRating)} ${hotel.starRating || 0}성급</div>
                    <h2>${escapeHtml(hotel.hotelName || "호텔명 없음")}</h2>
                    <p class="hotel-location">
                        <i class="fa-solid fa-location-dot"></i>
                        ${escapeHtml(hotel.location || "지역 정보 없음")}
                    </p>
                    <p class="hotel-desc">${escapeHtml(hotel.description || "편안한 숙박을 제공하는 StayNow 추천 호텔입니다.")}</p>
                    <div class="tag-list">
                        <span>${escapeHtml(hotel.typeTitle || "호텔")}</span>
                        <span>예약 가능</span>
                        <span>무료 취소</span>
                    </div>
                </div>

                <div class="hotel-price">
                    <small>1박 기준</small>
                    <strong>₩${price.toLocaleString()}</strong>
                    <a class="reserve-btn" href="hotel-detail.html?id=${hotel.sid || ""}">예약하기</a>
                </div>
            </article>
        `;

        list.append(card);

    });

    loadHotelThumbnails(sortedHotels);

}

function sortHotels(hotels) {

    const sortType = $("#sortSelect").val();
    const copiedHotels = hotels.slice();

    if (sortType === "priceAsc") {
        return copiedHotels.sort(function (a, b) {
            return (a.hotelPrice || 0) - (b.hotelPrice || 0);
        });
    }

    if (sortType === "priceDesc") {
        return copiedHotels.sort(function (a, b) {
            return (b.hotelPrice || 0) - (a.hotelPrice || 0);
        });
    }

    if (sortType === "starDesc") {
        return copiedHotels.sort(function (a, b) {
            return (b.starRating || 0) - (a.starRating || 0);
        });
    }

    return copiedHotels;

}

function loadHotelThumbnails(hotels) {

    $.each(hotels, function (index, hotel) {

        if (!hotel.sid) {
            return;
        }

        $.ajax({
            url: "http://localhost:33000/api/hotel/inimage/" + hotel.sid,
            type: "GET",

            success: function (result) {

                const photos = result.data || [];

                if (photos.length === 0 || !photos[0].imagePath) {
                    return;
                }

                const imagePath = normalizeImagePath(photos[0].imagePath);

                $(".hotel-thumb[data-hotel-id='" + hotel.sid + "'] img")
                    .attr("src", imagePath);

            },

            error: function () {

                console.log("호텔 사진을 불러오지 못했습니다.", hotel.sid);

            }
        });

    });

}

function normalizeImagePath(imagePath) {

    if (
        imagePath.startsWith("http://") ||
        imagePath.startsWith("https://") ||
        imagePath.startsWith("/") ||
        imagePath.startsWith("data:")
    ) {
        return imagePath;
    }

    return imagePath;

}

function getFallbackImage() {

    return "https://gunsancci.korcham.net/images/no-image01.gif";

}

function drawPagination(page) {

    const pagination = $("#pagination");
    const totalPages = page.totalPages || 0;
    const current = page.number || 0;

    pagination.empty();

    if (totalPages <= 1) {
        return;
    }

    pagination.append(makePageButton("이전", current - 1, current === 0));

    for (let i = 0; i < totalPages; i++) {
        pagination.append(makePageButton(i + 1, i, false, i === current));
    }

    pagination.append(makePageButton("다음", current + 1, current >= totalPages - 1));

}

function makePageButton(text, page, disabled, active) {

    const button = $("<button>")
        .addClass("page-btn")
        .text(text)
        .prop("disabled", disabled);

    if (active) {
        button.addClass("active");
    }

    button.click(function () {
        requestHotels(page);
    });

    return button;

}

function resetFilters() {

    $("#minPrice, #maxPrice").val("");
    $("input[name='star'][value='']").prop("checked", true);
    $(".room-type").prop("checked", false);
    $("#sortSelect").val("recommended");

    requestHotels(0);

}

function showLoading(show) {

    if (show) {
        $("#loadingState").show();
        $("#hotelResultList, #emptyState, #pagination").hide();
    } else {
        $("#loadingState").hide();
        $("#hotelResultList, #pagination").show();
    }

}

function getStoredRequest() {

    const stored = sessionStorage.getItem("hotelSearchRequest");

    if (!stored) {
        return null;
    }

    return JSON.parse(stored);

}

function toDateValue(dateTime) {

    return dateTime ? dateTime.substring(0, 10) : "";

}

function toNullableNumber(value) {

    return value === "" || value == null ? null : Number(value);

}

function formatDate(value) {

    if (!value) {
        return "날짜 미선택";
    }

    return value.replaceAll("-", ".");

}

function drawStars(count) {

    const starCount = count || 0;

    return "★".repeat(starCount) + "☆".repeat(Math.max(0, 5 - starCount));

}

function escapeHtml(value) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}
