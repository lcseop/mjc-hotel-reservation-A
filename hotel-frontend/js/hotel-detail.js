const API_BASE = "http://localhost:33000/api";
const FALLBACK_IMAGE = "https://gunsancci.korcham.net/images/no-image01.gif";

let hotelImages = [];

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

        const selected = {
            hotelId: new URLSearchParams(location.search).get("id"),
            roomId: $(this).data("room-id")
        };

        sessionStorage.setItem("selectedReservationRoom", JSON.stringify(selected));
        alert("객실이 선택되었습니다. 예약 정보 입력 화면과 연결하면 바로 이어서 사용할 수 있습니다.");
    });
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
    const description = hotel.description || "편안한 휴식과 여행을 위한 StayNow 추천 호텔입니다. 객실과 편의시설을 확인하고 원하는 숙박 옵션을 선택해보세요.";
    const maxDiscountRate = hotel.maxDiscountRate || 0;

    $("#breadcrumbHotel").text(hotel.hotelName || "호텔 상세");
    $("#hotelName").text(hotel.hotelName || "호텔명 없음");
    $("#hotelLocation, #mapAddress").text(hotel.location || "위치 정보 없음");
    $("#hotelDescription").text(description);
    $("#hotelStars").text(drawStars(hotel.starRating) + " " + (hotel.starRating || 0) + "성급");

    if (maxDiscountRate > 0) {
        $("#detailSaleBadge").addClass("show").text("SALE " + maxDiscountRate + "%");
    }
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

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
