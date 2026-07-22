const HOTEL_API_BASE = window.StayNowConfig.apiBase;
const HOTEL_SEARCH_API = window.StayNowConfig.apiUrl("/hotel/search");
const HOTEL_TYPE_API = window.StayNowConfig.apiUrl("/hoteltype");
const HOTEL_ALL_API = window.StayNowConfig.apiUrl("/hotel/all");
const HOTEL_SEARCH_COOKIE = "staynowSearchRequest";
const PAGE_SIZE = 5;
const RESULT_LOCATION_SUGGESTIONS = [
    "서울특별시", "부산광역시", "제주특별자치도", "강릉", "경주", "여수", "속초", "인천", "대구", "대전", "광주", "전주", "수원", "가평", "춘천"
];
let currentHotels = [];
let resultDatesAdjusted = false;
let resultHotelSuggestions = [];
let resultDatePickerMonth = null;

$(function () {

    init();

});

function init() {

    bindEvent();
    loadHotelTypeFilters().always(function () {
        restoreSearchCondition();
        initResultDateBounds();
        initResultSearchSuggestions();
        drawCachedResultOrSearch();
    });

}

function bindEvent() {

    $("#researchBtn").click(function () {
        requestHotels(0);
    });

    $("#resetFilterBtn").click(resetFilters);

    $(document).on("change", "input[name='star'], .room-type", function () {
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

function loadHotelTypeFilters() {

    return $.ajax({
        url: HOTEL_TYPE_API,
        type: "GET",
        success: function (result) {
            const types = result.data || [];
            renderHotelTypeFilters(types);
        },
        error: function () {
            renderHotelTypeFilters([
                { sid: 1, title: "호텔" },
                { sid: 2, title: "리조트" },
                { sid: 3, title: "펜션/풀빌라" }
            ]);
        }
    });

}

function renderHotelTypeFilters(types) {

    const list = $("#hotelTypeFilterList");
    list.empty();

    if (!types.length) {
        list.html('<p class="filter-empty">등록된 숙소 유형이 없습니다.</p>');
        return;
    }

    types.forEach(function (type) {
        const id = type.sid || type.id;
        const title = type.title || type.typeTitle || "숙소 유형";

        if (!id) {
            return;
        }

        list.append(
            '<label class="check-row">' +
                '<input type="checkbox" class="room-type" value="' + escapeHtml(id) + '">' +
                '<span>' + escapeHtml(title) + '</span>' +
            '</label>'
        );
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

    if (cached && !resultDatesAdjusted) {
        sessionStorage.removeItem("hotelSearchResult");
        if (!validateRequest(makeRequest())) {
            return;
        }
        drawResult(JSON.parse(cached));
        return;
    }

    if (cached) {
        sessionStorage.removeItem("hotelSearchResult");
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

            saveStoredRequest(request);
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
        $("#resultDateRangeToggle").focus();
        return false;
    }

    if (!request.checkOut) {
        alert("체크아웃 날짜를 선택하세요.");
        $("#resultDateRangeToggle").focus();
        return false;
    }

    if (isPastResultDate($("#resultCheckIn").val())) {
        alert("지난 날짜로는 체크인할 수 없습니다.");
        $("#resultDateRangeToggle").focus();
        return false;
    }

    if (new Date($("#resultCheckIn").val()) >= new Date($("#resultCheckOut").val())) {
        alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        $("#resultDateRangeToggle").focus();
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

function initResultDateBounds() {
    normalizeResultDateRange();
    initResultDateRangePicker();
    updateResultDateRangeText();
}

function updateResultCheckoutMin(forceValid) {
    const checkIn = $("#resultCheckIn").val() || toResultInputDate(new Date());
    const minCheckout = addResultDays(checkIn, 1);

    $("#resultCheckOut").attr("min", minCheckout);

    if (forceValid && (!$("#resultCheckOut").val() || $("#resultCheckOut").val() <= checkIn)) {
        $("#resultCheckOut").val(minCheckout);
        resultDatesAdjusted = true;
    }
}

function normalizeResultDateRange() {
    const today = toResultInputDate(new Date());
    let checkIn = $("#resultCheckIn").val();
    let checkOut = $("#resultCheckOut").val();

    if (!checkIn || isPastResultDate(checkIn)) {
        checkIn = today;
        $("#resultCheckIn").val(checkIn);
        resultDatesAdjusted = true;
    }

    if (!checkOut || checkOut <= checkIn) {
        $("#resultCheckOut").val(addResultDays(checkIn, 1));
        resultDatesAdjusted = true;
    }
}

function addResultDays(value, days) {
    const date = new Date(value + "T00:00:00");
    date.setDate(date.getDate() + days);
    return toResultInputDate(date);
}

function toResultInputDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
}

function isPastResultDate(value) {
    return value && value < toResultInputDate(new Date());
}

function initResultSearchSuggestions() {
    const input = $("#resultLocation");
    const suggestions = $("#resultSearchSuggestions");

    if (!input.length || !suggestions.length) {
        return;
    }

    loadResultHotelSuggestions();

    input.on("focus input", function () {
        renderResultSearchSuggestions($(this).val());
    });

    input.on("keydown", function (event) {
        if (event.key === "Escape") {
            hideResultSearchSuggestions();
        }
    });

    suggestions.on("mousedown", ".result-suggestion-item", function (event) {
        event.preventDefault();
        input.val($(this).data("value"));
        hideResultSearchSuggestions();
    });

    $(document).on("mousedown", function (event) {
        if (!$(event.target).closest(".result-location-wrap").length) {
            hideResultSearchSuggestions();
        }
    });
}

function loadResultHotelSuggestions() {
    $.ajax({
        url: HOTEL_ALL_API,
        type: "GET",
        timeout: 5000,
        success: function (result) {
            const hotels = Array.isArray(result)
                ? result
                : (result && Array.isArray(result.data)
                    ? result.data
                    : (result && Array.isArray(result.content) ? result.content : []));

            resultHotelSuggestions = hotels
                .map(function (hotel) {
                    const name = hotel.hotelName || hotel.name;
                    if (!name) {
                        return null;
                    }
                    return {
                        type: "호텔",
                        value: name,
                        label: name,
                        subtitle: hotel.location || hotel.address || "호텔명 검색"
                    };
                })
                .filter(Boolean)
                .slice(0, 80);
        }
    });
}

function renderResultSearchSuggestions(keyword) {
    const suggestions = $("#resultSearchSuggestions");
    const query = String(keyword || "").trim().toLowerCase();
    const locationItems = RESULT_LOCATION_SUGGESTIONS.map(function (name) {
        return {
            type: "지역",
            value: name,
            label: name,
            subtitle: "인기 여행지"
        };
    });
    const items = locationItems.concat(resultHotelSuggestions)
        .filter(function (item) {
            if (!query) {
                return item.type === "지역";
            }
            return String(item.label).toLowerCase().includes(query) || String(item.subtitle).toLowerCase().includes(query);
        })
        .slice(0, 8);

    if (!items.length) {
        suggestions.html(`
            <button type="button" class="result-suggestion-item" data-value="${escapeHtml(keyword)}">
                <span class="result-suggestion-main">
                    <span class="result-suggestion-name">${escapeHtml(keyword)}</span>
                    <span class="result-suggestion-sub">입력한 검색어로 바로 검색</span>
                </span>
                <span class="result-suggestion-type">검색</span>
            </button>
        `);
    } else {
        suggestions.html(items.map(function (item) {
            return `
                <button type="button" class="result-suggestion-item" role="option" data-value="${escapeHtml(item.value)}">
                    <span class="result-suggestion-main">
                        <span class="result-suggestion-name">${escapeHtml(item.label)}</span>
                        <span class="result-suggestion-sub">${escapeHtml(item.subtitle)}</span>
                    </span>
                    <span class="result-suggestion-type">${escapeHtml(item.type)}</span>
                </button>
            `;
        }).join(""));
    }

    suggestions.prop("hidden", false);
    $("#resultLocation").attr("aria-expanded", "true");
}

function hideResultSearchSuggestions() {
    $("#resultSearchSuggestions").prop("hidden", true);
    $("#resultLocation").attr("aria-expanded", "false");
}

function initResultDateRangePicker() {
    if (!$("#resultDateRangeToggle").length) {
        return;
    }

    resultDatePickerMonth = getResultMonthStart($("#resultCheckIn").val() || toResultInputDate(new Date()));
    renderResultDateRangePicker();

    $("#resultDateRangeToggle").on("click", function (event) {
        event.stopPropagation();
        const picker = $("#resultDateRangePicker");
        const willOpen = picker.prop("hidden");
        picker.prop("hidden", !willOpen);
        $(this).attr("aria-expanded", String(willOpen));
        if (willOpen) {
            renderResultDateRangePicker();
        }
    });

    $("#resultDateRangePicker").on("click", function (event) {
        event.stopPropagation();
    });

    $("#resultDateRangePicker").on("click", "[data-result-date-nav]", function () {
        resultDatePickerMonth.setMonth(resultDatePickerMonth.getMonth() + Number($(this).data("result-date-nav")));
        renderResultDateRangePicker();
    });

    $("#resultDateRangePicker").on("click", ".result-date-cell:not(.disabled)", function () {
        selectResultSearchDate($(this).data("date"));
    });

    $(document).on("click", function () {
        closeResultDateRangePicker();
    });
}

function renderResultDateRangePicker() {
    const monthOne = getResultMonthStart(resultDatePickerMonth || new Date());
    const monthTwo = addResultMonths(monthOne, 1);
    const checkIn = $("#resultCheckIn").val();
    const checkOut = $("#resultCheckOut").val();

    $("#resultDateRangePicker").html(`
        <div class="result-date-picker-head">
            <button type="button" class="result-date-picker-nav" data-result-date-nav="-1" aria-label="이전 달">
                <i class="fa-solid fa-chevron-left" aria-hidden="true"></i>
            </button>
            <div class="result-date-picker-title">체크인과 체크아웃을 선택하세요</div>
            <button type="button" class="result-date-picker-nav" data-result-date-nav="1" aria-label="다음 달">
                <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
            </button>
        </div>
        <div class="result-date-months">
            ${renderResultCalendarMonth(monthOne, checkIn, checkOut)}
            ${renderResultCalendarMonth(monthTwo, checkIn, checkOut)}
        </div>
        <p class="result-date-hint">${checkIn && !checkOut ? "체크아웃 날짜를 선택해주세요." : "체크인은 오늘 이후, 체크아웃은 체크인 다음 날부터 선택할 수 있어요."}</p>
    `);
}

function renderResultCalendarMonth(monthDate, checkIn, checkOut) {
    const year = monthDate.getFullYear();
    const month = monthDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const start = new Date(firstDay);
    start.setDate(firstDay.getDate() - firstDay.getDay());
    let days = "";

    for (let i = 0; i < 42; i++) {
        const day = new Date(start);
        day.setDate(start.getDate() + i);
        const value = toResultInputDate(day);
        const isOtherMonth = day.getMonth() !== month;
        const isDisabled = isPastResultDate(value);
        const isStart = value === checkIn;
        const isEnd = value === checkOut;
        const inRange = checkIn && checkOut && value > checkIn && value < checkOut;
        const classes = [
            "result-date-cell",
            isOtherMonth ? "other-month" : "",
            isDisabled ? "disabled" : "",
            isStart ? "selected-start" : "",
            isEnd ? "selected-end" : "",
            inRange ? "in-range" : ""
        ].filter(Boolean).join(" ");

        days += `<button type="button" class="${classes}" data-date="${value}" ${isDisabled ? "disabled" : ""}>${day.getDate()}</button>`;
    }

    return `
        <div class="result-date-month">
            <div class="result-date-month-title">${year}년 ${month + 1}월</div>
            <div class="result-date-week">
                <span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span>
            </div>
            <div class="result-date-days">${days}</div>
        </div>
    `;
}

function selectResultSearchDate(value) {
    const checkIn = $("#resultCheckIn").val();
    const checkOut = $("#resultCheckOut").val();

    if (!checkIn || (checkIn && checkOut) || value <= checkIn) {
        $("#resultCheckIn").val(value);
        $("#resultCheckOut").val("");
    } else {
        $("#resultCheckOut").val(value);
        setTimeout(closeResultDateRangePicker, 120);
    }

    updateResultDateRangeText();
    renderResultDateRangePicker();
}

function updateResultDateRangeText() {
    const checkIn = $("#resultCheckIn").val();
    const checkOut = $("#resultCheckOut").val();
    const text = checkIn && checkOut
        ? formatResultDateLabel(checkIn) + " - " + formatResultDateLabel(checkOut)
        : (checkIn ? formatResultDateLabel(checkIn) + " - 체크아웃 선택" : "날짜 선택");

    $("#resultDateRangeText").text(text);
}

function closeResultDateRangePicker() {
    $("#resultDateRangePicker").prop("hidden", true);
    $("#resultDateRangeToggle").attr("aria-expanded", "false");
}

function formatResultDateLabel(value) {
    const date = new Date(value + "T00:00:00");
    const days = ["일", "월", "화", "수", "목", "금", "토"];

    return String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0") + " (" + days[date.getDay()] + ")";
}

function getResultMonthStart(value) {
    const date = value instanceof Date ? new Date(value) : new Date(value + "T00:00:00");
    return new Date(date.getFullYear(), date.getMonth(), 1);
}

function addResultMonths(value, count) {
    const date = new Date(value);
    date.setMonth(date.getMonth() + count);
    return date;
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

                <div class="hotel-price" data-hotel-id="${hotel.sid || ""}">
                    <small class="price-room-label">예약 가능한 객실 최저가</small>
                    <strong class="price-value">₩${price.toLocaleString()}~</strong>
                    <span class="price-room-name">객실 확인 중</span>
                    <a class="reserve-btn" href="hotel-detail.html?id=${hotel.sid || ""}">예약하기</a>
                </div>
            </article>
        `;

        list.append(card);

    });

    loadHotelThumbnails(sortedHotels);
    loadLowestRoomPrices(sortedHotels);

}

function loadLowestRoomPrices(hotels) {
    hotels.forEach(function (hotel) {
        if (!hotel.sid) {
            return;
        }

        $.ajax({
            url: HOTEL_API_BASE + "/hotel/inroom/" + hotel.sid,
            type: "GET",
            success: function (result) {
                const rooms = (result.data || []).filter(function (room) {
                    return room.roomAvailable !== false && Number(room.roomPrice || 0) > 0;
                });
                const target = $(".hotel-price[data-hotel-id='" + hotel.sid + "']");

                if (rooms.length === 0) {
                    target.find(".price-room-label").text("예약 가능한 객실 없음");
                    target.find(".price-value").text("가격 확인");
                    target.find(".price-room-name").text("상세에서 객실 정보를 확인해주세요");
                    return;
                }

                const cheapest = rooms.sort(function (a, b) {
                    return Number(a.discountedRoomPrice || a.roomPrice || 0) - Number(b.discountedRoomPrice || b.roomPrice || 0);
                })[0];
                const basePrice = Number(cheapest.roomPrice || 0);
                const displayPrice = Number(cheapest.discountedRoomPrice || basePrice);
                const hasPromotion = cheapest.promotionDiscountAmount && displayPrice < basePrice;
                const hotelIndex = currentHotels.findIndex(function (item) {
                    return String(item.sid) === String(hotel.sid);
                });

                if (hotelIndex >= 0) {
                    currentHotels[hotelIndex].lowestRoomPrice = displayPrice;
                    currentHotels[hotelIndex].lowestRoomName = cheapest.roomName || cheapest.roomTypeTitle || "객실";
                }

                target.find(".price-room-label").text(hasPromotion ? "프로모션 최저가 · 1박 기준" : "1박 기준");
                target.find(".price-value").text("₩" + displayPrice.toLocaleString() + "~");
                target.find(".price-room-name").text((cheapest.roomName || cheapest.roomTypeTitle || "객실") + (hasPromotion ? " · " + (cheapest.promotionDiscountContent || "할인 적용") : ""));
                if (hasPromotion && !$(".hotel-thumb[data-hotel-id='" + hotel.sid + "'] .sale-badge").length) {
                    $(".hotel-thumb[data-hotel-id='" + hotel.sid + "']").append(`<span class="sale-badge show">${escapeHtml(cheapest.promotionDiscountContent || "SALE")}</span>`);
                }
            },
            error: function () {
                const target = $(".hotel-price[data-hotel-id='" + hotel.sid + "']");
                target.find(".price-room-label").text("객실 기준가 확인 필요");
                target.find(".price-room-name").text("상세에서 객실 정보를 확인해주세요");
            }
        });
    });
}

function sortHotels(hotels) {

    const sortType = $("#sortSelect").val();
    const copiedHotels = hotels.slice();

    if (sortType === "priceAsc") {
        return copiedHotels.sort(function (a, b) {
            return getSortPrice(a) - getSortPrice(b);
        });
    }

    if (sortType === "priceDesc") {
        return copiedHotels.sort(function (a, b) {
            return getSortPrice(b) - getSortPrice(a);
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
            url: HOTEL_API_BASE + "/hotel/inimage/" + hotel.sid,
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

            error: function () {}
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
    const cookieRequest = getCookieRequest();

    if (stored) {
        try {
            return JSON.parse(stored);
        } catch (error) {
            return cookieRequest;
        }
    }

    return cookieRequest;

}

function getSortPrice(hotel) {
    return Number(hotel.lowestRoomPrice || hotel.hotelPrice || 0);
}

function saveStoredRequest(request) {

    const value = JSON.stringify(request);
    sessionStorage.setItem("hotelSearchRequest", value);
    document.cookie = HOTEL_SEARCH_COOKIE + "=" + encodeURIComponent(value) + "; path=/; max-age=604800";

}

function getCookieRequest() {

    const row = document.cookie
        .split("; ")
        .find(function (item) {
            return item.indexOf(HOTEL_SEARCH_COOKIE + "=") === 0;
        });

    if (!row) {
        return null;
    }

    try {
        return JSON.parse(decodeURIComponent(row.split("=")[1]));
    } catch (error) {
        return null;
    }

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
