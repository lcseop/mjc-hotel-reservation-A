const GLOBAL_HOTEL_SEARCH_API = window.StayNowConfig.apiUrl("/hotel/search");
const GLOBAL_SEARCH_COOKIE = "staynowSearchRequest";
const GLOBAL_LOCATION_SUGGESTIONS = [
    "서울특별시", "부산광역시", "제주특별자치도", "강릉", "경주", "여수", "속초", "인천", "대구", "대전", "광주", "전주", "수원", "가평", "춘천"
];
let globalSearchInitialized = false;
let globalHotelSuggestions = [];
let globalDatePickerMonth = null;

$(function () {
    initGlobalSearchHeader();
});

document.addEventListener("component:loaded", function (event) {
    if (event.detail && event.detail.id === "searchHeader") {
        initGlobalSearchHeader();
    }
});

function initGlobalSearchHeader() {
    if (globalSearchInitialized || !$("#globalSearchBtn").length) {
        return;
    }

    globalSearchInitialized = true;
    restoreGlobalSearchRequest();
    initGlobalDateBounds();
    initGlobalSearchSuggestions();

    $("#globalSearchBtn").on("click", function () {
        requestGlobalSearch();
    });
}

function requestGlobalSearch() {
    const request = makeGlobalSearchRequest();

    if (!validateGlobalSearchRequest(request)) {
        return;
    }

    saveGlobalSearchRequest(request);
    $("#globalSearchBtn").prop("disabled", true);

    $.ajax({
        url: GLOBAL_HOTEL_SEARCH_API + "?page=0&size=5",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(request),
        success: function (result) {
            sessionStorage.setItem("hotelSearchResult", JSON.stringify(result));
            window.location.href = "hotel-search.html";
        },
        error: function () {
            alert("호텔 검색 결과를 불러오지 못했습니다.");
        },
        complete: function () {
            $("#globalSearchBtn").prop("disabled", false);
        }
    });
}

function makeGlobalSearchRequest() {
    const checkIn = $("#globalSearchCheckIn").val();
    const checkOut = $("#globalSearchCheckOut").val();

    return {
        location: $("#globalSearchLocation").val().trim(),
        checkIn: checkIn ? checkIn + "T15:00:00" : null,
        checkOut: checkOut ? checkOut + "T11:00:00" : null,
        adult: Number($("#globalSearchAdult").val()) || 1,
        child: Number($("#globalSearchChild").val()) || 0,
        minPrice: null,
        maxPrice: null,
        star: null,
        roomTypeIds: []
    };
}

function validateGlobalSearchRequest(request) {
    if (!request.location) {
        alert("여행지 또는 호텔명을 입력하세요.");
        $("#globalSearchLocation").focus();
        return false;
    }

    if (!request.checkIn) {
        alert("체크인 날짜를 선택하세요.");
        $("#globalDateRangeToggle").focus();
        return false;
    }

    if (!request.checkOut) {
        alert("체크아웃 날짜를 선택하세요.");
        $("#globalDateRangeToggle").focus();
        return false;
    }

    if (isPastGlobalDate($("#globalSearchCheckIn").val())) {
        alert("지난 날짜로는 체크인할 수 없습니다.");
        $("#globalDateRangeToggle").focus();
        return false;
    }

    if (new Date($("#globalSearchCheckIn").val()) >= new Date($("#globalSearchCheckOut").val())) {
        alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        $("#globalDateRangeToggle").focus();
        return false;
    }

    return true;
}

function restoreGlobalSearchRequest() {
    const request = getGlobalSearchRequest() || getDefaultGlobalSearchRequest();

    $("#globalSearchLocation").val(request.location || "서울특별시");
    $("#globalSearchCheckIn").val(toGlobalDateValue(request.checkIn));
    $("#globalSearchCheckOut").val(toGlobalDateValue(request.checkOut));
    $("#globalSearchAdult").val(request.adult || 2);
    $("#globalSearchChild").val(request.child || 0);
}

function initGlobalDateBounds() {
    normalizeGlobalSearchDateRange();
    initGlobalDateRangePicker();
    updateGlobalDateRangeText();
}

function updateGlobalCheckoutMin(forceValid) {
    const checkIn = $("#globalSearchCheckIn").val() || toGlobalInputDate(new Date());
    const minCheckout = toGlobalInputDate(addGlobalDays(new Date(checkIn + "T00:00:00"), 1));

    $("#globalSearchCheckOut").attr("min", minCheckout);

    if (forceValid && (!$("#globalSearchCheckOut").val() || $("#globalSearchCheckOut").val() <= checkIn)) {
        $("#globalSearchCheckOut").val(minCheckout);
    }
}

function normalizeGlobalSearchDateRange() {
    const today = toGlobalInputDate(new Date());
    let checkIn = $("#globalSearchCheckIn").val();
    let checkOut = $("#globalSearchCheckOut").val();

    if (!checkIn || isPastGlobalDate(checkIn)) {
        checkIn = today;
        $("#globalSearchCheckIn").val(checkIn);
    }

    if (!checkOut || checkOut <= checkIn) {
        $("#globalSearchCheckOut").val(toGlobalInputDate(addGlobalDays(new Date(checkIn + "T00:00:00"), 1)));
    }
}

function initGlobalSearchSuggestions() {
    const input = $("#globalSearchLocation");
    const suggestions = $("#globalSearchSuggestions");

    if (!input.length || !suggestions.length) {
        return;
    }

    loadGlobalHotelSuggestions();

    input.on("focus input", function () {
        renderGlobalSearchSuggestions($(this).val());
    });

    input.on("keydown", function (event) {
        if (event.key === "Escape") {
            hideGlobalSearchSuggestions();
        }
    });

    suggestions.on("mousedown", ".global-suggestion-item", function (event) {
        event.preventDefault();
        input.val($(this).data("value"));
        hideGlobalSearchSuggestions();
    });

    $(document).on("mousedown", function (event) {
        if (!$(event.target).closest(".global-location-wrap").length) {
            hideGlobalSearchSuggestions();
        }
    });
}

function loadGlobalHotelSuggestions() {
    $.ajax({
        url: window.StayNowConfig.apiUrl("/hotel/all"),
        type: "GET",
        timeout: 5000,
        success: function (result) {
            const hotels = Array.isArray(result)
                ? result
                : (result && Array.isArray(result.data)
                    ? result.data
                    : (result && Array.isArray(result.content) ? result.content : []));

            globalHotelSuggestions = hotels
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

function renderGlobalSearchSuggestions(keyword) {
    const suggestions = $("#globalSearchSuggestions");
    const query = String(keyword || "").trim().toLowerCase();
    const locationItems = GLOBAL_LOCATION_SUGGESTIONS.map(function (name) {
        return {
            type: "지역",
            value: name,
            label: name,
            subtitle: "인기 여행지"
        };
    });
    const items = locationItems.concat(globalHotelSuggestions)
        .filter(function (item) {
            if (!query) {
                return item.type === "지역";
            }
            return String(item.label).toLowerCase().includes(query) || String(item.subtitle).toLowerCase().includes(query);
        })
        .slice(0, 8);

    if (!items.length) {
        suggestions.html(`
            <button type="button" class="global-suggestion-item" data-value="${escapeGlobalHtml(keyword)}">
                <span class="global-suggestion-main">
                    <span class="global-suggestion-name">${escapeGlobalHtml(keyword)}</span>
                    <span class="global-suggestion-sub">입력한 검색어로 바로 검색</span>
                </span>
                <span class="global-suggestion-type">검색</span>
            </button>
        `);
    } else {
        suggestions.html(items.map(function (item) {
            return `
                <button type="button" class="global-suggestion-item" role="option" data-value="${escapeGlobalHtml(item.value)}">
                    <span class="global-suggestion-main">
                        <span class="global-suggestion-name">${escapeGlobalHtml(item.label)}</span>
                        <span class="global-suggestion-sub">${escapeGlobalHtml(item.subtitle)}</span>
                    </span>
                    <span class="global-suggestion-type">${escapeGlobalHtml(item.type)}</span>
                </button>
            `;
        }).join(""));
    }

    suggestions.prop("hidden", false);
    $("#globalSearchLocation").attr("aria-expanded", "true");
}

function hideGlobalSearchSuggestions() {
    $("#globalSearchSuggestions").prop("hidden", true);
    $("#globalSearchLocation").attr("aria-expanded", "false");
}

function initGlobalDateRangePicker() {
    if (!$("#globalDateRangeToggle").length) {
        return;
    }

    globalDatePickerMonth = getGlobalMonthStart($("#globalSearchCheckIn").val() || toGlobalInputDate(new Date()));
    renderGlobalDateRangePicker();

    $("#globalDateRangeToggle").on("click", function (event) {
        event.stopPropagation();
        const picker = $("#globalDateRangePicker");
        const willOpen = picker.prop("hidden");
        picker.prop("hidden", !willOpen);
        $(this).attr("aria-expanded", String(willOpen));
        if (willOpen) {
            renderGlobalDateRangePicker();
        }
    });

    $("#globalDateRangePicker").on("click", function (event) {
        event.stopPropagation();
    });

    $("#globalDateRangePicker").on("click", "[data-global-date-nav]", function () {
        globalDatePickerMonth.setMonth(globalDatePickerMonth.getMonth() + Number($(this).data("global-date-nav")));
        renderGlobalDateRangePicker();
    });

    $("#globalDateRangePicker").on("click", ".global-date-cell:not(.disabled)", function () {
        selectGlobalSearchDate($(this).data("date"));
    });

    $(document).on("click", function () {
        closeGlobalDateRangePicker();
    });
}

function renderGlobalDateRangePicker() {
    const monthOne = getGlobalMonthStart(globalDatePickerMonth || new Date());
    const monthTwo = addGlobalMonths(monthOne, 1);
    const checkIn = $("#globalSearchCheckIn").val();
    const checkOut = $("#globalSearchCheckOut").val();

    $("#globalDateRangePicker").html(`
        <div class="global-date-picker-head">
            <button type="button" class="global-date-picker-nav" data-global-date-nav="-1" aria-label="이전 달">
                <i class="fa-solid fa-chevron-left" aria-hidden="true"></i>
            </button>
            <div class="global-date-picker-title">숙박 기간을 선택하세요</div>
            <button type="button" class="global-date-picker-nav" data-global-date-nav="1" aria-label="다음 달">
                <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
            </button>
        </div>
        <div class="global-date-months">
            ${renderGlobalCalendarMonth(monthOne, checkIn, checkOut)}
            ${renderGlobalCalendarMonth(monthTwo, checkIn, checkOut)}
        </div>
        <p class="global-date-hint">${checkIn && !checkOut ? "체크아웃 날짜를 선택해주세요." : "체크인은 오늘 이후, 체크아웃은 체크인 다음 날부터 선택할 수 있어요."}</p>
    `);
}

function renderGlobalCalendarMonth(monthDate, checkIn, checkOut) {
    const year = monthDate.getFullYear();
    const month = monthDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const start = new Date(firstDay);
    start.setDate(firstDay.getDate() - firstDay.getDay());
    let days = "";

    for (let i = 0; i < 42; i++) {
        const day = new Date(start);
        day.setDate(start.getDate() + i);
        const value = toGlobalInputDate(day);
        const isOtherMonth = day.getMonth() !== month;
        const isDisabled = isPastGlobalDate(value);
        const isStart = value === checkIn;
        const isEnd = value === checkOut;
        const inRange = checkIn && checkOut && value > checkIn && value < checkOut;
        const classes = [
            "global-date-cell",
            isOtherMonth ? "other-month" : "",
            isDisabled ? "disabled" : "",
            isStart ? "selected-start" : "",
            isEnd ? "selected-end" : "",
            inRange ? "in-range" : ""
        ].filter(Boolean).join(" ");

        days += `<button type="button" class="${classes}" data-date="${value}" ${isDisabled ? "disabled" : ""}>${day.getDate()}</button>`;
    }

    return `
        <div class="global-date-month">
            <div class="global-date-month-title">${year}년 ${month + 1}월</div>
            <div class="global-date-week">
                <span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span>
            </div>
            <div class="global-date-days">${days}</div>
        </div>
    `;
}

function selectGlobalSearchDate(value) {
    const checkIn = $("#globalSearchCheckIn").val();
    const checkOut = $("#globalSearchCheckOut").val();

    if (!checkIn || (checkIn && checkOut) || value <= checkIn) {
        $("#globalSearchCheckIn").val(value);
        $("#globalSearchCheckOut").val("");
    } else {
        $("#globalSearchCheckOut").val(value);
        setTimeout(closeGlobalDateRangePicker, 120);
    }

    updateGlobalDateRangeText();
    renderGlobalDateRangePicker();
}

function updateGlobalDateRangeText() {
    const checkIn = $("#globalSearchCheckIn").val();
    const checkOut = $("#globalSearchCheckOut").val();
    const text = checkIn && checkOut
        ? formatGlobalDateLabel(checkIn) + " - " + formatGlobalDateLabel(checkOut)
        : (checkIn ? formatGlobalDateLabel(checkIn) + " - 체크아웃 선택" : "날짜 선택");

    $("#globalDateRangeText").text(text);
}

function closeGlobalDateRangePicker() {
    $("#globalDateRangePicker").prop("hidden", true);
    $("#globalDateRangeToggle").attr("aria-expanded", "false");
}

function formatGlobalDateLabel(value) {
    const date = new Date(value + "T00:00:00");
    const days = ["일", "월", "화", "수", "목", "금", "토"];

    return String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0") + " (" + days[date.getDay()] + ")";
}

function getGlobalMonthStart(value) {
    const date = value instanceof Date ? new Date(value) : new Date(value + "T00:00:00");
    return new Date(date.getFullYear(), date.getMonth(), 1);
}

function addGlobalMonths(value, count) {
    const date = new Date(value);
    date.setMonth(date.getMonth() + count);
    return date;
}

function isPastGlobalDate(value) {
    return value && value < toGlobalInputDate(new Date());
}

function getDefaultGlobalSearchRequest() {
    const today = new Date();
    const tomorrow = addGlobalDays(today, 1);

    return {
        location: "서울특별시",
        checkIn: toGlobalInputDate(today) + "T15:00:00",
        checkOut: toGlobalInputDate(tomorrow) + "T11:00:00",
        adult: 2,
        child: 0
    };
}

function getGlobalSearchRequest() {
    const sessionRequest = readGlobalSearchSession();
    const cookieRequest = readGlobalSearchCookie();

    return sessionRequest || cookieRequest;
}

function readGlobalSearchSession() {
    try {
        const raw = sessionStorage.getItem("hotelSearchRequest");
        return raw ? JSON.parse(raw) : null;
    } catch (error) {
        return null;
    }
}

function readGlobalSearchCookie() {
    const row = document.cookie
        .split("; ")
        .find(function (value) {
            return value.indexOf(GLOBAL_SEARCH_COOKIE + "=") === 0;
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

function saveGlobalSearchRequest(request) {
    const value = encodeURIComponent(JSON.stringify(request));
    document.cookie = GLOBAL_SEARCH_COOKIE + "=" + value + "; path=/; max-age=604800";
    sessionStorage.setItem("hotelSearchRequest", JSON.stringify(request));
}

function toGlobalDateValue(value) {
    if (!value) {
        return "";
    }

    return String(value).slice(0, 10);
}

function toGlobalInputDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return year + "-" + month + "-" + day;
}

function addGlobalDays(date, days) {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
}

function escapeGlobalHtml(value) {
    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
