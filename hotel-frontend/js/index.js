const INDEX_SEARCH_COOKIE = "staynowSearchRequest";
const INDEX_API_BASE = window.StayNowConfig.apiBase;
const DEFAULT_LOCATION_SUGGESTIONS = [
    "서울특별시", "부산광역시", "제주특별자치도", "강릉", "경주", "여수", "속초", "인천", "대구", "대전", "광주", "전주", "수원", "가평", "춘천"
];

let indexHotelSuggestionItems = [];
let datePickerMonth = null;

$(function () {

    init();

});

function init() {
    setDefaultSearchValues();
    initSearchDateBounds();
    initSearchSuggestions();
    guestPicker();
    cardHover();
    travelTypeSelect();
    categoryTab();
    searchValidation();
    dealButton();
    recommendButton();
    loadFlashDeals();
    scrollAnimation();
}

/* ===========================
   여행 유형 선택
=========================== */

function travelTypeSelect() {

    $(".travel-card").on("click keydown", function (event) {

        if (event.type === "keydown" && event.key !== "Enter" && event.key !== " ") {
            return;
        }

        event.preventDefault();

        $(".travel-card").removeClass("selected");

        $(this).addClass("selected");

        const index = $(".travel-card").index(this);
        const presets = [
            { location: "", hotelTypeNames: ["호텔"], star: null },
            { location: "", hotelTypeNames: ["펜션/풀빌라"], star: null },
            { location: "", hotelTypeNames: ["리조트"], star: null },
            { location: "", hotelTypeNames: ["호텔"], star: 5 },
            { location: "커플", hotelTypeNames: ["호텔"], star: null }
        ];

        goHotelSearch(makePresetSearchRequest(presets[index]));

    });

}

/* ===========================
   인기 여행지 탭
=========================== */

function categoryTab() {

    $(".category-tab button").click(function () {

        $(".category-tab button").removeClass("active");
        $(this).addClass("active");

        let type = $(this).text();

        if (type === "전체") {

            $(".hotel-card").fadeIn(300);

        }

        else if (type === "국내") {

            $(".hotel-card").hide();

            $(".hotel-card").eq(0).fadeIn();
            $(".hotel-card").eq(1).fadeIn();
            $(".hotel-card").eq(2).fadeIn();
            $(".hotel-card").eq(3).fadeIn();

        }

        else {

            $(".hotel-card").fadeOut();

            alert("현재 해외 호텔 데이터가 없습니다.");

        }

    });

}

/* ===========================
   검색
=========================== */

function searchValidation() {

    $(document).on("click", ".search-btn", function (e) {

        e.preventDefault();

        let request = makeHotelSearchRequest();
        let checkInDate = $("#searchCheckIn").val();
        let checkOutDate = $("#searchCheckOut").val();

        if (request.location === "") {

            alert("여행지를 입력하세요.");

            return;

        }

        if (checkInDate === "") {

            alert("체크인 날짜를 선택하세요.");

            return;

        }

        if (checkOutDate === "") {

            alert("체크아웃 날짜를 선택하세요.");

            return;

        }

        if (isBeforeToday(checkInDate)) {

            alert("지난 날짜로는 체크인할 수 없습니다.");
            $("#dateRangeToggle").focus();

            return;

        }

        if (new Date(checkInDate) >= new Date(checkOutDate)) {

            alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");

            return;

        }

        searchHotels(request);

    });

}

function initSearchDateBounds() {

    normalizeSearchDateRange();
    initDateRangePicker();
    updateDateRangeText();

}

function updateSearchCheckoutMin(forceValid) {

    const checkIn = $("#searchCheckIn").val() || toDateInputValue(new Date());
    const minCheckout = addDateInputDays(checkIn, 1);

    $("#searchCheckOut").attr("min", minCheckout);

    if (forceValid && ($("#searchCheckOut").val() === "" || $("#searchCheckOut").val() <= checkIn)) {
        $("#searchCheckOut").val(minCheckout);
    }

}

function normalizeSearchDateRange() {

    const today = toDateInputValue(new Date());
    let checkIn = $("#searchCheckIn").val();
    let checkOut = $("#searchCheckOut").val();

    if (!checkIn || isBeforeToday(checkIn)) {
        checkIn = today;
        $("#searchCheckIn").val(checkIn);
    }

    if (!checkOut || checkOut <= checkIn) {
        checkOut = addDateInputDays(checkIn, 1);
        $("#searchCheckOut").val(checkOut);
    }

}

function setDefaultSearchValues() {

    const today = new Date();
    const tomorrow = new Date();

    tomorrow.setDate(today.getDate() + 1);

    if ($("#searchCheckIn").val() === "") {
        $("#searchCheckIn").val(toDateInputValue(today));
    }

    if ($("#searchCheckOut").val() === "") {
        $("#searchCheckOut").val(toDateInputValue(tomorrow));
    }

}

function addDateInputDays(value, days) {

    const date = new Date(value + "T00:00:00");
    date.setDate(date.getDate() + days);

    return toDateInputValue(date);

}

function isBeforeToday(value) {

    return value && value < toDateInputValue(new Date());

}

function toDateInputValue(date) {

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return year + "-" + month + "-" + day;

}

function initSearchSuggestions() {

    const input = $("#searchLocation");
    const suggestions = $("#searchSuggestions");

    if (!input.length || !suggestions.length) {
        return;
    }

    loadIndexHotelSuggestions();

    input.on("focus input", function () {
        renderSearchSuggestions($(this).val());
    });

    input.on("keydown", function (event) {
        if (event.key === "Escape") {
            hideSearchSuggestions();
        }
    });

    suggestions.on("mousedown", ".suggestion-item", function (event) {
        event.preventDefault();
        input.val($(this).data("value"));
        hideSearchSuggestions();
    });

    $(document).on("mousedown", function (event) {
        if (!$(event.target).closest(".location-input-wrap").length) {
            hideSearchSuggestions();
        }
    });

}

function loadIndexHotelSuggestions() {

    $.ajax({
        url: INDEX_API_BASE + "/hotel/all",
        type: "GET",
        timeout: 5000,
        success: function (result) {
            const hotels = Array.isArray(result)
                ? result
                : (result && Array.isArray(result.data)
                    ? result.data
                    : (result && Array.isArray(result.content) ? result.content : []));
            indexHotelSuggestionItems = hotels
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

function renderSearchSuggestions(keyword) {

    const suggestions = $("#searchSuggestions");
    const query = String(keyword || "").trim().toLowerCase();
    const locationItems = DEFAULT_LOCATION_SUGGESTIONS.map(function (name) {
        return {
            type: "지역",
            value: name,
            label: name,
            subtitle: "인기 여행지"
        };
    });
    const items = locationItems.concat(indexHotelSuggestionItems)
        .filter(function (item) {
            if (!query) {
                return item.type === "지역";
            }
            return String(item.label).toLowerCase().includes(query) || String(item.subtitle).toLowerCase().includes(query);
        })
        .slice(0, 8);

    if (!items.length) {
        suggestions.html(`
            <button type="button" class="suggestion-item" data-value="${escapeHtml(keyword)}">
                <span class="suggestion-main">
                    <span class="suggestion-name">${escapeHtml(keyword)}</span>
                    <span class="suggestion-sub">입력한 검색어로 바로 검색</span>
                </span>
                <span class="suggestion-type">검색</span>
            </button>
        `);
    } else {
        suggestions.html(items.map(function (item) {
            return `
                <button type="button" class="suggestion-item" role="option" data-value="${escapeHtml(item.value)}">
                    <span class="suggestion-main">
                        <span class="suggestion-name">${escapeHtml(item.label)}</span>
                        <span class="suggestion-sub">${escapeHtml(item.subtitle)}</span>
                    </span>
                    <span class="suggestion-type">${escapeHtml(item.type)}</span>
                </button>
            `;
        }).join(""));
    }

    suggestions.prop("hidden", false);
    $("#searchLocation").attr("aria-expanded", "true");

}

function hideSearchSuggestions() {

    $("#searchSuggestions").prop("hidden", true);
    $("#searchLocation").attr("aria-expanded", "false");

}

function initDateRangePicker() {

    if (!$("#dateRangeToggle").length) {
        return;
    }

    datePickerMonth = getMonthStart($("#searchCheckIn").val() || toDateInputValue(new Date()));
    renderDateRangePicker();

    $("#dateRangeToggle").on("click", function (event) {
        event.stopPropagation();
        const picker = $("#dateRangePicker");
        const willOpen = picker.prop("hidden");
        picker.prop("hidden", !willOpen);
        $(this).attr("aria-expanded", String(willOpen));
        if (willOpen) {
            renderDateRangePicker();
        }
    });

    $("#dateRangePicker").on("click", function (event) {
        event.stopPropagation();
    });

    $("#dateRangePicker").on("click", "[data-date-nav]", function () {
        datePickerMonth.setMonth(datePickerMonth.getMonth() + Number($(this).data("date-nav")));
        renderDateRangePicker();
    });

    $("#dateRangePicker").on("click", ".date-cell:not(.disabled)", function () {
        selectSearchDate($(this).data("date"));
    });

    $(document).on("click", function () {
        closeDateRangePicker();
    });

}

function renderDateRangePicker() {

    const monthOne = getMonthStart(datePickerMonth || new Date());
    const monthTwo = addMonths(monthOne, 1);
    const checkIn = $("#searchCheckIn").val();
    const checkOut = $("#searchCheckOut").val();

    $("#dateRangePicker").html(`
        <div class="date-picker-head">
            <button type="button" class="date-picker-nav" data-date-nav="-1" aria-label="이전 달">
                <i class="fa-solid fa-chevron-left" aria-hidden="true"></i>
            </button>
            <div class="date-picker-title">숙박 기간 선택</div>
            <button type="button" class="date-picker-nav" data-date-nav="1" aria-label="다음 달">
                <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
            </button>
        </div>
        <div class="date-picker-months">
            ${renderCalendarMonth(monthOne, checkIn, checkOut)}
            ${renderCalendarMonth(monthTwo, checkIn, checkOut)}
        </div>
        <p class="date-picker-hint">${checkIn && !checkOut ? "체크아웃 날짜를 선택해주세요." : "체크인은 오늘 이후, 체크아웃은 체크인 다음 날부터 선택할 수 있어요."}</p>
    `);

}

function renderCalendarMonth(monthDate, checkIn, checkOut) {

    const year = monthDate.getFullYear();
    const month = monthDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const start = new Date(firstDay);
    start.setDate(firstDay.getDate() - firstDay.getDay());

    let days = "";

    for (let i = 0; i < 42; i++) {
        const day = new Date(start);
        day.setDate(start.getDate() + i);
        const value = toDateInputValue(day);
        const isOtherMonth = day.getMonth() !== month;
        const isDisabled = isBeforeToday(value);
        const isStart = value === checkIn;
        const isEnd = value === checkOut;
        const inRange = checkIn && checkOut && value > checkIn && value < checkOut;
        const classes = [
            "date-cell",
            isOtherMonth ? "other-month" : "",
            isDisabled ? "disabled" : "",
            isStart ? "selected-start" : "",
            isEnd ? "selected-end" : "",
            inRange ? "in-range" : ""
        ].filter(Boolean).join(" ");

        days += `<button type="button" class="${classes}" data-date="${value}" ${isDisabled ? "disabled" : ""}>${day.getDate()}</button>`;
    }

    return `
        <div class="date-picker-month">
            <div class="date-picker-month-title">${year}년 ${month + 1}월</div>
            <div class="date-picker-week">
                <span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span>
            </div>
            <div class="date-picker-days">${days}</div>
        </div>
    `;

}

function selectSearchDate(value) {

    const checkIn = $("#searchCheckIn").val();
    const checkOut = $("#searchCheckOut").val();

    if (!checkIn || (checkIn && checkOut) || value <= checkIn) {
        $("#searchCheckIn").val(value);
        $("#searchCheckOut").val("");
    } else {
        $("#searchCheckOut").val(value);
        setTimeout(closeDateRangePicker, 120);
    }

    updateDateRangeText();
    renderDateRangePicker();

}

function updateDateRangeText() {

    const checkIn = $("#searchCheckIn").val();
    const checkOut = $("#searchCheckOut").val();
    const text = checkIn && checkOut
        ? formatDateRangeLabel(checkIn) + " - " + formatDateRangeLabel(checkOut)
        : (checkIn ? formatDateRangeLabel(checkIn) + " - 체크아웃 선택" : "날짜 선택");

    $("#dateRangeText").text(text);

}

function closeDateRangePicker() {

    $("#dateRangePicker").prop("hidden", true);
    $("#dateRangeToggle").attr("aria-expanded", "false");

}

function formatDateRangeLabel(value) {

    const date = new Date(value + "T00:00:00");
    const days = ["일", "월", "화", "수", "목", "금", "토"];

    return String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0") + " (" + days[date.getDay()] + ")";

}

function getMonthStart(value) {

    const date = value instanceof Date ? new Date(value) : new Date(value + "T00:00:00");
    return new Date(date.getFullYear(), date.getMonth(), 1);

}

function addMonths(value, count) {

    const date = new Date(value);
    date.setMonth(date.getMonth() + count);
    return date;

}

function makeHotelSearchRequest() {

    const checkIn = $("#searchCheckIn").val();
    const checkOut = $("#searchCheckOut").val();
    const adult = Number($("#adultCount").text());
    const child = Number($("#childCount").text());

    return {
        location: $("#searchLocation").val().trim(),
        checkIn: checkIn ? checkIn + "T15:00:00" : null,
        checkOut: checkOut ? checkOut + "T11:00:00" : null,
        adult: adult,
        child: child,
        minPrice: null,
        maxPrice: null,
        star: null,
        roomTypeIds: [],
        hotelTypeNames: []
    };

}

function guestPicker() {

    $("#guestToggle").click(function (e) {
        e.stopPropagation();
        $("#guestPopover").toggleClass("open");
    });

    $("#guestPopover").click(function (e) {
        e.stopPropagation();
    });

    $(document).click(function () {
        $("#guestPopover").removeClass("open");
    });

    $(".guest-plus").click(function () {
        changeGuestCount($(this).data("target"), 1);
    });

    $(".guest-minus").click(function () {
        changeGuestCount($(this).data("target"), -1);
    });

    updateGuestToggle();

}

function changeGuestCount(type, delta) {

    const target = type === "adult" ? $("#adultCount") : $("#childCount");
    const min = type === "adult" ? 1 : 0;
    const max = 10;
    const next = Math.max(min, Math.min(max, Number(target.text()) + delta));

    target.text(next);
    updateGuestToggle();

}

function updateGuestToggle() {

    const adult = Number($("#adultCount").text());
    const child = Number($("#childCount").text());

    $("#guestToggle").text("성인 " + adult + "명 · 어린이 " + child + "명");
    $(".guest-minus[data-target='adult']").prop("disabled", adult <= 1);
    $(".guest-minus[data-target='child']").prop("disabled", child <= 0);
    $(".guest-plus").prop("disabled", adult + child >= 10);

}

function searchHotels(request) {

    $(".search-btn")
        .addClass("disabled")
        .html('<i class="fa-solid fa-spinner fa-spin"></i> 검색중');

    $.ajax({
        url: INDEX_API_BASE + "/hotel/search?page=0&size=5",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),
        timeout: 5000,

        success: function (result) {

            saveHotelSearchRequest(request);
            sessionStorage.setItem("hotelSearchResult", JSON.stringify(result));
            location.href = "hotel-search.html";

        },

        error: function () {

            alert("호텔 검색 중 오류가 발생했습니다.");

        },

        complete: function () {

            $(".search-btn")
                .removeClass("disabled")
                .html('<i class="fa-solid fa-magnifying-glass"></i> 검색');

        }
    });

}

function makePresetSearchRequest(preset) {

    const checkIn = $("#searchCheckIn").val();
    const checkOut = $("#searchCheckOut").val();
    const adult = Number($("#adultCount").text());
    const child = Number($("#childCount").text());

    return {
        location: preset.location,
        checkIn: checkIn ? checkIn + "T15:00:00" : null,
        checkOut: checkOut ? checkOut + "T11:00:00" : null,
        adult: adult,
        child: child,
        minPrice: null,
        maxPrice: null,
        star: preset.star,
        roomTypeIds: preset.roomTypeIds || [],
        hotelTypeNames: preset.hotelTypeNames || []
    };

}

function goHotelSearch(request) {

    if (!isIndexSearchDateRequestValid(request)) {
        return;
    }

    $.ajax({
        url: INDEX_API_BASE + "/hotel/search?page=0&size=5",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),

        success: function (result) {
            saveHotelSearchRequest(request);
            sessionStorage.setItem("hotelSearchResult", JSON.stringify(result));
            location.href = "hotel-search.html";
        },

        error: function () {
            alert("호텔 검색 중 오류가 발생했습니다.");
        }
    });

}

function isIndexSearchDateRequestValid(request) {

    const checkIn = String(request.checkIn || "").slice(0, 10);
    const checkOut = String(request.checkOut || "").slice(0, 10);

    if (!checkIn || !checkOut) {
        alert("체크인과 체크아웃 날짜를 선택하세요.");
        return false;
    }

    if (isBeforeToday(checkIn)) {
        alert("지난 날짜로는 체크인할 수 없습니다.");
        $("#dateRangeToggle").focus();
        return false;
    }

    if (checkOut <= checkIn) {
        alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        $("#dateRangeToggle").focus();
        return false;
    }

    return true;

}

function saveHotelSearchRequest(request) {

    const value = JSON.stringify(request);
    sessionStorage.setItem("hotelSearchRequest", value);
    document.cookie = INDEX_SEARCH_COOKIE + "=" + encodeURIComponent(value) + "; path=/; max-age=604800";

}

/* ===========================
   특가 예약
=========================== */

function dealButton() {

    $(document).on("click", ".deal-body button", function () {

        const hotelId = $(this).data("hotel-id");

        if (hotelId) {
            location.href = "hotel-detail.html?id=" + hotelId;
        }

    });

}

/* ===========================
   추천 버튼
=========================== */

function recommendButton() {

    const recommendations = [
        {
            title: "서울 프리미엄 호캉스",
            desc: "5성급 호텔 중심으로 조용하고 편한 숙소를 추천합니다.",
            request: { location: "서울", hotelTypeNames: ["호텔"], star: 5 }
        },
        {
            title: "커플을 위한 로맨틱 스테이",
            desc: "이름과 설명에 커플 감성이 담긴 호텔을 찾아볼게요.",
            request: { location: "커플", hotelTypeNames: ["호텔"], star: null }
        },
        {
            title: "리조트에서 쉬어가는 하루",
            desc: "리조트 타입 객실 중심으로 여유로운 숙소를 추천합니다.",
            request: { location: "", hotelTypeNames: ["리조트"], star: null }
        }
    ];

    const auth = getStoredAuth();
    let currentIndex = new Date().getDate() % recommendations.length;

    $(".recommend .section-header h2").text((auth && auth.name ? auth.name + "님" : "고객님") + "을 위한 맞춤 추천");
    renderRecommendation(recommendations[currentIndex]);

    $(".recommend-text button").click(function (e) {
        e.preventDefault();
        goHotelSearch(makePresetSearchRequest(recommendations[currentIndex].request));
    });

    $(".recommend .section-header a").click(function (e) {
        e.preventDefault();
        currentIndex = (currentIndex + 1) % recommendations.length;
        renderRecommendation(recommendations[currentIndex]);
    });

}

function renderRecommendation(recommendation) {

    $(".recommend-text h2").text(recommendation.title);
    $(".recommend-text p").text(recommendation.desc);

}

function getStoredAuth() {

    const stored = localStorage.getItem("staynowAuth") || sessionStorage.getItem("staynowAuth");

    if (!stored) {
        return null;
    }

    try {
        return JSON.parse(stored);
    } catch (e) {
        return null;
    }

}

/* ===========================
   Hover 효과
=========================== */

function cardHover() {

    $(".hotel-card, .deal-card").hover(

        function () {

            $(this).stop().css({

                transform: "translateY(-10px)",

                transition: ".3s"

            });

        },

        function () {

            $(this).stop().css({

                transform: "translateY(0)"

            });

        }

    );

}

/* ===========================
   스크롤 등장 효과
=========================== */

function scrollAnimation() {

    $(window).scroll(function () {

        $(".section").each(function () {

            let top = $(this).offset().top;

            let scroll = $(window).scrollTop();

            let windowHeight = $(window).height();

            if (scroll + windowHeight > top + 100) {

                $(this).css({

                    opacity: 1,

                    transform: "translateY(0)",

                    transition: ".8s"

                });

            }

        });

    }).trigger("scroll");

}

/* ===========================
   가장 인기 있는 호텔 4개 검색
=========================== */

$(function () {

    loadPopularHotels();

});

function loadPopularHotels() {

    $.ajax({

        url: INDEX_API_BASE + "/hotel/pop4",
        type: "GET",

        success: function (result) {

            drawPopularHotels(result.data);

        },

        error: function () {}

    });

}

function drawPopularHotels(hotels) {

    $(".hotel-grid").empty();

    $.each(hotels, function(index, hotel){

        const rating = Number(hotel.rating || 0);
        const hotelId = encodeURIComponent(hotel.sid || "");
        const hotelName = escapeHtml(hotel.hotelName || "호텔명 없음");
        const hotelLocation = escapeHtml(hotel.location || "위치 정보 없음");
        const firstImage = normalizeImagePath(hotel.firstImage);
        const price = Number(hotel.price || hotel.hotelPrice || 0);
        let card = `
            <a class="hotel-card" href="hotel-detail.html?id=${hotelId}">

                <div class="hotel-image">
                    <img src="${firstImage}" alt="${hotelName}">
                </div>

                <div class="hotel-body">

                    <div class="title-row">
                        <h3>${hotelName}</h3>
                        <span class="price">₩${price.toLocaleString()}</span>
                    </div>

                    <p>${hotelLocation}</p>

                    <div class="card-footer">
                        <span>${formatPopularRating(rating)}</span>
                    </div>

                </div>

            </a>
        `;

        $(".hotel-grid").append(card);

    });

}

function formatPopularRating(rating) {
    if (!rating || rating <= 0) {
        return "평점 없음";
    }

    const filled = Math.max(0, Math.min(5, Math.round(rating)));
    return '<b class="popular-stars">' + "★".repeat(filled) + "☆".repeat(5 - filled) + '</b> ' + rating.toFixed(1);
}

function loadFlashDeals() {

    $(".flash").hide();

    const request = makePresetSearchRequest({
        location: "",
        roomTypeIds: [],
        hotelTypeNames: [],
        star: null
    });

    $.ajax({
        url: INDEX_API_BASE + "/hotel/search?page=0&size=20",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),

        success: function (result) {
            const hotels = (result.data && result.data.content ? result.data.content : [])
                .filter(function (hotel) {
                    return hotel.maxDiscountRate && hotel.maxDiscountRate > 0;
                })
                .slice(0, 3);

            if (hotels.length === 0) {
                $(".flash").hide();
                return;
            }

            drawFlashDeals(hotels);
            $(".flash").show();
        },

        error: function () {
            $(".flash").hide();
        }
    });

}

function drawFlashDeals(hotels) {

    $(".deal-grid").empty();

    $.each(hotels, function (index, hotel) {
        const discountRate = hotel.maxDiscountRate || 0;
        const price = hotel.hotelPrice || 0;
        const discountedPrice = Math.floor(price * (100 - discountRate) / 100);
        const hotelId = escapeHtml(hotel.sid || "");
        const hotelName = escapeHtml(hotel.hotelName || "호텔명 없음");
        const card = `
            <div class="deal-card" data-hotel-id="${hotelId}">
                <div class="discount">-${discountRate}%</div>
                <img src="https://gunsancci.korcham.net/images/no-image01.gif" alt="${hotelName}">

                <div class="deal-body">
                    <h3>${hotelName}</h3>
                    <p class="old-price" data-price-role="old">₩${price.toLocaleString()}</p>
                    <p class="new-price" data-price-role="new">₩${discountedPrice.toLocaleString()}</p>
                    <button type="button" data-hotel-id="${hotelId}">예약하기</button>
                </div>
            </div>
        `;

        $(".deal-grid").append(card);
        loadDealImage(hotel.sid);
        loadDealRoomPrice(hotel.sid, discountRate);
    });

}

function loadDealRoomPrice(hotelId, discountRate) {

    $.ajax({
        url: INDEX_API_BASE + "/hotel/inroom/" + hotelId,
        type: "GET",

        success: function (result) {
            const rooms = (result.data || []).filter(function (room) {
                return room.roomAvailable !== false;
            });

            if (rooms.length === 0) {
                return;
            }

            const minRoom = rooms.reduce(function (minRoom, room) {
                const price = Number(room.discountedRoomPrice || room.roomPrice || 0);
                const minPrice = Number(minRoom.discountedRoomPrice || minRoom.roomPrice || 0);
                return price > 0 && price < minPrice ? room : minRoom;
            }, rooms[0]);
            const minRoomPrice = rooms.reduce(function (min, room) {
                const price = room.roomPrice || min;
                return Math.min(min, price);
            }, rooms[0].roomPrice || 0);

            const discountedPrice = Number(minRoom.discountedRoomPrice || Math.floor(minRoomPrice * (100 - discountRate) / 100));
            const dealCard = $(".deal-card[data-hotel-id='" + hotelId + "']");

            dealCard.find("[data-price-role='old']").text("₩" + minRoomPrice.toLocaleString());
            dealCard.find("[data-price-role='new']").text("₩" + discountedPrice.toLocaleString());
        }
    });

}

function loadDealImage(hotelId) {

    $.ajax({
        url: INDEX_API_BASE + "/hotel/inimage/" + hotelId,
        type: "GET",

        success: function (result) {
            const photos = result.data || [];

            if (photos.length === 0 || !photos[0].imagePath) {
                return;
            }

            $(".deal-card[data-hotel-id='" + hotelId + "'] img")
                .attr("src", normalizeImagePath(photos[0].imagePath));
        }
    });

}

function normalizeImagePath(imagePath) {

    if (!imagePath) {
        return "https://gunsancci.korcham.net/images/no-image01.gif";
    }

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

function escapeHtml(value) {

    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}

