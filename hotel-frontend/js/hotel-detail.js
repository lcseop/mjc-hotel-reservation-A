const API_BASE = "http://localhost:33000/api";
const FALLBACK_IMAGE = "https://gunsancci.korcham.net/images/no-image01.gif";
const KAKAO_MAP_APP_KEY = "4b9798cdfdfbd7e08b433eacafb45cfc";

let hotelImages = [];
let currentHotel = null;
let currentRooms = [];
let kakaoMapPromise = null;
let detailMap = null;
let detailMapOverlay = null;
let stationMarker = null;

$(function () {
    const hotelId = new URLSearchParams(location.search).get("id");

    if (!hotelId) {
        alert("호텔 정보가 없습니다.");
        location.href = "hotel-search.html";
        return;
    }

    bindEvent();
    loadHotelDetail(hotelId);
    loadHotelImages(hotelId);
    loadAmenities(hotelId);
    loadRooms(hotelId);
    loadReviews(hotelId);
});

function bindEvent() {
    $(document).on("click", ".thumb-btn", function () {
        setMainImage(Number($(this).data("index")));
    });

    $(".detail-tabs a").click(function () {
        $(".detail-tabs a").removeClass("active");
        $(this).addClass("active");
    });

    $(document).on("click", ".room-price a", function (e) {
        e.preventDefault();

        const roomId = $(this).data("room-id");
        const room = currentRooms.find(function (item) {
            return String(item.sid) === String(roomId);
        });
        const searchRequest = readJson("hotelSearchRequest") || {};
        const auth = getAuthData();

        if (!auth || !auth.memberSid) {
            alert("예약을 진행하려면 로그인이 필요합니다.");
            sessionStorage.setItem("afterLoginRedirect", location.href);
            location.href = "login.html";
            return;
        }

        const selected = {
            hotelId: new URLSearchParams(location.search).get("id"),
            roomId: roomId,
            hotel: currentHotel || {},
            room: room || {},
            searchRequest: searchRequest,
            selectedAt: new Date().toISOString()
        };

        sessionStorage.setItem("selectedReservationRoom", JSON.stringify(selected));
        location.href = "reservation.html";
    });

    $(document).on("click", ".station-card", function () {
        focusStation($(this).data("lat"), $(this).data("lng"), $(this).data("name"));
    });
}

function readJson(key) {
    try {
        const value = sessionStorage.getItem(key) || localStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function getAuthData() {
    return readJson("staynowAuth");
}

function loadHotelDetail(hotelId) {
    $.ajax({
        url: API_BASE + "/hotel/" + hotelId,
        type: "GET",
        success: function (result) {
            drawHotel(result.data || {});
        },
        error: function () {
            alert("호텔 정보를 불러오지 못했습니다.");
        }
    });
}

function drawHotel(hotel) {
    currentHotel = hotel;
    const description = hotel.description || "편안한 휴식과 여행을 위한 StayNow 추천 호텔입니다. 객실과 편의시설을 확인하고 원하는 숙박 옵션을 선택해보세요.";
    const maxDiscountRate = hotel.maxDiscountRate || 0;

    $("#breadcrumbHotel").text(hotel.hotelName || "호텔 상세");
    $("#hotelName").text(hotel.hotelName || "호텔명 없음");
    $("#hotelLocation, #mapAddress").text(hotel.location || "위치 정보 없음");
    $("#hotelDescription").text(description);
    $("#hotelStars").text(drawStars(hotel.starRating) + " " + (hotel.starRating || 0) + "성급");
    drawLocationAndTransport(hotel);

    if (maxDiscountRate > 0) {
        $("#detailSaleBadge").addClass("show").text("SALE " + maxDiscountRate + "%");
    }
}

function drawLocationAndTransport(hotel) {
    $("#mapAddress").text(hotel.location || "위치 정보 없음");

    if (!KAKAO_MAP_APP_KEY) {
        drawStationFallback("카카오맵 JavaScript 앱 키를 설정하면 지도와 주변 역 정보가 표시됩니다.");
        return;
    }

    loadKakaoMapSdk()
        .then(function () {
            resolveHotelLatLng(hotel, function (latLng) {
                if (!latLng) {
                    drawStationFallback("호텔 좌표를 확인하지 못했습니다.");
                    return;
                }

                drawKakaoMap(latLng, hotel);
                searchNearbyStations(latLng);
            });
        })
        .catch(function (error) {
            console.error("Kakao Map load failed:", error);
            drawStationFallback("카카오맵을 불러오지 못했습니다.");
        });
}

function loadKakaoMapSdk() {
    if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
        return Promise.resolve();
    }

    if (kakaoMapPromise) {
        return kakaoMapPromise;
    }

    kakaoMapPromise = new Promise(function (resolve, reject) {
        const script = document.createElement("script");
        script.src = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=" +
            encodeURIComponent(KAKAO_MAP_APP_KEY) +
            "&libraries=services&autoload=false";
        script.onload = function () {
            if (!window.kakao || !kakao.maps) {
                reject(new Error("Kakao Maps SDK was not initialized."));
                return;
            }

            kakao.maps.load(resolve);
        };
        script.onerror = function () {
            reject(new Error("Kakao Maps SDK script failed: " + script.src));
        };
        document.head.appendChild(script);
    });

    return kakaoMapPromise;
}

function resolveHotelLatLng(hotel, callback) {
    const latitude = Number(hotel.latitude);
    const longitude = Number(hotel.longitude);

    if (!Number.isNaN(latitude) && !Number.isNaN(longitude) && latitude !== 0 && longitude !== 0) {
        callback(new kakao.maps.LatLng(latitude, longitude));
        return;
    }

    if (!hotel.location) {
        callback(null);
        return;
    }

    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.addressSearch(hotel.location, function (result, status) {
        if (status !== kakao.maps.services.Status.OK || result.length === 0) {
            callback(null);
            return;
        }

        callback(new kakao.maps.LatLng(Number(result[0].y), Number(result[0].x)));
    });
}

function drawKakaoMap(latLng, hotel) {
    const mapContainer = document.getElementById("kakaoMap");
    mapContainer.innerHTML = "";

    const map = new kakao.maps.Map(mapContainer, {
        center: latLng,
        level: 4
    });
    detailMap = map;

    const marker = new kakao.maps.Marker({
        map: map,
        position: latLng
    });

    detailMapOverlay = createMapOverlay(hotel.hotelName || "호텔 위치", latLng);
    detailMapOverlay.setMap(map);

    setTimeout(function () {
        kakao.maps.event.trigger(map, "resize");
        map.setCenter(latLng);
    }, 100);
}

function searchNearbyStations(latLng) {
    const places = new kakao.maps.services.Places();

    places.categorySearch("SW8", function (result, status) {
        if (status !== kakao.maps.services.Status.OK || result.length === 0) {
            drawStationFallback("반경 2km 안의 지하철역 정보를 찾지 못했습니다.");
            return;
        }

        drawStations(result.slice(0, 4));
    }, {
        location: latLng,
        radius: 2000,
        sort: kakao.maps.services.SortBy.DISTANCE
    });
}

function drawStations(stations) {
    const stationList = $("#stationList");

    stationList.empty();

    stations.forEach(function (station) {
        const distance = Number(station.distance || 0);
        const walkMinutes = distance > 0 ? Math.max(1, Math.ceil(distance / 67)) : "-";
        const distanceText = distance > 0 ? formatDistance(distance) : "거리 확인 필요";

        stationList.append(`
            <button type="button" class="station-card" data-lat="${station.y}" data-lng="${station.x}" data-name="${escapeHtml(station.place_name || "지하철역")}">
                <i class="fa-solid fa-train-subway"></i>
                <div>
                    <strong>${escapeHtml(station.place_name || "지하철역")}</strong>
                    <span>${distanceText}</span>
                </div>
                <small>도보 ${walkMinutes}분</small>
            </button>
        `);
    });
}

function focusStation(lat, lng, name) {
    if (!detailMap || !window.kakao || lat == null || lng == null) {
        return;
    }

    const stationLatLng = new kakao.maps.LatLng(Number(lat), Number(lng));

    if (stationMarker) {
        stationMarker.setMap(null);
    }

    stationMarker = new kakao.maps.Marker({
        map: detailMap,
        position: stationLatLng
    });

    if (detailMapOverlay) {
        detailMapOverlay.setMap(null);
    }

    detailMapOverlay = createMapOverlay(name || "지하철역", stationLatLng);
    detailMapOverlay.setMap(detailMap);
    detailMap.panTo(stationLatLng);
    detailMap.setLevel(4);

    $(".station-card").removeClass("active");
    $(".station-card").filter(function () {
        return String($(this).data("lat")) === String(lat) && String($(this).data("lng")) === String(lng);
    }).addClass("active");
}

function createMapOverlay(title, position) {
    return new kakao.maps.CustomOverlay({
        position: position,
        yAnchor: 1.95,
        content: '<div class="map-custom-overlay">' + escapeHtml(title) + "</div>"
    });
}

function drawStationFallback(message) {
    $("#stationList").html('<div class="station-empty">' + escapeHtml(message) + "</div>");
}

function loadHotelImages(hotelId) {
    $.ajax({
        url: API_BASE + "/hotel/inimage/" + hotelId,
        type: "GET",
        success: function (result) {
            const photos = result.data || [];
            hotelImages = photos.length > 0
                ? photos.map(function (photo) { return normalizeImagePath(photo.imagePath); })
                : [FALLBACK_IMAGE];
            drawGallery();
        },
        error: function () {
            hotelImages = [FALLBACK_IMAGE];
            drawGallery();
        }
    });
}

function drawGallery() {
    const thumbnailList = $("#thumbnailList");
    const visibleImages = hotelImages.slice(0, 4);

    thumbnailList.empty();

    $.each(visibleImages, function (index, imagePath) {
        const remainCount = hotelImages.length - visibleImages.length;
        const moreOverlay = index === visibleImages.length - 1 && remainCount > 0
            ? `<span class="more-photos"><i class="fa-regular fa-images"></i> 사진 ${hotelImages.length}장</span>`
            : "";

        thumbnailList.append(`
            <button type="button" class="thumb-btn ${index === 0 ? "active" : ""}" data-index="${index}">
                <img src="${imagePath}" alt="호텔 이미지 ${index + 1}">
                ${moreOverlay}
            </button>
        `);
    });

    setMainImage(0);
}

function setMainImage(index) {
    const imagePath = hotelImages[index] || FALLBACK_IMAGE;

    $("#mainHotelImage").attr("src", imagePath);
    $("#photoCounter").text((index + 1) + " / " + hotelImages.length);
    $(".thumb-btn").removeClass("active");
    $(".thumb-btn[data-index='" + index + "']").addClass("active");
}

function loadAmenities(hotelId) {
    $.ajax({
        url: API_BASE + "/hotel/iname/" + hotelId,
        type: "GET",
        success: function (result) {
            drawAmenities(result.data || []);
        },
        error: function () {
            drawAmenities([]);
        }
    });
}

function drawAmenities(amenities) {
    const grid = $("#amenityGrid");

    grid.empty();

    if (amenities.length === 0) {
        grid.append('<div class="empty">등록된 편의시설 정보가 없습니다.</div>');
        return;
    }

    $.each(amenities, function (index, amenity) {
        grid.append(`
            <div class="amenity-item">
                <i class="fa-solid fa-circle-check"></i>
                <span>${escapeHtml(amenity.title || "편의시설")}</span>
            </div>
        `);
    });
}

function loadRooms(hotelId) {
    $.ajax({
        url: API_BASE + "/hotel/inroom/" + hotelId,
        type: "GET",
        success: function (result) {
            drawRooms(result.data || []);
        },
        error: function () {
            drawRooms([]);
        }
    });
}

function drawRooms(rooms) {
    const list = $("#roomList");

    currentRooms = rooms || [];
    list.empty();
    $("#roomCount").text(rooms.length + "개 객실");

    if (rooms.length === 0) {
        list.append('<div class="empty">예약 가능한 객실 정보가 없습니다.</div>');
        return;
    }

    $.each(rooms, function (index, room) {
        const image = room.roomPhotoPath ? normalizeImagePath(room.roomPhotoPath) : FALLBACK_IMAGE;
        const price = room.roomPrice || 0;

        list.append(`
            <article class="room-card" data-room-id="${room.sid}">
                <div class="room-image">
                    <img src="${image}" alt="${escapeHtml(room.roomName || "객실")} 이미지">
                </div>

                <div class="room-info">
                    <p class="room-type">${escapeHtml(room.roomTypeTitle || "객실")}</p>
                    <h3>${escapeHtml(room.roomName || "객실명 없음")}</h3>
                    <div class="room-specs">
                        <span>${room.area || "-"}㎡</span>
                        <span>최대 ${room.maximumPeople || "-"}명</span>
                        <span>${formatHour(room.checkInTime)} 체크인</span>
                        <span>${formatHour(room.checkOutTime)} 체크아웃</span>
                        <span>주차 ${formatValue(room.parking)}</span>
                    </div>
                </div>

                <div class="room-price">
                    <small>1박 기준</small>
                    <strong>₩${price.toLocaleString()}</strong>
                    <a href="#" data-room-id="${room.sid}">이 객실 선택</a>
                </div>
            </article>
        `);

        loadRoomImage(room.sid);
    });
}

function loadRoomImage(roomId) {
    $.ajax({
        url: API_BASE + "/room/inimage/" + roomId,
        type: "GET",
        success: function (result) {
            const photos = result.data || [];

            if (photos.length === 0 || !photos[0].imagePath) {
                return;
            }

            $(".room-card[data-room-id='" + roomId + "'] .room-image img")
                .attr("src", normalizeImagePath(photos[0].imagePath));

            currentRooms = currentRooms.map(function (room) {
                if (String(room.sid) === String(roomId)) {
                    return Object.assign({}, room, {
                        roomPhotoPath: normalizeImagePath(photos[0].imagePath)
                    });
                }
                return room;
            });
        }
    });
}

function loadReviews(hotelId) {
    $.ajax({
        url: API_BASE + "/hotel/inreview/" + hotelId + "?page=0&size=5",
        type: "GET",
        success: function (result) {
            const page = result.data || {};
            drawReviews(page.content || [], page.totalElements || 0);
        },
        error: function () {
            drawReviews([], 0);
        }
    });
}

function drawReviews(reviews, totalElements) {
    const list = $("#reviewList");
    const average = getAverageRating(reviews);

    list.empty();
    $("#reviewCount").text(totalElements.toLocaleString());
    $("#reviewTotalText").text(totalElements.toLocaleString() + "개 리뷰");
    $("#reviewScore").text(average.toFixed(1));
    updateScoreMetrics(average, totalElements);

    if (reviews.length === 0) {
        list.append('<div class="empty">아직 등록된 리뷰가 없습니다.</div>');
        return;
    }

    $.each(reviews, function (index, review) {
        list.append(`
            <article class="review-card">
                <div class="review-head">
                    <strong>${review.rating || 0}.0</strong>
                    <span>${formatDate(review.createdAt)}</span>
                </div>
                <p>${escapeHtml(review.content || "리뷰 내용이 없습니다.")}</p>
            </article>
        `);
    });
}

function getAverageRating(reviews) {
    if (reviews.length === 0) {
        return 0;
    }

    const sum = reviews.reduce(function (total, review) {
        return total + (review.rating || 0);
    }, 0);

    return sum / reviews.length;
}

function updateScoreMetrics(score, totalElements) {
    const safeScore = totalElements > 0 ? Math.max(0, Math.min(5, Number(score) || 0)) : 0;
    const percent = (safeScore / 5) * 100;

    $(".score-metrics b").each(function (index) {
        const colors = ["#10b981", "#0ea5e9", "#8b5cf6", "#f59e0b"];
        $(this).css({
            "--score-percent": percent + "%",
            "--score-color": colors[index] || "#10b981"
        });
    });
}

function drawStars(count) {
    const starCount = count || 0;

    return "★".repeat(starCount) + "☆".repeat(Math.max(0, 5 - starCount));
}

function normalizeImagePath(imagePath) {
    if (!imagePath) {
        return FALLBACK_IMAGE;
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

function formatHour(hour) {
    if (hour == null) {
        return "-";
    }

    return String(hour).padStart(2, "0") + ":00";
}

function formatValue(value) {
    return value || "-";
}

function formatDate(dateTime) {
    return dateTime ? dateTime.substring(0, 10).replaceAll("-", ".") : "";
}

function formatDistance(meters) {
    if (meters >= 1000) {
        return (meters / 1000).toFixed(1) + "km";
    }

    return meters.toLocaleString() + "m";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
