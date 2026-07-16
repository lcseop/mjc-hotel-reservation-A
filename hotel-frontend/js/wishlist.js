const WISHLIST_API_BASE = window.StayNowConfig.apiBase;
const WISHLIST_FALLBACK_IMAGE = "https://gunsancci.korcham.net/images/no-image01.gif";

let wishlistAuth = null;
let wishlistItems = [];
let wishlistFilter = "ALL";

$(function () {
    wishlistAuth = readWishlistJson("staynowAuth");

    if (!wishlistAuth || !wishlistAuth.memberSid) {
        alert("위시리스트를 보려면 로그인이 필요합니다.");
        sessionStorage.setItem("afterLoginRedirect", location.href);
        location.href = "login.html";
        return;
    }

    bindWishlistEvents();
    drawWishlistProfile();
    loadWishlistMember();
    loadWishlist();
});

function bindWishlistEvents() {
    $("#wishlistKeyword, #wishlistSort").on("input change", drawWishlist);

    $(".wishlist-filter-buttons button").on("click", function () {
        wishlistFilter = $(this).data("filter");
        $(".wishlist-filter-buttons button").removeClass("active");
        $(this).addClass("active");
        drawWishlist();
    });

    $(document).on("click", "[data-wishlist-remove]", function (event) {
        event.stopPropagation();
        removeWishlist($(this).data("wishlistRemove"), $(this).data("hotelId"));
    });

    $(document).on("click", ".wishlist-card", function () {
        const hotelId = $(this).data("hotelId");
        if (hotelId) {
            location.href = "hotel-detail.html?id=" + encodeURIComponent(hotelId);
        }
    });

    $(document).on("keydown", ".wishlist-card", function (event) {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            $(this).trigger("click");
        }
    });

    $("#sideLogoutBtn").on("click", function () {
        localStorage.removeItem("staynowAuth");
        sessionStorage.removeItem("staynowAuth");
        location.href = "index.html";
    });
}

function loadWishlist() {
    $.ajax({
        url: WISHLIST_API_BASE + "/wish",
        type: "GET",
        data: { memberId: wishlistAuth.memberSid },
        success: function (result) {
            wishlistItems = unwrapWishlist(result);
            enrichWishlistRooms().always(drawWishlist);
        },
        error: function () {
            $("#wishlistList").html('<div class="empty-state">위시리스트를 불러오지 못했습니다.</div>');
        }
    });
}

function enrichWishlistRooms() {
    const requests = wishlistItems.map(function (item) {
        if (!item.hotelId) return $.Deferred().resolve().promise();

        return $.ajax({
            url: WISHLIST_API_BASE + "/hotel/inroom/" + encodeURIComponent(item.hotelId),
            type: "GET"
        }).then(function (result) {
            const rooms = unwrapWishlist(result);
            const availableRooms = rooms
                .map(function (room) {
                    const basePrice = Number(room.roomPrice || 0);
                    const salePrice = Number(room.discountedRoomPrice || basePrice || 0);
                    const hasPromotion = Boolean(room.promotionDiscountAmount) && salePrice > 0 && salePrice < basePrice;
                    return {
                        price: salePrice || basePrice,
                        hasPromotion: hasPromotion,
                        promotionText: room.promotionDiscountContent || "할인중"
                    };
                })
                .filter(function (room) {
                    return room.price > 0;
                })
                .sort(function (a, b) {
                    return a.price - b.price;
                });

            const promotionRooms = availableRooms.filter(function (room) {
                return room.hasPromotion;
            });

            item.minRoomPrice = availableRooms.length ? availableRooms[0].price : Number(item.hotelPrice || 0);
            item.hasPromotion = promotionRooms.length > 0;
            item.promotionText = promotionRooms.length ? promotionRooms[0].promotionText : "";
        }, function () {
            item.minRoomPrice = Number(item.hotelPrice || 0);
            item.hasPromotion = false;
            item.promotionText = "";
        });
    });

    return $.when.apply($, requests);
}

function loadWishlistMember() {
    $.ajax({
        url: WISHLIST_API_BASE + "/member/" + wishlistAuth.memberSid,
        type: "GET",
        success: function (result) {
            const member = result && result.data ? result.data : result;
            if (member && typeof member === "object") {
                wishlistAuth.name = member.name || wishlistAuth.name;
                wishlistAuth.email = member.email || wishlistAuth.email;
                wishlistAuth.point = member.point || wishlistAuth.point || 0;
                drawWishlistProfile();
            }
        }
    });
}

function drawWishlistProfile() {
    const name = wishlistAuth.name || wishlistAuth.email || "회원";
    $("#profileInitial").text(String(name).slice(0, 1));
    $("#profileName").text(name);
    $("#profileEmail").text(wishlistAuth.email || "");
    $("#profilePoint").text(formatWishlistNumber(wishlistAuth.point || 0));
}

function drawWishlist() {
    const items = getFilteredWishlist();
    $("#wishlistCountSide, #navWishlistCount").text(items.length);

    if (!items.length) {
        $("#wishlistList").html('<div class="empty-state">조건에 맞는 찜한 호텔이 없습니다.</div>');
        return;
    }

    $("#wishlistList").html(items.map(renderWishlistCard).join(""));
}

function getFilteredWishlist() {
    const keyword = String($("#wishlistKeyword").val() || "").trim().toLowerCase();
    const sort = $("#wishlistSort").val();

    let items = wishlistItems.filter(function (item) {
        const text = [item.hotelName, item.location, item.typeTitle, item.description].join(" ").toLowerCase();
        if (keyword && !text.includes(keyword)) return false;
        if (wishlistFilter === "SALE" && !item.hasPromotion) return false;
        if (wishlistFilter === "FIVE" && Number(item.starRating || 0) !== 5) return false;
        if (wishlistFilter === "SEOUL" && !String(item.location || "").includes("서울")) return false;
        return true;
    });

    items = items.slice().sort(function (a, b) {
        if (sort === "priceAsc") return Number(a.minRoomPrice || a.hotelPrice || 0) - Number(b.minRoomPrice || b.hotelPrice || 0);
        if (sort === "priceDesc") return Number(b.minRoomPrice || b.hotelPrice || 0) - Number(a.minRoomPrice || a.hotelPrice || 0);
        if (sort === "starDesc") return Number(b.starRating || 0) - Number(a.starRating || 0);
        return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
    });

    return items;
}

function renderWishlistCard(item) {
    const image = resolveWishlistImage(item.imagePath);
    const price = Number(item.minRoomPrice || item.hotelPrice || 0);
    return `<article class="wishlist-card" data-hotel-id="${escapeWishlist(item.hotelId)}" tabindex="0" role="link" aria-label="${escapeWishlist(item.hotelName || "호텔")} 상세 보기">
        <button class="wishlist-remove-btn" type="button" data-wishlist-remove="${escapeWishlist(item.sid)}" data-hotel-id="${escapeWishlist(item.hotelId)}" aria-label="위시리스트에서 삭제">
            <i class="fa-solid fa-trash-can"></i>
        </button>
        <div class="wishlist-card-image">
            <img src="${escapeWishlist(image)}" alt="${escapeWishlist(item.hotelName || "호텔 이미지")}" onerror="this.src='${WISHLIST_FALLBACK_IMAGE}'">
            ${item.hasPromotion ? `<span class="sale-badge">${escapeWishlist(item.promotionText || "할인중")}</span>` : ""}
        </div>
        <div class="wishlist-card-body">
            <small>${drawWishlistStars(item.starRating)} ${escapeWishlist(item.starRating || 0)}성급 ${escapeWishlist(item.typeTitle || "")}</small>
            <h2>${escapeWishlist(item.hotelName || "호텔명 없음")}</h2>
            <p><i class="fa-solid fa-location-dot"></i> ${escapeWishlist(item.location || "위치 정보 없음")}</p>
            <p>${escapeWishlist(shortWishlistText(item.description || "편안한 여행을 위한 StayNow 추천 숙소입니다.", 82))}</p>
            <div class="wishlist-meta">
                ${item.hasPromotion ? `<span>할인중</span>` : ""}
            </div>
            <div class="wishlist-price-row">
                <span>객실 최저가</span>
                <strong>${price ? formatWishlistWon(price) : "-"}~</strong>
            </div>
        </div>
    </article>`;
}

function removeWishlist(wishlistId, hotelId) {
    if (!confirm("위시리스트에서 삭제할까요?")) return;

    $.ajax({
        url: WISHLIST_API_BASE + "/wish/" + encodeURIComponent(wishlistId),
        type: "DELETE",
        success: function () {
            wishlistItems = wishlistItems.filter(function (item) {
                return String(item.sid) !== String(wishlistId) && String(item.hotelId) !== String(hotelId);
            });
            drawWishlist();
        },
        error: function () {
            alert("위시리스트 삭제에 실패했습니다.");
        }
    });
}

function unwrapWishlist(result) {
    if (!result) return [];
    if (Array.isArray(result)) return result;
    if (Array.isArray(result.data)) return result.data;
    return [];
}

function readWishlistJson(key) {
    try {
        const value = localStorage.getItem(key) || sessionStorage.getItem(key);
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function resolveWishlistImage(path) {
    if (!path) return WISHLIST_FALLBACK_IMAGE;
    if (/^https?:\/\//i.test(path)) return path;
    return window.StayNowConfig.assetUrl(path);
}

function drawWishlistStars(rating) {
    const count = Math.max(0, Math.min(5, Number(rating || 0)));
    return "★".repeat(count) + "☆".repeat(5 - count);
}

function shortWishlistText(value, max) {
    const text = String(value || "");
    return text.length > max ? text.slice(0, max) + "..." : text;
}

function formatWishlistWon(value) {
    return "₩" + formatWishlistNumber(value);
}

function formatWishlistNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function escapeWishlist(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
