const GLOBAL_HOTEL_SEARCH_API = window.StayNowConfig.apiUrl("/hotel/search");
const GLOBAL_SEARCH_COOKIE = "staynowSearchRequest";
let globalSearchInitialized = false;

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
        $("#globalSearchCheckIn").focus();
        return false;
    }

    if (!request.checkOut) {
        alert("체크아웃 날짜를 선택하세요.");
        $("#globalSearchCheckOut").focus();
        return false;
    }

    if (isPastGlobalDate($("#globalSearchCheckIn").val())) {
        alert("지난 날짜로는 체크인할 수 없습니다.");
        $("#globalSearchCheckIn").focus();
        return false;
    }

    if (new Date($("#globalSearchCheckIn").val()) >= new Date($("#globalSearchCheckOut").val())) {
        alert("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        $("#globalSearchCheckOut").focus();
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
    const today = toGlobalInputDate(new Date());

    $("#globalSearchCheckIn").attr("min", today);

    if (isPastGlobalDate($("#globalSearchCheckIn").val())) {
        $("#globalSearchCheckIn").val(today);
    }

    updateGlobalCheckoutMin(true);

    $("#globalSearchCheckIn").on("change", function () {
        if (isPastGlobalDate($(this).val())) {
            $(this).val(today);
        }
        updateGlobalCheckoutMin(true);
    });

    $("#globalSearchCheckOut").on("change", function () {
        const minCheckout = $(this).attr("min");
        if ($(this).val() && $(this).val() < minCheckout) {
            $(this).val(minCheckout);
        }
    });
}

function updateGlobalCheckoutMin(forceValid) {
    const checkIn = $("#globalSearchCheckIn").val() || toGlobalInputDate(new Date());
    const minCheckout = toGlobalInputDate(addGlobalDays(new Date(checkIn + "T00:00:00"), 1));

    $("#globalSearchCheckOut").attr("min", minCheckout);

    if (forceValid && (!$("#globalSearchCheckOut").val() || $("#globalSearchCheckOut").val() <= checkIn)) {
        $("#globalSearchCheckOut").val(minCheckout);
    }
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
