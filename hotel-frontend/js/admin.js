const ADMIN_PAGES = [
    { id: "dashboard", label: "대시보드", file: "admin-dashboard.html", icon: "fa-table-cells-large", group: "메인" },
    { id: "reservations", label: "예약 관리", file: "admin-reservations.html", icon: "fa-calendar-check", group: "메인" },
    { id: "checkin", label: "체크인 현황", file: "admin-checkin.html", icon: "fa-clock", group: "메인" },
    { id: "guests", label: "고객 관리", file: "admin-guests.html", icon: "fa-users", group: "메인" },
    { id: "rooms", label: "객실 관리", file: "admin-rooms.html", icon: "fa-bed", group: "객실 · 요금" },
    { id: "promotions", label: "프로모션", file: "admin-promotions.html", icon: "fa-percent", group: "객실 · 요금" },
    { id: "sales", label: "매출 분석", file: "admin-sales.html", icon: "fa-chart-bar", group: "분석 · 리포트" },
    { id: "reviews", label: "리뷰 관리", file: "admin-reviews.html", icon: "fa-star", group: "분석 · 리포트" },
    { id: "settings", label: "호텔 관리", file: "admin-hotel.html", icon: "fa-hotel", group: "설정" }
];

const ADMIN_TOP_NAV = ["dashboard", "reservations", "guests", "rooms", "sales", "reviews", "settings"];
const ADMIN_SELECTED_HOTEL_KEY = "staynowAdminSelectedHotelId";
const ADMIN_NOTIFICATION_READ_KEY = "staynowAdminNotificationReadIds";
const ADMIN_NOTIFICATION_DELETED_KEY = "staynowAdminNotificationDeletedIds";
const ADMIN_WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];
let ADMIN_DASHBOARD_STATE = null;
let ADMIN_RESERVATION_STATE = null;
let ADMIN_GUEST_STATE = null;
let ADMIN_SALES_STATE = null;
let ADMIN_PROMOTION_STATE = null;
let ADMIN_ROOM_STATE = null;
let ADMIN_REVIEW_STATE = null;
let ADMIN_HOTEL_MANAGE_STATE = null;
let ADMIN_ROOM_VIEW = "card";
let ADMIN_ROOM_PHOTO_STATE = { existing: [], added: [], deleted: [] };
let ADMIN_HOTEL_PHOTO_STATE = { existing: [], added: [], deleted: [] };
let ADMIN_HOTEL_CREATE_PHOTO_STATE = { added: [] };
let ADMIN_CURRENT_PAGE = "dashboard";
let ADMIN_ROOM_TYPES = [];
let ADMIN_QR_STREAM = null;
let ADMIN_QR_TIMER = null;
let ADMIN_QR_DETECTOR = null;
let ADMIN_QR_PROCESSING = false;
let ADMIN_NOTICE_TIMER = null;

window.alert = function (message) {
    showAdminNotice(message, "info");
};

const reservations = [
    ["SN-0714-8842", "김민준", "디럭스 더블 101호", "07.14", "07.16", "₩240,000", "예약확정", "green"],
    ["SN-0714-8841", "박서연", "프리미엄 스위트 502호", "07.14", "07.17", "₩1,800,000", "체크인중", "orange"],
    ["SN-0714-8840", "이지현", "스탠다드 트윈 215호", "07.15", "07.16", "₩185,000", "예약확정", "green"],
    ["SN-0714-8839", "최준혁", "디럭스 킹 308호", "07.13", "07.14", "₩320,000", "체크아웃", "blue"],
    ["SN-0714-8838", "정하은", "비즈니스 더블 402호", "07.14", "07.15", "₩165,000", "취소요청", "red"]
];

$(function () {
    const pageId = $("body").data("admin-page") || "dashboard";
    const auth = getAdminAuth();

    if (!auth || !auth.token) {
        sessionStorage.setItem("afterLoginRedirect", location.pathname.split("/").pop());
        alert("관리자 화면은 로그인이 필요합니다.");
        location.href = "login.html";
        return;
    }

    if (auth.role !== "ADMIN") {
        alert("관리자만 접근할 수 있는 화면입니다.");
        location.href = "index.html";
        return;
    }

    renderAdminShell(pageId, auth);
});

function renderAdminShell(pageId, auth) {
    const page = ADMIN_PAGES.find(item => item.id === pageId) || ADMIN_PAGES[0];
    ADMIN_CURRENT_PAGE = page.id;
    $("#adminApp").html(`
        <header class="admin-topbar">
            <a class="admin-brand" href="admin-dashboard.html">
                <img class="admin-brand-logo" src="images/logo/Logo_Normal.png" alt="StayNow">
            </a>
            <nav class="admin-top-nav">${ADMIN_TOP_NAV.map(id => navTop(id, pageId)).join("")}</nav>
            <div class="admin-tools">
                <div class="quick-search-wrap">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input id="adminQuickSearch" class="quick-search" type="search" placeholder="빠른 검색...">
                    <div id="adminQuickResults" class="quick-results" hidden></div>
                </div>
                <button id="adminBell" class="bell" type="button" aria-label="알림">
                    <i class="fa-regular fa-bell"></i>
                    <span id="adminBellDot" class="bell-dot" hidden></span>
                </button>
                <div id="adminNotificationMenu" class="notification-menu" hidden>
                    <div class="dropdown-head">
                        <strong>알림</strong>
                        <button type="button" id="adminReadAllNotifications">모두 확인</button>
                    </div>
                    <div id="adminNotificationList" class="notification-list">
                        <div class="dropdown-empty">알림을 불러오는 중입니다.</div>
                    </div>
                </div>
                <button id="adminProfileButton" class="admin-profile" type="button">
                    <span class="avatar"><i class="fa-solid fa-user"></i></span>
                    <span><strong>${escapeHtml(auth.name || "김관리자")}</strong><span>Super Admin</span></span>
                    <i class="fa-solid fa-chevron-down"></i>
                </button>
                <div id="adminProfileMenu" class="profile-menu" hidden>
                    <button type="button" data-admin-profile-action="site"><i class="fa-solid fa-house"></i> 사용자 화면</button>
                    <button type="button" data-admin-profile-action="logout"><i class="fa-solid fa-right-from-bracket"></i> 로그아웃</button>
                </div>
            </div>
        </header>
        <aside class="admin-sidebar">
            <div class="hotel-switcher" id="adminHotelSwitcher">
                <span class="mini-icon"><i class="fa-solid fa-hotel"></i></span>
                <button class="admin-hotel-toggle" id="adminHotelToggle" type="button" aria-disabled="true">
                    <span id="adminHotelName">호텔 불러오는 중</span>
                    <i class="fa-solid fa-chevron-down"></i>
                </button>
                <div class="admin-hotel-menu" id="adminHotelMenu" hidden>
                    <label class="admin-hotel-search">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input id="adminHotelSearch" type="search" placeholder="호텔명, 지역 검색">
                    </label>
                    <div id="adminHotelOptions"></div>
                </div>
            </div>
            ${renderSideGroups(pageId)}
        </aside>
        <main class="admin-main">
            <section class="page-head">
                <div><h1>${page.label}${page.id === "dashboard" ? '<span class="trend">● 실시간 업데이트 중</span>' : ""}</h1><p id="adminPageSubtitle">${pageSubtitle(page.id)}</p></div>
                <div class="head-actions">${headActions(page.id)}</div>
            </section>
            <div id="adminNoticeHost" class="admin-notice-host" aria-live="polite"></div>
            <section id="adminContent">${renderPage(page.id)}</section>
            <div class="admin-note">${adminPageNote(page.id)}</div>
        </main>
    `);

    initAdminTopbar(auth);
    if (page.id === "promotions") {
        renderPromotionHotelScope();
    }

    if (page.id === "dashboard") {
        loadAdminDashboardData();
    } else if (page.id === "reservations") {
        loadAdminReservationData();
    } else if (page.id === "guests") {
        loadAdminGuestData();
    } else if (page.id === "rooms") {
        loadAdminRoomData();
    } else if (page.id === "checkin") {
        loadAdminCheckinData();
    } else if (page.id === "sales") {
        loadAdminSalesData();
    } else if (page.id === "promotions") {
        loadAdminPromotionData();
    } else if (page.id === "reviews") {
        loadAdminReviewData();
    } else if (page.id === "settings") {
        loadAdminHotelManageData();
    }

    clearInterval(window.adminClockTimer);
    window.adminClockTimer = setInterval(function () {
        $("#adminPageSubtitle").text(pageSubtitle(page.id));
    }, 60000);

    $(document).off("click.adminRefresh").on("click.adminRefresh", "[data-admin-action='refresh-dashboard']", function () {
        loadAdminDashboardData();
    });
    $(document).off("click.adminReport").on("click.adminReport", "[data-admin-action='download-dashboard-report']", function () {
        downloadDashboardReport();
    });
    $(document).off("click.adminReservationReport").on("click.adminReservationReport", "[data-admin-action='download-reservation-report']", function () {
        downloadReservationReport();
    });
    $(document).off("click.adminSalesReport").on("click.adminSalesReport", "[data-admin-action='download-sales-report']", function () {
        downloadSalesReport();
    });
    $(document).off("click.adminRoomsReport").on("click.adminRoomsReport", "[data-admin-action='download-rooms-report']", function () {
        downloadRoomReport();
    });
    $(document).off("click.adminRoomsAdd").on("click.adminRoomsAdd", "[data-admin-action='open-room-create']", function () {
        openRoomModal();
    });
    $(document).off("click.adminCheckinCalendar").on("click.adminCheckinCalendar", "[data-admin-action='open-checkin-calendar']", function () {
        $("#checkinCalendarPanel")[0]?.scrollIntoView({ behavior: "smooth", block: "center" });
        setTimeout(highlightCheckinCalendarPanel, 360);
    });
    $(document).off("click.adminCheckinReport").on("click.adminCheckinReport", "[data-admin-action='download-checkin-report']", function () {
        downloadCheckinReport();
    });
    $(document).off("click.adminQrCheckin").on("click.adminQrCheckin", "[data-admin-action='open-qr-checkin']", function () {
        openAdminQrCheckinModal();
    });
    $(document).off("click.adminPromotionCreate").on("click.adminPromotionCreate", "[data-admin-action='create-promotion']", function () {
        openPromotionModal();
    });
    $(document).off("click.adminCheckin").on("click.adminCheckin", "[data-admin-checkin]", function () {
        checkInAdminReservation($(this).data("adminCheckin"));
    });
    $(document).off("click.adminCheckout").on("click.adminCheckout", "[data-admin-checkout]", function () {
        checkOutAdminReservation($(this).data("adminCheckout"));
    });
    $(document).off("click.adminCancelReservation").on("click.adminCancelReservation", "[data-admin-cancel-reservation]", function () {
        cancelAdminReservation($(this).data("adminCancelReservation"));
    });
    $(document).off("click.adminHotelCreate").on("click.adminHotelCreate", "[data-admin-action='open-hotel-create']", function () {
        openHotelManageModal();
    });
    $(document).off("click.adminHotelImport").on("click.adminHotelImport", "[data-admin-action='open-hotel-import']", function () {
        openHotelImportModal();
    });
    $(document).off("click.adminHotelTypeManage").on("click.adminHotelTypeManage", "[data-admin-action='open-hotel-type-manage']", function () {
        openHotelTypeManager();
    });
    $(document).off("click.adminHotelAmenityManage").on("click.adminHotelAmenityManage", "[data-admin-action='open-hotel-amenity-manage']", function () {
        openHotelAmenityManager();
    });
    $(document).off("click.adminReviewTagManage").on("click.adminReviewTagManage", "[data-admin-action='open-review-tag-manage']", function () {
        openReviewTagManager();
    });
    $(document).off("click.adminReviewCategoryManage").on("click.adminReviewCategoryManage", "[data-admin-action='open-review-category-manage']", function () {
        openReviewCategoryManager();
    });
    $(document).off("click.adminGuestCoupon").on("click.adminGuestCoupon", "[data-admin-action='open-guest-coupon']", function () {
        openGuestCouponModal();
    });
    $(document).off("click.adminGuestCouponIssue").on("click.adminGuestCouponIssue", "[data-admin-action='open-guest-coupon-issue']", function () {
        openGuestCouponIssueModal();
    });
}

function getAdminNowLabel() {
    const now = new Date();
    const hour = now.getHours();
    const displayHour = hour % 12 || 12;
    const meridiem = hour < 12 ? "오전" : "오후";
    return `${now.getFullYear()}년 ${now.getMonth() + 1}월 ${now.getDate()}일 (${ADMIN_WEEKDAYS[now.getDay()]}) · ${meridiem} ${String(displayHour).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
}

function getAdminDateLabel() {
    const now = new Date();
    return `${now.getFullYear()}년 ${now.getMonth() + 1}월 ${now.getDate()}일`;
}

function getAdminMonthLabel() {
    return `이번 달 (${new Date().getMonth() + 1}월)`;
}

function getAdminMonthSettlementLabel() {
    const now = new Date();
    return `${now.getFullYear()}년 ${now.getMonth() + 1}월`;
}

function getCurrentMonthValue() {
    const now = new Date();
    return now.getFullYear() + "-" + String(now.getMonth() + 1).padStart(2, "0");
}

function parseMonthValue(value) {
    const text = String(value || getCurrentMonthValue());
    const parts = text.split("-");
    const year = Number(parts[0]) || new Date().getFullYear();
    const month = Math.max(0, Math.min(11, (Number(parts[1]) || (new Date().getMonth() + 1)) - 1));
    return { year, month };
}

function getAdminMonthRangeLabel() {
    const now = new Date();
    const month = now.getMonth() + 1;
    return `이번 달 (${String(month).padStart(2, "0")}.01 ~ ${String(month).padStart(2, "0")}.${String(now.getDate()).padStart(2, "0")})`;
}

function addOneDay(dateValue) {
    const date = new Date(dateValue + "T00:00:00");
    date.setDate(date.getDate() + 1);
    return [
        date.getFullYear(),
        String(date.getMonth() + 1).padStart(2, "0"),
        String(date.getDate()).padStart(2, "0")
    ].join("-");
}

function debounce(fn, delay) {
    let timer = null;
    return function () {
        const args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(null, args);
        }, delay);
    };
}

function clampAdminPercent(value) {
    const numeric = Number(value);
    if (!Number.isFinite(numeric) || numeric <= 0) {
        return 0;
    }
    return Math.min(100, Math.max(0, Math.round(numeric * 10) / 10));
}

function renderMetricProgress(progressPercent) {
    if (progressPercent == null) {
        return "";
    }
    return `<div class="progress"><i style="width:${clampAdminPercent(progressPercent)}%"></i></div>`;
}

function navTop(id, activeId) {
    const page = ADMIN_PAGES.find(item => item.id === id);
    return `<a class="top-nav-link ${id === activeId ? "active" : ""}" href="${page.file}"><i class="fa-solid ${page.icon}"></i>${page.label}</a>`;
}

function renderSideGroups(activeId) {
    const groups = [...new Set(ADMIN_PAGES.map(page => page.group))];
    return groups.map(group => `
        <div class="side-group">
            <p class="side-title">${group}</p>
            ${ADMIN_PAGES.filter(page => page.group === group).map(page => `
                <a class="side-link ${page.id === activeId ? "active" : ""}" href="${page.file}">
                    <span class="mini-icon"><i class="fa-solid ${page.icon}"></i></span>
                    ${page.label}
                    ${page.badge ? `<span class="badge">${page.badge}</span>` : ""}
                </a>
            `).join("")}
        </div>
    `).join("");
}

function initAdminTopbar(auth) {
    bindAdminQuickSearch();
    bindAdminNotificationMenu();
    bindAdminProfileMenu(auth);
    loadAdminNotifications();
}

function bindAdminQuickSearch() {
    const input = $("#adminQuickSearch");
    const results = $("#adminQuickResults");

    $(".quick-search-wrap").off("click.adminQuick").on("click.adminQuick", function (event) {
        event.stopPropagation();
    });

    input.off("input.adminQuick focus.adminQuick keydown.adminQuick")
        .on("input.adminQuick focus.adminQuick", function () {
            renderAdminQuickResults($(this).val());
        })
        .on("keydown.adminQuick", function (event) {
            if (event.key !== "Enter") return;
            const first = results.find("[data-admin-quick-link]").first();
            if (first.length) {
                event.preventDefault();
                location.href = first.data("adminQuickLink");
            }
        });

    results.off("click.adminQuick").on("click.adminQuick", "[data-admin-quick-link]", function () {
        location.href = $(this).data("adminQuickLink");
    });
}

function renderAdminQuickResults(keyword) {
    const results = $("#adminQuickResults");
    const normalized = String(keyword || "").trim().toLowerCase();

    if (!normalized) {
        results.prop("hidden", true).empty();
        return;
    }

    const matches = ADMIN_PAGES.filter(function (page) {
        return [page.label, page.group, page.id]
            .join(" ")
            .toLowerCase()
            .includes(normalized);
    });

    if (!matches.length) {
        results.html('<div class="dropdown-empty">일치하는 메뉴가 없습니다.</div>').prop("hidden", false);
        return;
    }

    results.html(matches.map(function (page) {
        return `<button type="button" data-admin-quick-link="${escapeHtml(page.file)}">
            <span class="mini-icon"><i class="fa-solid ${page.icon}"></i></span>
            <span><strong>${escapeHtml(page.label)}</strong><small>${escapeHtml(page.group)}</small></span>
        </button>`;
    }).join("")).prop("hidden", false);
}

function bindAdminNotificationMenu() {
    $("#adminBell").off("click.adminNotifications").on("click.adminNotifications", function (event) {
        event.stopPropagation();
        closeAdminProfileMenu();
        closeAdminQuickResults();
        $("#adminNotificationMenu").prop("hidden", !$("#adminNotificationMenu").prop("hidden"));
    });

    $("#adminNotificationMenu").off("click.adminNotifications").on("click.adminNotifications", function (event) {
        event.stopPropagation();
    });

    $("#adminReadAllNotifications").off("click.adminNotifications").on("click.adminNotifications", function () {
        const ids = $("#adminNotificationList [data-admin-notification-id]").map(function () {
            return String($(this).data("adminNotificationId"));
        }).get();
        saveAdminReadNotifications(ids);
        renderAdminNotificationState();
    });

    $("#adminNotificationList").off("click.adminNotifications").on("click.adminNotifications", "[data-admin-notification-id]", function () {
        saveAdminReadNotifications([String($(this).data("adminNotificationId"))]);
    });

    $("#adminNotificationList").off("click.adminDeleteNotification").on("click.adminDeleteNotification", "[data-admin-delete-notification]", function (event) {
        event.preventDefault();
        event.stopPropagation();
        saveAdminDeletedNotifications([String($(this).data("adminDeleteNotification"))]);
        $(this).closest(".notification-item").remove();
        if (!$("#adminNotificationList [data-admin-notification-id]").length) {
            $("#adminNotificationList").html('<div class="dropdown-empty">새 알림이 없습니다.</div>');
        }
        renderAdminNotificationState();
    });
}

function bindAdminProfileMenu() {
    $("#adminProfileButton").off("click.adminProfile").on("click.adminProfile", function (event) {
        event.stopPropagation();
        closeAdminNotificationMenu();
        closeAdminQuickResults();
        $("#adminProfileMenu").prop("hidden", !$("#adminProfileMenu").prop("hidden"));
    });

    $("#adminProfileMenu").off("click.adminProfile").on("click.adminProfile", function (event) {
        event.stopPropagation();
    }).on("click.adminProfile", "[data-admin-profile-action]", function () {
        const action = $(this).data("adminProfileAction");
        if (action === "site") {
            location.href = "index.html";
            return;
        }
        if (action === "logout") {
            const logoutRequest = window.StayNowConfig && window.StayNowConfig.logout
                ? window.StayNowConfig.logout()
                : $.Deferred().resolve().promise();
            logoutRequest.always(function () {
                location.href = "login.html";
            });
        }
    });

    $(document).off("click.adminTopbarMenus").on("click.adminTopbarMenus", function () {
        closeAdminTopbarMenus();
    });
}

function loadAdminNotifications() {
    adminGetSafe("/reservation/search?page=0&size=50&sort=createdAt,desc", { content: [] })
        .then(function (page) {
            const reservations = asPageContent(page);
            const items = buildAdminNotifications(reservations);
            $("#adminNotificationList").html(renderAdminNotifications(items));
            renderAdminNotificationState();
        }, function () {
            $("#adminNotificationList").html('<div class="dropdown-empty">알림을 불러오지 못했습니다.</div>');
            $("#adminBellDot").prop("hidden", true);
        });
}

function buildAdminNotifications(reservations) {
    const items = [];
    reservations.slice(0, 8).forEach(function (reservation) {
        if (isToday(reservation.createdAt)) {
            items.push(makeAdminNotification(
                "new-" + reservation.sid,
                "새 예약이 들어왔습니다",
                (reservation.guestName || reservation.memberName || "고객") + " · " + makeAdminRoomName(reservation),
                "admin-reservations.html"
            ));
        }
        if (isToday(reservation.checkInDate) && ["CONFIRMED", "UPCOMING", "PENDING"].includes(reservation.reservationStatus)) {
            items.push(makeAdminNotification(
                "checkin-" + reservation.sid,
                "오늘 체크인 예정",
                formatAdminShortDate(reservation.checkInDate) + " · " + (reservation.guestName || reservation.memberName || "고객"),
                "admin-reservations.html"
            ));
        }
        if (reservation.reservationStatus === "CANCELLED" || reservation.reservationStatus === "NO_SHOW") {
            items.push(makeAdminNotification(
                "cancel-" + reservation.sid,
                "취소/노쇼 예약 확인",
                (reservation.reservationNumber || "RSV-" + reservation.sid) + " · " + formatAdminStatus(reservation.reservationStatus),
                "admin-reservations.html"
            ));
        }
    });

    return items.slice(0, 10);
}

function makeAdminNotification(id, title, message, link) {
    return {
        id,
        title,
        message,
        link
    };
}

function renderAdminNotifications(items) {
    const deletedIds = getAdminDeletedNotifications();
    const visibleItems = items.filter(function (item) {
        return !deletedIds.has(item.id);
    });

    if (!visibleItems.length) {
        return '<div class="dropdown-empty">새 알림이 없습니다.</div>';
    }

    const readIds = getAdminReadNotifications();
    return visibleItems.map(function (item) {
        const unread = !readIds.has(item.id);
        return `<div class="notification-item ${unread ? "unread" : ""}" data-admin-notification-id="${escapeHtml(item.id)}">
            <a href="${escapeHtml(item.link)}">
                <span class="notification-dot"></span>
                <span><strong>${escapeHtml(item.title)}</strong><small>${escapeHtml(item.message)}</small></span>
            </a>
            <button class="notification-delete" type="button" data-admin-delete-notification="${escapeHtml(item.id)}">삭제</button>
        </div>`;
    }).join("");
}

function renderAdminNotificationState() {
    const readIds = getAdminReadNotifications();
    const hasUnread = $("#adminNotificationList [data-admin-notification-id]").filter(function () {
        return !readIds.has(String($(this).data("adminNotificationId")));
    }).length > 0;

    $("#adminBellDot").prop("hidden", !hasUnread);
    $("#adminNotificationList [data-admin-notification-id]").each(function () {
        $(this).toggleClass("unread", !readIds.has(String($(this).data("adminNotificationId"))));
    });
}

function getAdminReadNotifications() {
    try {
        return new Set(JSON.parse(localStorage.getItem(ADMIN_NOTIFICATION_READ_KEY) || "[]").map(String));
    } catch (e) {
        return new Set();
    }
}

function saveAdminReadNotifications(ids) {
    const readIds = getAdminReadNotifications();
    ids.forEach(function (id) {
        readIds.add(String(id));
    });
    localStorage.setItem(ADMIN_NOTIFICATION_READ_KEY, JSON.stringify(Array.from(readIds).slice(-100)));
}

function getAdminDeletedNotifications() {
    try {
        return new Set(JSON.parse(localStorage.getItem(ADMIN_NOTIFICATION_DELETED_KEY) || "[]").map(String));
    } catch (e) {
        return new Set();
    }
}

function saveAdminDeletedNotifications(ids) {
    const deletedIds = getAdminDeletedNotifications();
    ids.forEach(function (id) {
        deletedIds.add(String(id));
    });
    localStorage.setItem(ADMIN_NOTIFICATION_DELETED_KEY, JSON.stringify(Array.from(deletedIds).slice(-100)));
}

function closeAdminTopbarMenus() {
    closeAdminQuickResults();
    closeAdminNotificationMenu();
    closeAdminProfileMenu();
}

function closeAdminQuickResults() {
    $("#adminQuickResults").prop("hidden", true);
}

function closeAdminNotificationMenu() {
    $("#adminNotificationMenu").prop("hidden", true);
}

function closeAdminProfileMenu() {
    $("#adminProfileMenu").prop("hidden", true);
}

function pageSubtitle(id) {
    const nowLabel = getAdminNowLabel();
    const copy = {
        dashboard: `${nowLabel}`,
        reservations: `${nowLabel} · 전체 예약 현황 및 승인 관리`,
        checkin: `${nowLabel} · 체크인 / 체크아웃 운영`,
        guests: "회원 정보, 예약 이력, 포인트 현황 관리",
        rooms: "실시간 객실 상태, 예약, 공실 관리",
        promotions: `${nowLabel} · 특가 및 할인 프로모션 관리`,
        sales: `${getAdminDateLabel()} 기준`,
        reviews: "호텔별 리뷰 확인 및 답변 관리",
        settings: "호텔 정보, 편의시설, 타입, 사진 관리"
    };
    return copy[id] || nowLabel;
}

function adminPageNote(id) {
    if (id === "dashboard") {
        return "대시보드에서 여러 정보들을 한 눈에 쉽고 빠르게 파악할 수 있습니다.";
    }
    if (id === "sales") {
        return "월별 매출 상황을 볼 수 있습니다.";
    }
    if (id === "reservations") {
        return "현재 선택된 호텔의 예약에 대해 관리할 수 있습니다.";
    }
    if (id === "guests") {
        return "모든 회원에 대한 관리를 합니다. 선택된 호텔이 있다면 그 호텔의 예약 건수를 볼 수 있습니다.";
    }
    if (id === "promotions") {
        return "프로모션은 전체 호텔의 동일 객실 타입에 공통 적용됩니다.";
    }
    if (id === "reviews") {
        return "현재 선택된 호텔의 리뷰에 대해 관리할 수 있습니다.";
    }
    if (id === "settings") {
        return "현재 선택된 호텔에 대해 정보를 수정할 수 있습니다.";
    }
    return "현재 화면은 관리자 UI 입니다.";
}

function showAdminNotice(message, tone) {
    const text = String(message || "").trim();
    if (!text) {
        return;
    }

    const noticeTone = tone || (/(실패|오류|불가|확인|필수|없습니다)/.test(text) ? "danger" : "info");
    let host = $("#adminNoticeHost");
    if (!host.length) {
        host = $('<div id="adminNoticeHost" class="admin-notice-host floating" aria-live="polite"></div>');
        $("body").append(host);
    }

    clearTimeout(ADMIN_NOTICE_TIMER);
    host.html(`
        <div class="admin-notice ${noticeTone}">
            <i class="fa-solid ${noticeTone === "success" ? "fa-circle-check" : noticeTone === "danger" ? "fa-triangle-exclamation" : "fa-circle-info"}"></i>
            <span>${escapeHtml(text)}</span>
            <button type="button" class="admin-notice-close" aria-label="안내 닫기"><i class="fa-solid fa-xmark"></i></button>
        </div>
    `).prop("hidden", false);
    host.find(".admin-notice-close").on("click", function () {
        host.empty().prop("hidden", true);
    });
    ADMIN_NOTICE_TIMER = setTimeout(function () {
        host.empty().prop("hidden", true);
    }, 5200);
}

function headActions(id) {
    const byPage = {
        dashboard: [["리포트 다운로드", "fa-download", "", "download-dashboard-report"], ["새로고침", "fa-rotate-right", "", "refresh-dashboard"]],
        reservations: [["리포트 다운로드", "fa-download", "", "download-reservation-report"]],
        guests: [["쿠폰 생성", "fa-ticket", "", "open-guest-coupon"], ["쿠폰 발급", "fa-paper-plane", "primary", "open-guest-coupon-issue"]],
        rooms: [["리포트 다운로드", "fa-download", "", "download-rooms-report"], ["객실 추가", "fa-plus", "primary", "open-room-create"]],
        promotions: [["프로모션 생성", "fa-plus", "primary", "create-promotion"]],
        sales: [["리포트 다운로드", "fa-download", "", "download-sales-report"]],
        checkin: [["QR 체크인", "fa-qrcode", "primary", "open-qr-checkin"], ["캘린더 보기", "fa-calendar", "", "open-checkin-calendar"], ["리포트 다운로드", "fa-download", "", "download-checkin-report"]],
        reviews: [["리뷰 태그 관리", "fa-tags", "", "open-review-tag-manage"], ["리뷰 카테고리 관리", "fa-list-check", "", "open-review-category-manage"]],
        settings: [["호텔 타입 관리", "fa-layer-group", "", "open-hotel-type-manage"], ["편의시설 관리", "fa-wand-magic-sparkles", "", "open-hotel-amenity-manage"], ["호텔 불러오기", "fa-cloud-arrow-down", "", "open-hotel-import"], ["호텔 추가", "fa-plus", "primary", "open-hotel-create"]]
    };
    const buttons = byPage[id] || [[getAdminMonthLabel(), "fa-calendar"], ["리포트 다운로드", "fa-download"]];
    const actionButtons = buttons.map(([text, icon, type, action]) => `<button class="admin-btn ${type || ""}" type="button"${action ? ` data-admin-action="${action}"` : ""}><i class="fa-solid ${icon}"></i> ${text}</button>`).join("");
    if (id === "sales") {
        return `<label class="admin-month-picker"><span>기준 월</span><input id="salesMonthPicker" type="month" value="${getCurrentMonthValue()}"></label>${actionButtons}`;
    }
    return actionButtons;
}

function renderPage(id) {
    const pages = {
        dashboard: renderDashboard,
        reservations: renderReservations,
        checkin: renderCheckin,
        guests: renderGuests,
        rooms: renderRooms,
        promotions: renderPromotions,
        sales: renderSales,
        reviews: renderReviews,
        settings: renderSettings
    };
    return (pages[id] || renderDashboard)();
}

function renderDashboard() {
    return `
        ${metrics([["오늘 예약", "47건", "fa-calendar-check", "+12.4%"], ["체크인 예정", "18팀", "fa-right-to-bracket", "오늘", "warn"], ["객실 점유율", "84.2%", "fa-bed", "+3.2%"], ["이번 달 매출", "₩284M", "fa-wallet", "+18.7%"], ["평균 평점", "9.4점", "fa-star", "+0.2"]], 5)}
        <div class="grid two-col" style="margin-top:22px">
            ${salesPanel("월별 매출 현황", "2025년 1월 ~ 7월")}
            ${roomRingPanel()}
        </div>
        <div class="grid two-col" style="margin-top:22px">
            <div class="panel">${panelHead("최근 예약 현황", "오늘 기준 최신 12건")} ${reservationTable()}</div>
            <div class="grid">${todaySchedule()}${taskPanel()}</div>
        </div>
        <div class="panel" style="margin-top:22px">${panelHead("객실 현황 맵", "실시간 객실 상태")}${roomMap()}</div>
    `;
}

function renderReservations() {
    return `
        <div id="reservationMetrics" class="grid stats-grid cols-4">
            ${dashboardMetricSkeleton("오늘 신규 예약", "fa-calendar-check")}
            ${dashboardMetricSkeleton("체크인 예정", "fa-right-to-bracket")}
            ${dashboardMetricSkeleton("취소된 건", "fa-circle-xmark")}
            ${dashboardMetricSkeleton("이번 달 예약", "fa-calendar-days")}
        </div>
        <div class="panel reservation-admin-panel">
            <div class="reservation-filter-grid">
                <label class="admin-search-field">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input id="reservationKeyword" type="search" placeholder="예약번호, 고객명 검색...">
                </label>
                <select id="reservationStatusFilter" class="admin-select">
                    <option value="">전체 상태</option>
                    <option value="CONFIRMED">예약확정</option>
                    <option value="CHECKED_IN">체크인</option>
                    <option value="CHECKED_OUT">체크아웃</option>
                    <option value="PENDING">예약대기</option>
                    <option value="CANCELLED">취소</option>
                    <option value="NO_SHOW">노쇼</option>
                </select>
                <label class="admin-date-field">
                    <span>시작 날짜</span>
                    <input id="reservationDateFrom" type="date">
                </label>
                <label class="admin-date-field">
                    <span>종료 날짜</span>
                    <input id="reservationDateTo" type="date">
                </label>
                <select id="reservationRoomTypeFilter" class="admin-select">
                    <option value="">전체 객실</option>
                </select>
                <button id="reservationFilterReset" class="admin-btn" type="button"><i class="fa-solid fa-rotate-right"></i></button>
            </div>
            <div id="reservationActiveFilters" class="active-filter-row"></div>
            <div id="reservationTableWrap" class="admin-table-wrap">
                <div class="admin-loading">예약 데이터를 불러오는 중입니다.</div>
            </div>
        </div>
    `;
}

function renderCheckin() {
    return `
        <div id="checkinMetrics" class="grid stats-grid cols-4">
            ${dashboardMetricSkeleton("오늘 전체 예약", "fa-calendar-check")}
            ${dashboardMetricSkeleton("오늘 체크인", "fa-right-to-bracket")}
            ${dashboardMetricSkeleton("오늘 체크아웃", "fa-right-from-bracket")}
            ${dashboardMetricSkeleton("현재 사용중", "fa-bed")}
        </div>
        <div class="grid split checkin-layout" style="margin-top:22px">
            <div class="panel checkin-admin-panel">
                <div class="checkin-toolbar">
                    <label class="admin-search-field">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input id="checkinKeyword" type="search" placeholder="예약번호, 고객명, 객실 검색">
                    </label>
                    <select id="checkinStatusFilter" class="admin-select">
                        <option value="">전체 상태</option>
                        <option value="CONFIRMED">예약확정</option>
                        <option value="UPCOMING">체크인 예정</option>
                        <option value="CHECKED_IN">체크인</option>
                        <option value="CHECKED_OUT">체크아웃</option>
                        <option value="CANCELLED">취소</option>
                    </select>
                    <div class="checkin-bulk-actions">
                        <button class="admin-btn" type="button" data-checkin-bulk="check-in">일괄 체크인</button>
                        <button class="admin-btn" type="button" data-checkin-bulk="check-out">일괄 체크아웃</button>
                        <button class="admin-btn danger" type="button" data-checkin-bulk="cancel">일괄 취소</button>
                    </div>
                </div>
                <div id="checkinTableWrap" class="admin-table-wrap">
                    <div class="admin-loading">체크인 데이터를 불러오는 중입니다.</div>
                </div>
            </div>
            <div class="grid">
                <div id="todayCheckinPanel" class="panel">${emptyAdminState("오늘 체크인 현황을 불러오는 중입니다.")}</div>
                <div id="checkinCalendarPanel" class="panel">${emptyAdminState("캘린더를 불러오는 중입니다.")}</div>
            </div>
        </div>
        <div id="checkinModalRoot"></div>
    `;
}

function renderGuests() {
    return `
        <div id="guestMetrics" class="grid stats-grid cols-4">
            ${dashboardMetricSkeleton("총 고객", "fa-users")}
            ${dashboardMetricSkeleton("활성 고객", "fa-user-check")}
            ${dashboardMetricSkeleton("예약 고객", "fa-calendar-check")}
            ${dashboardMetricSkeleton("포인트 합계", "fa-coins")}
        </div>
        <div class="panel guest-admin-panel">
            <div class="guest-filter-grid">
                <label class="admin-search-field">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input id="guestKeyword" type="search" placeholder="고객명, 이메일, 전화번호 검색...">
                </label>
                <select id="guestStatusFilter" class="admin-select">
                    <option value="">전체 상태</option>
                    <option value="ACTIVE">활성</option>
                    <option value="INACTIVE">비활성</option>
                    <option value="STOP">정지</option>
                    <option value="DELETED">탈퇴</option>
                </select>
                <select id="guestPointFilter" class="admin-select">
                    <option value="">전체 포인트</option>
                    <option value="HAS_POINT">포인트 보유</option>
                    <option value="NO_POINT">포인트 없음</option>
                </select>
                <button id="guestFilterReset" class="admin-btn" type="button"><i class="fa-solid fa-rotate-right"></i></button>
            </div>
            <div id="guestTableWrap" class="admin-table-wrap">
                <div class="admin-loading">고객 데이터를 불러오는 중입니다.</div>
            </div>
        </div>
        <div id="guestReservationPanel" class="panel guest-reservation-panel" hidden>
            <div class="admin-loading">예약 내역을 준비하는 중입니다.</div>
        </div>
        <div id="guestCouponModalRoot"></div>
    `;
}

function renderRooms() {
    return `
        <div id="roomMetrics" class="grid stats-grid cols-5 room-stats-grid">
            ${dashboardMetricSkeleton("전체 객실", "fa-bed")}
            ${dashboardMetricSkeleton("사용중", "fa-users")}
            ${dashboardMetricSkeleton("예약완료", "fa-calendar-check")}
            ${dashboardMetricSkeleton("공실", "fa-door-open")}
            ${dashboardMetricSkeleton("예약 불가", "fa-ban")}
        </div>
        <div class="room-control-panel">
            <div class="room-filter-grid">
                <label class="admin-search-field">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <input id="roomKeywordFilter" type="search" placeholder="객실번호, 타입 검색...">
                </label>
                <select id="roomFloorFilter" class="admin-select">
                    <option value="">전체 층</option>
                </select>
                <select id="roomTypeFilter" class="admin-select">
                    <option value="">객실 유형 전체</option>
                </select>
                <select id="roomStatusFilter" class="admin-select">
                    <option value="">상태 전체</option>
                    <option value="use">사용중</option>
                    <option value="done">예약완료</option>
                    <option value="available">공실</option>
                    <option value="blocked">예약 불가</option>
                </select>
                <div class="room-view-switch" aria-label="보기 방식">
                    <button class="active" type="button" title="카드 보기" data-room-view="card"><i class="fa-solid fa-table-cells-large"></i></button>
                    <button type="button" title="리스트 보기" data-room-view="list"><i class="fa-solid fa-list"></i></button>
                </div>
                <select id="roomSortFilter" class="admin-select">
                    <option value="numberAsc">객실번호순</option>
                    <option value="numberDesc">객실번호 역순</option>
                    <option value="priceDesc">높은 가격순</option>
                    <option value="priceAsc">낮은 가격순</option>
                </select>
                <button id="roomFilterReset" class="admin-btn" type="button" title="필터 초기화"><i class="fa-solid fa-rotate-right"></i></button>
            </div>
        </div>
        <div id="roomFloorPanel" class="room-floor-panel">
            ${emptyAdminState("객실 현황을 불러오는 중입니다.")}
        </div>
        <div id="roomTypeSummaryPanel" class="panel room-type-summary-panel">${emptyAdminState("객실 유형별 현황을 불러오는 중입니다.")}</div>
        <div id="roomModalRoot"></div>
    `;
}

function renderRates() {
    return `
        ${metrics([["평균 객실 단가", "₩182,400", "fa-tag", "+4.2%"], ["성수기 요금", "₩248,000", "fa-sun", "+22.0%"], ["요금 경쟁력 지수", "87.3점", "fa-arrow-trend-up", "+8.7%"], ["변경 예정", "2025.07.17", "fa-calendar-days", "D-3", "danger"]])}
        <div class="grid split" style="margin-top:22px">
            <div class="panel">${tabs(["스탠다드", "디럭스", "스위트", "프리미엄"])}${simpleTable(["시즌명", "기간", "기준 요금", "주말 요금", "상태", "관리"], [["성수기", "07.17 ~ 08.15", "₩248,000", "₩278,000", "예정", "수정"], ["준성수기", "07.01 ~ 07.16", "₩198,000", "₩228,000", "진행중", "수정"], ["평시", "04.01 ~ 06.30", "₩162,000", "₩182,000", "종료", "수정"], ["비수기", "11.01 ~ 03.31", "₩128,000", "₩148,000", "예정", "수정"]])}</div>
            <div class="panel">${rateSimulator()}</div>
        </div>
        <div class="panel" style="margin-top:22px">${panelHead("요일별 요금 정책", "요일별 차등 적용")}<div class="mini-chart">${["월 ₩162,000", "화 ₩162,000", "수 ₩162,000", "목 ₩162,000", "금 ₩178,200", "토 ₩194,400", "일 ₩194,400"].map((v, i) => `<div class="policy-item"><strong>${v}</strong><span class="status ${i > 3 ? "orange" : "blue"}">${i > 3 ? "할증" : "평일"}</span></div>`).join("")}</div></div>
    `;
}

function renderPromotions() {
    return `
        <div id="promotionMetrics" class="grid stats-grid cols-3">
            ${dashboardMetricSkeleton("진행중 프로모션", "fa-percent")}
            ${dashboardMetricSkeleton("예정 프로모션", "fa-calendar-plus")}
            ${dashboardMetricSkeleton("총 할인 정책", "fa-tags")}
        </div>
        <div class="panel promotion-admin-panel">
            <div id="promotionFilterBar" class="promotion-filter-bar"></div>
            <div id="promotionTableWrap">${emptyAdminState("프로모션 데이터를 불러오는 중입니다.")}</div>
        </div>
        <div id="promotionModalRoot"></div>
    `;
}

function renderSales() {
    return `
        <div id="salesMetrics" class="grid stats-grid cols-4">
            ${dashboardMetricSkeleton("선택 월 총 매출", "fa-money-bill")}
            ${dashboardMetricSkeleton("결제 완료 건수", "fa-receipt")}
            ${dashboardMetricSkeleton("취소 제외 예약", "fa-calendar-check")}
            ${dashboardMetricSkeleton("순매출", "fa-chart-line")}
        </div>
        <div class="panel" id="salesMonthlyPanel" style="margin-top:22px">${emptyAdminState("매출 데이터를 불러오는 중입니다.")}</div>
        <div class="grid split" style="margin-top:22px">
            <div class="panel" id="salesRoomTypePanel">${emptyAdminState("객실 유형별 매출을 불러오는 중입니다.")}</div>
            <div class="panel" id="salesTopReservationPanel">${emptyAdminState("매출 상위 예약을 불러오는 중입니다.")}</div>
        </div>
        <div class="panel" id="salesDailyTablePanel" style="margin-top:22px">${emptyAdminState("일별 매출 표를 불러오는 중입니다.")}</div>
    `;
}

function renderReviews() {
    return `
        <div id="adminReviewMetrics" class="grid stats-grid cols-4">
            ${dashboardMetricSkeleton("평균 평점", "fa-star")}
            ${dashboardMetricSkeleton("전체 리뷰", "fa-message")}
            ${dashboardMetricSkeleton("답변 완료", "fa-circle-check")}
            ${dashboardMetricSkeleton("답변 대기", "fa-reply")}
        </div>
        <div class="panel admin-review-panel">
            <div class="admin-review-toolbar">
                <div class="tabs admin-review-tabs">
                    <button class="chip active" type="button" data-admin-review-filter="all">전체</button>
                    <button class="chip" type="button" data-admin-review-filter="photo">사진포함 리뷰</button>
                    <button class="chip" type="button" data-admin-review-filter="waiting">답변 대기</button>
                </div>
                <label class="admin-review-sort">
                    <span>정렬</span>
                    <select id="adminReviewSort" class="admin-select">
                        <option value="latest">최신순</option>
                        <option value="ratingDesc">평점 높은순</option>
                        <option value="ratingAsc">평점 낮은순</option>
                        <option value="waiting">답변 대기 우선</option>
                    </select>
                </label>
            </div>
            <div id="adminReviewList" class="admin-review-list">
                <div class="admin-loading">리뷰 데이터를 불러오는 중입니다.</div>
            </div>
        </div>
    `;
}

function renderSettlement() {
    return `
        ${metrics([["총 매출액", "₩284,320,000", "fa-wallet", "+18.7%"], ["수수료 합계", "₩14,216,000", "fa-receipt", "+2.1%", "danger"], ["순 정산액", "₩270,104,000", "fa-money-check-dollar", "+19.4%"], ["미정산 잔액", "₩32,480,000", "fa-credit-card", "D+3", "warn"]])}
        <div class="grid split" style="margin-top:22px">
            ${salesPanel("월별 정산 추이", "2025년 1월 ~ 7월 · 매출 / 수수료 / 순정산")}
            <div class="panel">${panelHead("객실 유형별 매출", "7월 기준 비중")}<div class="ring"><strong>₩284M</strong></div>${barLines([["스탠다드", 29, "₩84.2M"], ["디럭스", 34, "₩97.6M"], ["스위트", 25, "₩71.1M"], ["프리미엄", 11, "₩31.4M"]])}</div>
        </div>
        <div class="panel" style="margin-top:22px">${panelHead("정산 상세 내역", "2025년 7월 1일 ~ 14일")} ${simpleTable(["정산일", "예약 건수", "객실 매출", "부대시설 매출", "총 매출", "수수료", "순 정산액", "상태"], [["07.14", "47건", "₩22,480,000", "₩2,140,000", "₩24,620,000", "₩1,231,000", "₩23,389,000", "정산중"], ["07.13", "52건", "₩28,960,000", "₩3,200,000", "₩32,160,000", "₩1,608,000", "₩30,552,000", "완료"], ["07.12", "58건", "₩31,420,000", "₩4,180,000", "₩35,600,000", "₩1,780,000", "₩33,820,000", "완료"]])}</div>
    `;
}

function renderSettings() {
    return `
        <div class="hotel-manage-grid">
            <form id="hotelManageForm" class="panel hotel-manage-form">
                <div class="panel-head"><div><h2>호텔 기본 정보</h2><span>선택한 호텔의 검색/예약 노출 정보를 수정합니다.</span></div></div>
                <div id="hotelManageFields" class="admin-loading">호텔 정보를 불러오는 중입니다.</div>
            </form>
        </div>
        <div id="hotelManageModalRoot"></div>
    `;
}

function metrics(items, count) {
    const cols = count || items.length;
    return `<div class="grid stats-grid cols-${Math.min(cols, 5)}">${items.map(([label, value, icon, trend, tone, progressPercent]) => `
        <article class="metric-card">
            <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${trend}</span></div>
            <div class="metric-label">${label}</div><div class="metric-value">${value}</div>${renderMetricProgress(progressPercent)}
        </article>
    `).join("")}</div>`;
}

function filterPanel(placeholder, chips) {
    return `<div class="filter-panel"><div class="filter-row"><input class="admin-input" type="search" placeholder="${placeholder}" disabled>${chips.map((chip, idx) => `<button class="chip ${idx === 0 ? "active" : ""}" type="button">${chip}</button>`).join("")}<button class="admin-btn" type="button"><i class="fa-solid fa-rotate-right"></i></button></div></div>`;
}

function tabs(items) {
    return `<div class="tabs" style="margin-bottom:18px">${items.map((item, idx) => `<button class="chip ${idx === 0 ? "active" : ""}" type="button">${item}</button>`).join("")}</div>`;
}

function panelHead(title, sub) {
    return `<div class="panel-head"><div><h2>${title}</h2><span>${sub}</span></div></div>`;
}

function salesPanel(title, sub) {
    return `<div class="panel">${panelHead(title, sub)}<div class="chart-bars">${[62, 58, 70, 65, 76, 68, 86].map((h, i) => `<div class="bar-group"><i class="bar" style="height:${h}%"></i><i class="bar green" style="height:${h - 22}%"></i><i class="bar orange" style="height:${Math.max(22, h - 44)}%"></i></div>`).join("")}</div></div>`;
}

function roomRingPanel() {
    return `<div class="panel">${panelHead("객실 현황", "실시간 기준")}<div class="ring"><strong>84%</strong></div><div class="task-list">${task("사용중 73실 (61%)", "")}${task("예약완료 28실 (23%)", "")}${task("공실 19실 (16%)", "")}</div></div>`;
}

function reservationTable() {
    const headers = ["예약번호", "고객명", "객실", "체크인", "체크아웃", "금액", "상태", "액션"];
    const rows = reservations.map(row => {
        const cells = [`<strong>${row[0]}</strong>`, row[1], row[2], row[3], row[4], `<strong>${row[5]}</strong>`, `<span class="status ${row[7]}">${row[6]}</span>`, actionBtns()];
        return cells;
    });
    return simpleTable(headers, rows, true);
}

function checkinTable() {
    return simpleTable(["", "예약번호", "고객명", "객실 유형", "체크인", "체크아웃", "금액", "상태"], [
        ['<input type="checkbox" disabled>', "RES-2024071", "김민준", "스위트룸 101", "07.17 (목)", "07.19 (토)", "₩556,000", '<span class="status red">승인대기</span>'],
        ['<input type="checkbox" disabled>', "RES-2024070", "이서연", "디럭스룸 205", "07.14 (월)", "07.16 (수)", "₩396,000", '<span class="status">체크인</span>'],
        ['<input type="checkbox" disabled>', "RES-2024069", "박지훈", "스탠다드룸 312", "07.12 (토)", "07.14 (월)", "₩162,000", '<span class="status orange">체크아웃</span>']
    ], true);
}

function simpleTable(headers, rows, raw) {
    return `<div style="overflow-x:auto"><table class="admin-table"><thead><tr>${headers.map(h => `<th>${h}</th>`).join("")}</tr></thead><tbody>${rows.map(row => `<tr>${row.map(cell => `<td>${raw ? cell : escapeHtml(cell)}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
}

function actionBtns() {
    return '<span class="inline-actions"><button class="icon-btn" type="button"><i class="fa-regular fa-eye"></i></button><button class="icon-btn" type="button"><i class="fa-solid fa-pen"></i></button></span>';
}

function todaySchedule() {
    return `<div class="panel">${panelHead("오늘 일정", "체크인 · 체크아웃")}<div class="timeline">${["김민준 · 101호 체크인", "박서연 · 502호 체크인중", "최준혁 · 308호 체크아웃", "정하은 · 402호 취소요청"].map(text => `<div class="timeline-item"><span class="mini-icon"><i class="fa-solid fa-circle"></i></span><strong>${text}</strong></div>`).join("")}</div></div>`;
}

function taskPanel() {
    return `<div class="panel">${panelHead("운영 메모", "최근 알림")}<div class="task-list">${task("취소된 예약은 매출 집계에서 제외됩니다.", "danger")}${task("미답변 리뷰 · 최근 7일 이내", "warn")}${task("고객 문의 · 미확인 문의 메시지", "")}</div></div>`;
}

function checkinPanel() {
    return `<div class="panel">${panelHead("오늘 체크인 현황", "12건")}<div class="task-list">${task("이서연 · 15:00 도착예정", "")}${task("강태양 · 16:00 도착예정", "warn")}<button class="admin-btn" type="button">나머지 10건 더 보기</button></div></div>`;
}

function weekPanel() {
    return `<div class="panel">${panelHead("이번 주 예약 현황", "7.14 ~ 7.20")}<div class="chart-bars" style="height:170px;padding-top:10px">${[66, 44, 58, 36, 82, 92, 84].map((h, i) => `<div class="bar-group"><i class="bar ${i > 3 ? "orange" : ""}" style="height:${h}%"></i></div>`).join("")}</div></div>`;
}

function urgentPanel() {
    return `<div class="panel">${panelHead("즉시 처리 필요", "3건")}<div class="task-list">${task("RES-2024071 · 승인 대기 2시간 초과", "danger")}${task("RES-2024058 · 체크아웃 30분 초과", "warn")}</div></div>`;
}

function roomMap() {
    const floors = [["5F", ["501 use", "502 done", "503", "504", "505 done", "506"]], ["4F", ["401 use", "402 use", "403 done", "404 done", "405", "406", "407 done", "408 use"]], ["3F", ["301", "302 done", "303 use", "304 use", "305", "306 done", "307", "308 done"]], ["2F", ["201 use", "202 done", "203 done", "204", "205 use", "206", "207 done"]]];
    return `<div class="room-map">${floors.map(([floor, rooms]) => `<div class="floor-row"><div class="floor-label">${floor}</div>${rooms.map(room => {
        const [num, cls = ""] = room.split(" ");
        const label = cls === "use" ? "사용중" : cls === "done" ? "예약완료" : "공실";
        return `<div class="room-cell ${cls}">${num}<span>${label}</span></div>`;
    }).join("")}</div>`).join("")}</div>`;
}

function rateSimulator() {
    return `${panelHead("요금 시뮬레이터", "실시간 계산")}<div class="task-list"><label>객실 유형<select class="admin-input" disabled><option>스탠다드룸</option></select></label><label>체크인 날짜<input class="admin-input" type="text" value="2025.07.18 ~ 2025.07.20" disabled></label><div class="notice"><strong>총 결제 금액</strong><span style="margin-left:auto;font-size:24px;font-weight:950;color:#15965f">₩644,600</span></div><button class="admin-btn primary" type="button">요금 재계산</button></div>`;
}

function barLines(items) {
    return `<div class="mini-chart">${items.map(([label, percent, value]) => `<div class="bar-line"><strong>${label}</strong><span class="bar-track"><i style="width:${percent}%"></i></span><strong>${value}</strong></div>`).join("")}</div>`;
}

function settingCard(title, body) {
    return `<article class="panel"><div class="metric-top"><span class="metric-icon"><i class="fa-solid fa-gear"></i></span><button class="icon-btn" type="button"><i class="fa-solid fa-chevron-right"></i></button></div><h2>${title}</h2><p style="color:#64748b;font-weight:800">${body}</p></article>`;
}

function task(text, tone) {
    return `<div class="task ${tone || ""}"><span class="mini-icon"><i class="fa-solid fa-circle-info"></i></span><strong>${text}</strong></div>`;
}

function getAdminAuth() {
    try {
        const value = localStorage.getItem("staynowAuth") || sessionStorage.getItem("staynowAuth");
        return value ? JSON.parse(value) : null;
    } catch (e) {
        return null;
    }
}

function escapeHtml(value) {
    return String(value == null ? "" : value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function renderDashboard() {
    return `
        <div id="dashboardError" class="admin-error" hidden></div>
        <div id="dashboardMetrics" class="grid stats-grid cols-5">
            ${dashboardMetricSkeleton("오늘 예약", "fa-calendar-check")}
            ${dashboardMetricSkeleton("체크인 예정", "fa-right-to-bracket")}
            ${dashboardMetricSkeleton("객실 점유율", "fa-bed")}
            ${dashboardMetricSkeleton("이번 달 매출", "fa-wallet")}
            ${dashboardMetricSkeleton("평균 평점", "fa-star")}
        </div>
        <div class="grid two-col" style="margin-top:22px">
            <div id="dashboardSalesPanel" class="panel">${panelHead("월별 매출 현황", "결제 데이터 기준")}<div class="admin-loading">매출 데이터를 불러오는 중입니다.</div></div>
            <div id="dashboardRoomPanel" class="panel">${panelHead("객실 현황", "예약/객실 데이터 기준")}<div class="admin-loading">객실 데이터를 불러오는 중입니다.</div></div>
        </div>
        <div class="grid two-col" style="margin-top:22px">
            <div id="dashboardReservationPanel" class="panel">${panelHead("최근 예약 현황", "최신 예약 기준")}<div class="admin-loading">예약 데이터를 불러오는 중입니다.</div></div>
            <div class="grid">
                <div id="dashboardSchedulePanel" class="panel">${panelHead("오늘 일정", "체크인 · 체크아웃")}<div class="admin-loading">오늘 일정을 불러오는 중입니다.</div></div>
                <div id="dashboardTaskPanel" class="panel">${panelHead("처리 필요 항목", "운영 확인 항목")}<div class="admin-loading">확인 항목을 불러오는 중입니다.</div></div>
            </div>
        </div>
        <div id="dashboardRoomMapPanel" class="panel" style="margin-top:22px">${panelHead("객실 현황 맵", "실시간 예약 기준")}<div class="admin-loading">객실 맵을 불러오는 중입니다.</div></div>
    `;
}

function dashboardMetricSkeleton(label, icon) {
    return `
        <article class="metric-card loading">
            <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend">-</span></div>
            <div class="metric-label">${label}</div>
            <div class="metric-value">...</div>
            <div class="progress"><i style="width:42%"></i></div>
        </article>
    `;
}

function loadAdminReviewData(filter) {
    const activeFilter = filter || (ADMIN_REVIEW_STATE && ADMIN_REVIEW_STATE.filter) || "all";
    const requests = {
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", [])
    };

    $.when(requests.hotels, requests.popularHotels)
        .done(function (hotelsResult, popularHotelsResult) {
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);

            renderAdminHotelSelector(hotels, selectedHotel);
            bindAdminReviewControls();

            if (!selectedHotel) {
                ADMIN_REVIEW_STATE = { selectedHotel: null, reviews: [], answers: {}, filter: activeFilter, sort: "latest" };
                renderAdminReviewPage();
                return;
            }

            const endpoint = activeFilter === "photo"
                ? "/review/exists-photo-search"
                : "/review/search";

            adminGet(endpoint + "?hotelId=" + encodeURIComponent(selectedHotel.sid) + "&page=0&size=100&sort=createdAt,desc")
                .then(function (reviewPage) {
                    const reviews = asPageContent(reviewPage).filter(function (review) {
                        return !review.deleted;
                    });
                    ADMIN_REVIEW_STATE = {
                        selectedHotel,
                        reviews,
                        answers: {},
                        photos: {},
                        filter: activeFilter,
                        sort: $("#adminReviewSort").val() || "latest"
                    };
                    fetchAdminReviewExtras(reviews).always(renderAdminReviewPage);
                }, function () {
                    $("#adminReviewList").html(emptyAdminState("리뷰 데이터를 불러오지 못했습니다."));
                });
        })
        .fail(function () {
            renderAdminHotelSelector([], null);
            $("#adminReviewList").html(emptyAdminState("리뷰 데이터를 불러오지 못했습니다."));
        });
}

function fetchAdminReviewExtras(reviews) {
    return $.when(fetchAdminReviewAnswers(reviews), fetchAdminReviewPhotos(reviews));
}

function fetchAdminReviewAnswers(reviews) {
    if (!reviews.length) {
        return $.Deferred().resolve().promise();
    }

    const requests = reviews.map(function (review) {
        return adminGetSafe("/review-answer/review-search?reviewId=" + encodeURIComponent(review.sid), null);
    });

    return $.when.apply($, requests).done(function () {
        const results = requests.length === 1 ? [arguments[0]] : Array.from(arguments);
        const answers = {};

        reviews.forEach(function (review, index) {
            const answer = normalizeDeferredValue(results[index]);
            if (answer && answer.sid && !answer.deleted) {
                answers[String(review.sid)] = answer;
            }
        });

        if (ADMIN_REVIEW_STATE) {
            ADMIN_REVIEW_STATE.answers = answers;
        }
    });
}

function fetchAdminReviewPhotos(reviews) {
    if (!reviews.length) {
        return $.Deferred().resolve().promise();
    }

    const requests = reviews.map(function (review) {
        return adminGetSafe("/review-photo/search?reviewId=" + encodeURIComponent(review.sid) + "&page=0&size=20", { content: [] });
    });

    return $.when.apply($, requests).done(function () {
        const results = requests.length === 1 ? [arguments[0]] : Array.from(arguments);
        const photos = {};

        reviews.forEach(function (review, index) {
            photos[String(review.sid)] = asPageContent(normalizeDeferredValue(results[index]));
        });

        if (ADMIN_REVIEW_STATE) {
            ADMIN_REVIEW_STATE.photos = photos;
        }
    });
}

function bindAdminReviewControls() {
    $(".admin-review-tabs").off("click.adminReview").on("click.adminReview", "[data-admin-review-filter]", function () {
        const filter = $(this).data("adminReviewFilter");
        $(".admin-review-tabs .chip").removeClass("active");
        $(this).addClass("active");
        loadAdminReviewData(filter);
    });

    $("#adminReviewSort").off("change.adminReview").on("change.adminReview", function () {
        if (ADMIN_REVIEW_STATE) {
            ADMIN_REVIEW_STATE.sort = $(this).val() || "latest";
        }
        renderAdminReviewPage();
    });

    $("#adminReviewList").off("click.adminReviewAnswer").on("click.adminReviewAnswer", "[data-admin-answer-review]", function () {
        answerAdminReview($(this).data("adminAnswerReview"));
    }).off("click.adminReviewPhoto").on("click.adminReviewPhoto", "[data-review-photo-view]", function () {
        openAdminReviewPhotoViewer($(this).data("reviewPhotoView"));
    });
}

function renderAdminReviewPage() {
    if (!ADMIN_REVIEW_STATE) {
        return;
    }

    const state = ADMIN_REVIEW_STATE;
    const allReviews = state.reviews.slice();
    const reviews = sortAdminReviews(filterAdminReviews(allReviews, state.filter), state.sort, state.answers);
    renderAdminReviewMetrics(allReviews, state.answers);

    $(".admin-review-tabs [data-admin-review-filter]").removeClass("active");
    $(".admin-review-tabs [data-admin-review-filter='" + state.filter + "']").addClass("active");
    $("#adminReviewSort").val(state.sort || "latest");

    $("#adminReviewList").html(
        reviews.length
            ? reviews.map(function (review, index) {
                return renderAdminReviewCard(review, index, state);
            }).join("")
            : emptyAdminState(state.selectedHotel ? "조건에 맞는 리뷰가 없습니다." : "선택 가능한 호텔이 없습니다.")
    );
}

function filterAdminReviews(reviews, filter) {
    if (filter === "waiting") {
        const answers = ADMIN_REVIEW_STATE ? ADMIN_REVIEW_STATE.answers : {};
        return reviews.filter(function (review) {
            return !answers[String(review.sid)];
        });
    }
    return reviews;
}

function sortAdminReviews(reviews, sort, answers) {
    return reviews.slice().sort(function (a, b) {
        if (sort === "ratingDesc") return getAdminReviewRating(b) - getAdminReviewRating(a);
        if (sort === "ratingAsc") return getAdminReviewRating(a) - getAdminReviewRating(b);
        if (sort === "waiting") {
            const aWaiting = answers[String(a.sid)] ? 1 : 0;
            const bWaiting = answers[String(b.sid)] ? 1 : 0;
            if (aWaiting !== bWaiting) return aWaiting - bWaiting;
        }
        return getAdminReviewTime(b) - getAdminReviewTime(a);
    });
}

function renderAdminReviewMetrics(reviews, answers) {
    const total = reviews.length;
    const average = total ? reviews.reduce(function (sum, review) {
        return sum + getAdminReviewRating(review);
    }, 0) / total : 0;
    const answered = reviews.filter(function (review) {
        return answers[String(review.sid)];
    }).length;
    const waiting = Math.max(0, total - answered);

    const markup = metrics([
        ["평균 평점", average ? average.toFixed(1) + "점" : "0점", "fa-star", total ? "호텔 기준" : "리뷰 없음", "", total ? Math.min(100, average * 20) : 0],
        ["전체 리뷰", total + "건", "fa-message", "누적", "", total ? 100 : 0],
        ["답변 완료", answered + "건", "fa-circle-check", total ? Math.round(answered / total * 100) + "%" : "0%", "", total ? answered / total * 100 : 0],
        ["답변 대기", waiting + "건", "fa-reply", waiting ? "처리 필요" : "완료", waiting ? "warn" : "", total ? waiting / total * 100 : 0]
    ], 4).replace('class="grid stats-grid cols-4"', 'id="adminReviewMetrics" class="grid stats-grid cols-4"');
    $("#adminReviewMetrics").replaceWith(markup);
}

function renderAdminReviewCard(review, index, state) {
    const answer = state.answers[String(review.sid)];
    const rating = getAdminReviewRating(review);
    const titleNumber = state.reviews.findIndex(function (item) {
        return String(item.sid) === String(review.sid);
    }) + 1;
    const tags = asArray(review.tags);
    const goodTags = tags.filter(function (tag) {
        return isPositiveReviewTag(tag);
    });
    const badTags = tags.filter(function (tag) {
        return isNegativeReviewTag(tag);
    });

    return `
        <article class="admin-review-card">
            <div class="admin-review-avatar">${titleNumber || index + 1}</div>
            <div class="admin-review-main">
                <div class="admin-review-head">
                    <div>
                        <strong>익명 고객</strong>
                        <p>#${titleNumber || index + 1}번째 리뷰 · ${formatAdminReviewDate(review.createdAt)} · ${escapeHtml(review.roomName || "객실 정보 없음")}${review.totalNights ? " · " + escapeHtml(review.totalNights + "박") : ""}</p>
                    </div>
                    <div class="admin-review-rating">
                        <span>${rating.toFixed(1)}</span>
                        <div>${renderAdminReviewStars(rating)}</div>
                    </div>
                </div>
                ${renderAdminReviewCategoryBars(review.categories)}
                <p class="admin-review-content">${escapeHtml(review.content || "작성된 리뷰 내용이 없습니다.")}</p>
                ${renderAdminReviewPhotos(state.photos && state.photos[String(review.sid)])}
                ${renderAdminReviewTagBlock("좋았던 점", goodTags, "good")}
                ${renderAdminReviewTagBlock("아쉬운 점", badTags, "bad")}
                <div class="admin-review-helpful">
                    <span>이 리뷰가 도움이 되었나요?</span>
                    <span><i class="fa-regular fa-thumbs-up"></i> ${Number(review.likeCount || 0)}</span>
                    <span><i class="fa-regular fa-thumbs-down"></i> ${Number(review.dislikeCount || 0)}</span>
                </div>
                ${answer ? renderAdminReviewAnswer(answer) : renderAdminReviewAnswerForm(review)}
            </div>
        </article>
    `;
}

function renderAdminReviewCategoryBars(categories) {
    const items = asArray(categories);
    if (!items.length) return "";
    const labels = ["청결도", "서비스", "위치", "시설", "가성비", "조식"];
    return `<div class="admin-review-categories">${items.map(function (category, index) {
        const score = Number(category.rating || 0);
        const label = labels[index] || ("항목 " + (index + 1));
        return `<span><b>${escapeHtml(label)}</b><i>${renderAdminReviewStars(score)}</i></span>`;
    }).join("")}</div>`;
}

function renderAdminReviewPhotos(photos) {
    const items = asArray(photos).filter(function (photo) {
        return photo && photo.imagePath && !photo.deleted;
    });
    if (!items.length) return "";
    return `<div class="admin-review-photos">${items.map(function (photo) {
        const src = resolveAdminImagePath(photo.imagePath);
        return `<button type="button" class="admin-review-photo" data-review-photo-view="${escapeHtml(src)}"><img src="${escapeHtml(src)}" alt="리뷰 사진"></button>`;
    }).join("")}</div>`;
}

function openAdminReviewPhotoViewer(src) {
    if (!src) return;
    $("body").append(`
        <div class="admin-modal-backdrop nested admin-photo-viewer" data-admin-photo-viewer>
            <button type="button" class="modal-close" data-admin-photo-viewer-close><i class="fa-solid fa-xmark"></i></button>
            <img src="${escapeHtml(src)}" alt="리뷰 사진 크게 보기">
        </div>
    `);
    $("[data-admin-photo-viewer-close], [data-admin-photo-viewer]").on("click", function (event) {
        if (event.target !== this) return;
        $(".admin-photo-viewer").remove();
    });
}

function renderAdminReviewTagBlock(label, tags, tone) {
    if (!tags.length) return "";
    return `<div class="admin-review-tags ${tone}">
        <strong>${label}</strong>
        ${tags.map(function (tag) {
            return `<span>${escapeHtml(tag.reviewTagName || "태그")}</span>`;
        }).join("")}
    </div>`;
}

function renderAdminReviewAnswer(answer) {
    return `<div class="admin-review-answer">
        <strong><i class="fa-solid fa-reply"></i> 관리자 답변</strong>
        <p>${escapeHtml(answer.reviewAnswer || "")}</p>
        <small>${formatAdminReviewDate(answer.createdAt)}</small>
    </div>`;
}

function renderAdminReviewAnswerForm(review) {
    return `<div class="admin-review-answer-form">
        <textarea id="adminAnswerText-${escapeHtml(review.sid)}" placeholder="고객에게 전달할 답변을 입력하세요."></textarea>
        <button class="admin-btn primary" type="button" data-admin-answer-review="${escapeHtml(review.sid)}">
            <i class="fa-solid fa-paper-plane"></i> 답변 등록
        </button>
    </div>`;
}

function answerAdminReview(reviewId) {
    const textarea = $("#adminAnswerText-" + reviewId);
    const reviewAnswer = String(textarea.val() || "").trim();
    if (!reviewAnswer) {
        alert("답변 내용을 입력해주세요.");
        textarea.focus();
        return;
    }

    const button = $("[data-admin-answer-review='" + reviewId + "']");
    button.prop("disabled", true).html('<i class="fa-solid fa-circle-notch fa-spin"></i> 등록 중');

    adminPost("/review-answer", { reviewId: Number(reviewId), reviewAnswer })
        .then(function () {
            loadAdminReviewData(ADMIN_REVIEW_STATE ? ADMIN_REVIEW_STATE.filter : "all");
        }, function (xhr) {
            alert(getAdminAjaxMessage(xhr, "리뷰 답변 등록에 실패했습니다."));
            button.prop("disabled", false).html('<i class="fa-solid fa-paper-plane"></i> 답변 등록');
        });
}

function openReviewTagManager() {
    openReviewMasterManager({
        title: "리뷰 태그 관리",
        description: "리뷰 작성에서 사용하는 장점/아쉬운 점 태그입니다.",
        endpoint: "/review-tag-master",
        nameKeys: ["reviewTagName", "title", "name"],
        hasCategory: true,
        categoryKey: "reviewTagCategory",
        emptyText: "등록된 리뷰 태그가 없습니다.",
        makePayload: function (sid, name, category) {
            return {
                sid: sid || null,
                reviewTagName: name,
                reviewTagCategory: category || "PROS"
            };
        }
    });
}

function openReviewCategoryManager() {
    openReviewMasterManager({
        title: "리뷰 카테고리 관리",
        description: "청결도, 서비스, 위치 같은 항목별 평점 카테고리입니다.",
        endpoint: "/review-category-master",
        nameKeys: ["reviewCategoryName", "title", "name"],
        hasCategory: false,
        emptyText: "등록된 리뷰 카테고리가 없습니다.",
        makePayload: function (sid, name) {
            return {
                sid: sid || null,
                reviewCategoryName: name
            };
        }
    });
}

function openReviewMasterManager(config) {
    closeReviewMasterManager();
    $("body").append(`
        <div class="admin-modal-backdrop nested review-master-backdrop">
            <div class="admin-modal hotel-master-modal review-master-modal">
                <div class="admin-modal-head">
                    <div><h2>${escapeHtml(config.title)}</h2><p>${escapeHtml(config.description)}</p></div>
                    <button type="button" class="modal-close" data-review-master-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <form class="hotel-master-form review-master-form">
                    <input class="admin-input" type="hidden" id="reviewMasterSid">
                    <input class="admin-input" type="text" id="reviewMasterName" placeholder="이름">
                    <select class="admin-select" id="reviewMasterCategory" ${config.hasCategory ? "" : "hidden"}>
                        <option value="PROS">좋았던 점</option>
                        <option value="CONS">아쉬운 점</option>
                    </select>
                    <button class="admin-btn primary" type="submit"><i class="fa-solid fa-floppy-disk"></i> 저장</button>
                    <button class="admin-btn ghost" type="button" data-review-master-reset><i class="fa-solid fa-rotate-left"></i> 초기화</button>
                </form>
                <div class="hotel-master-list review-master-list"><div class="admin-loading">목록을 불러오는 중입니다.</div></div>
            </div>
        </div>
    `);

    $("[data-review-master-close]").on("click", closeReviewMasterManager);
    $("[data-review-master-reset]").on("click", resetReviewMasterForm);
    $(".review-master-form").on("submit", function (event) {
        event.preventDefault();
        saveReviewMaster(config);
    });
    $(".review-master-list").on("click", "[data-review-master-edit]", function () {
        const sid = Number($(this).data("review-master-edit"));
        const item = ($(".review-master-backdrop").data("items") || []).find(function (target) {
            return Number(target.sid) === sid;
        });
        if (item) fillReviewMasterForm(item, config);
    });
    $(".review-master-list").on("click", "[data-review-master-delete]", function () {
        deleteReviewMaster(Number($(this).data("review-master-delete")), config);
    });

    loadReviewMasterItems(config);
}

function closeReviewMasterManager() {
    $(".review-master-backdrop").remove();
}

function renderReviewMasterRows(items, config) {
    if (!items.length) {
        return `<div class="empty-admin-state">${escapeHtml(config.emptyText)}</div>`;
    }

    return items.map(function (item) {
        const title = pickFirstValue(item, config.nameKeys) || "-";
        const meta = config.hasCategory && item[config.categoryKey] ? formatReviewMasterMeta(item[config.categoryKey]) : "";

        return `<article class="hotel-master-card review-master-card">
            <div class="hotel-master-card-main">
                <div class="hotel-master-item-title"><strong>${escapeHtml(title)}</strong><span>ID ${escapeHtml(item.sid)}</span></div>
                ${meta ? `<small>${escapeHtml(formatReviewMasterMeta(meta))}</small>` : ""}
            </div>
            <div class="row-actions">
                <button class="icon-btn" type="button" title="수정" data-review-master-edit="${escapeHtml(item.sid)}"><i class="fa-solid fa-pen"></i></button>
                <button class="icon-btn danger" type="button" title="삭제" data-review-master-delete="${escapeHtml(item.sid)}"><i class="fa-solid fa-trash"></i></button>
            </div>
        </article>`;
    }).join("");
}

function loadReviewMasterItems(config) {
    $(".review-master-list").html('<div class="admin-loading">목록을 불러오는 중입니다.</div>');
    adminGetSafe(config.endpoint, []).then(function (result) {
        const items = asArray(result);
        $(".review-master-backdrop").data("items", items);
        $(".review-master-list").html(renderReviewMasterRows(items, config));
    });
}

function saveReviewMaster(config) {
    const sid = Number($("#reviewMasterSid").val() || 0);
    const name = String($("#reviewMasterName").val() || "").trim();
    const category = $("#reviewMasterCategory").val();

    if (!name) {
        alert("이름을 입력해주세요.");
        $("#reviewMasterName").focus();
        return;
    }

    const payload = config.makePayload(sid || null, name, category);
    const request = sid ? adminPatch(config.endpoint, payload) : adminPost(config.endpoint, payload);
    request.then(function () {
        resetReviewMasterForm();
        loadReviewMasterItems(config);
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "저장에 실패했습니다."));
    });
}

function fillReviewMasterForm(item, config) {
    $("#reviewMasterSid").val(item.sid || "");
    $("#reviewMasterName").val(pickFirstValue(item, config.nameKeys) || "");
    if (config.hasCategory) {
        $("#reviewMasterCategory").val(item[config.categoryKey] || "PROS");
    }
}

function resetReviewMasterForm() {
    $("#reviewMasterSid").val("");
    $("#reviewMasterName").val("");
    $("#reviewMasterCategory").val("PROS");
}

function deleteReviewMaster(sid, config) {
    if (!sid || !confirm("삭제하시겠습니까?")) return;

    adminDelete(config.endpoint + "/" + sid).then(function () {
        resetReviewMasterForm();
        loadReviewMasterItems(config);
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "삭제에 실패했습니다. 이미 리뷰에 연결된 항목일 수 있습니다."));
    });
}

function pickFirstValue(item, keys) {
    for (const key of keys) {
        if (item && item[key]) return item[key];
    }
    return "";
}

function formatReviewMasterMeta(value) {
    const upper = String(value || "").toUpperCase();
    if (upper === "PROS" || upper === "POSITIVE" || upper === "좋았던 점") return "좋았던 점";
    if (upper === "CONS" || upper === "NEGATIVE" || upper === "아쉬운 점") return "아쉬운 점";
    return value;
}

function renderAdminReviewStars(score) {
    const normalized = Math.max(0, Math.min(5, Number(score || 0)));
    return Array.from({ length: 5 }).map(function (_, index) {
        return `<i class="fa-${index < Math.round(normalized) ? "solid" : "regular"} fa-star"></i>`;
    }).join("");
}

function getAdminReviewRating(review) {
    return Number(review && review.rating || 0);
}

function getAdminReviewTime(review) {
    const date = parseAdminDate(review && review.createdAt);
    return date ? date.getTime() : 0;
}

function formatAdminReviewDate(value) {
    const date = parseAdminDate(value);
    if (!date) return "-";
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, "0")}.${String(date.getDate()).padStart(2, "0")}`;
}

function isPositiveReviewTag(tag) {
    const category = String(tag && tag.reviewTagCategory || "").toUpperCase();
    return ["PROS", "GOOD", "POSITIVE", "PLUS", "ADVANTAGE"].some(function (key) {
        return category.includes(key);
    });
}

function isNegativeReviewTag(tag) {
    const category = String(tag && tag.reviewTagCategory || "").toUpperCase();
    return ["CONS", "BAD", "NEGATIVE", "MINUS", "DISADVANTAGE"].some(function (key) {
        return category.includes(key);
    });
}

function loadAdminHotelManageData() {
    const requests = {
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", []),
        types: adminGetSafe("/hoteltype", []),
        amenities: adminGetSafe("/hotelame", [])
    };

    $.when(requests.hotels, requests.popularHotels, requests.types, requests.amenities)
        .done(function (hotelsResult, popularHotelsResult, typesResult, amenitiesResult) {
            const hotels = mergeAdminHotels(asPageContent(normalizeAjaxResult(hotelsResult)), asArray(normalizeAjaxResult(popularHotelsResult)));
            const selectedHotel = getSelectedAdminHotel(hotels);
            const hotelTypes = asArray(normalizeAjaxResult(typesResult));
            const amenities = asArray(normalizeAjaxResult(amenitiesResult));

            renderAdminHotelSelector(hotels, selectedHotel);

            if (!selectedHotel) {
                ADMIN_HOTEL_MANAGE_STATE = { hotels: [], selectedHotel: null, hotelTypes, amenities, selectedAmenities: [], amenityMappings: [], photos: [] };
                renderHotelManagePage();
                return;
            }

            $.when(
                adminGetSafe("/hotel/" + selectedHotel.sid, selectedHotel),
                adminGetSafe("/hoteliname/hotel/" + selectedHotel.sid, []),
                adminGetSafe("/hotel/inimage/" + selectedHotel.sid, [])
            ).done(function (hotelResult, mappingResult, photoResult) {
                const hotel = Object.assign({}, selectedHotel, normalizeAjaxResult(hotelResult));
                const mappings = asArray(normalizeAjaxResult(mappingResult));
                const photos = asArray(normalizeAjaxResult(photoResult));
                ADMIN_HOTEL_MANAGE_STATE = {
                    hotels,
                    selectedHotel: hotel,
                    hotelTypes,
                    amenities,
                    selectedAmenities: mappings.map(function (mapping) { return mapping.amenitiesId; }),
                    amenityMappings: mappings,
                    photos
                };
                renderHotelManagePage();
            });
        })
        .fail(function () {
            renderAdminHotelSelector([], null);
            $("#hotelManageFields").html(emptyAdminState("호텔 관리 데이터를 불러오지 못했습니다."));
        });
}

function renderHotelManagePage() {
    const state = ADMIN_HOTEL_MANAGE_STATE;
    if (!state || !state.selectedHotel) {
        $("#hotelManageFields").html(emptyAdminState("선택 가능한 호텔이 없습니다. 우측 상단 호텔 추가를 눌러 등록해주세요."));
        $("#hotelPhotoGrid").empty();
        return;
    }

    $("#hotelManageFields").removeClass("admin-loading").html(renderHotelManageFields(state.selectedHotel, state, false));
    initHotelPhotoManager(state.selectedHotel);
    bindHotelManageControls();
}

function renderHotelManageFields(hotel, state, isCreate) {
    const selectedTypeId = getHotelTypeIdFromHotel(hotel, state.hotelTypes);
    const typeOptions = state.hotelTypes.map(function (type) {
        return `<option value="${escapeHtml(type.sid)}"${String(type.sid) === String(selectedTypeId) ? " selected" : ""}>${escapeHtml(type.title || "타입")}</option>`;
    }).join("");
    const amenities = state.amenities.map(function (amenity) {
        const checked = state.selectedAmenities.some(function (id) { return String(id) === String(amenity.sid); }) ? " checked" : "";
        return `<label class="hotel-amenity-check">
            <input type="checkbox" name="amenities" value="${escapeHtml(amenity.sid)}"${checked}>
            <span><strong>${escapeHtml(amenity.title || "편의시설")}</strong><small>${escapeHtml(amenity.description || "")}</small></span>
        </label>`;
    }).join("");

    const selectedAmenityCount = state.selectedAmenities.length;

    return `
        <input type="hidden" id="hotelSidInput" value="${escapeHtml(hotel.sid || "")}">
        <div class="hotel-info-surface">
            <div class="hotel-info-layout${isCreate ? " single" : ""}">
                <div class="admin-form-grid hotel-form-grid">
                    <label class="hotel-type-field">
                        <span class="hotel-field-label-row"><span class="hotel-field-title"><i class="fa-solid fa-layer-group"></i>호텔 타입</span></span>
                        <select id="hotelTypeInput">${typeOptions}</select>
                    </label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-hotel"></i>호텔 이름</span><input id="hotelNameInput" value="${escapeHtml(hotel.hotelName || "")}" placeholder="호텔 이름"></label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-won-sign"></i>호텔 가격</span><input id="hotelPriceInput" type="number" min="0" value="${escapeHtml(hotel.hotelPrice || "")}" placeholder="대표 가격"></label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-location-dot"></i>장소</span><input id="hotelLocationInput" value="${escapeHtml(hotel.location || "")}" placeholder="주소"></label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-star"></i>성급</span><input id="hotelStarInput" type="number" min="1" max="5" value="${escapeHtml(hotel.starRating || "")}" placeholder="1~5"></label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-map-pin"></i>위도</span><input id="hotelLatitudeInput" type="number" step="0.000001" value="${escapeHtml(hotel.latitude || "")}" placeholder="37.000000"></label>
                    <label><span class="hotel-field-title"><i class="fa-solid fa-map-location-dot"></i>경도</span><input id="hotelLongitudeInput" type="number" step="0.000001" value="${escapeHtml(hotel.longitude || "")}" placeholder="127.000000"></label>
                    <label class="full"><span class="hotel-field-title"><i class="fa-solid fa-align-left"></i>설명</span><textarea id="hotelDescriptionInput" placeholder="호텔 소개">${escapeHtml(hotel.description || "")}</textarea></label>
                </div>
                ${isCreate ? "" : `<section class="hotel-photo-inline" aria-label="호텔 사진">
                    <div class="hotel-field-head"><div><span>호텔 사진</span><small>대표 및 서브 이미지로 사용됩니다.</small></div></div>
                    <div id="hotelPhotoGrid" class="room-photo-grid hotel-photo-grid"></div>
                    <input id="hotelPhotoFilesInput" class="room-photo-native-input" type="file" accept="image/*" multiple>
                </section>`}
            </div>
            <div class="hotel-amenities-field">
                <div class="hotel-field-head"><div><span>편의시설</span><small>검색해서 필요한 편의시설만 선택하세요.</small></div></div>
                <div class="hotel-amenity-tools">
                    <label class="hotel-amenity-search"><i class="fa-solid fa-magnifying-glass"></i><input type="search" data-hotel-amenity-search placeholder="편의시설명 또는 설명 검색"></label>
                    <span class="hotel-amenity-count" data-hotel-amenity-count>선택 ${selectedAmenityCount}개</span>
                </div>
                <div class="hotel-amenity-grid" data-hotel-amenity-list>${amenities || '<div class="empty-admin-state">등록된 편의시설이 없습니다.</div>'}</div>
            </div>
        </div>
        <div class="admin-modal-actions inline">
            <button type="button" class="admin-btn" data-hotel-reset><i class="fa-solid fa-rotate-left"></i> 되돌리기</button>
            <button type="submit" class="admin-btn primary"><i class="fa-solid fa-check"></i> ${isCreate ? "호텔 추가" : "호텔 저장"}</button>
        </div>
    `;
}

function bindHotelManageControls() {
    $("#hotelManageForm").off("submit.hotelManage").on("submit.hotelManage", function (event) {
        event.preventDefault();
        saveHotelManage();
    });
    $("[data-hotel-reset]").off("click.hotelManage").on("click.hotelManage", function () {
        renderHotelManagePage();
    });
    $("[data-hotel-type-manage]").off("click.hotelManage").on("click.hotelManage", openHotelTypeManager);
    $("[data-hotel-amenity-manage]").off("click.hotelManage").on("click.hotelManage", openHotelAmenityManager);
    bindHotelAmenitySearch($("#hotelManageForm"));
}

function bindHotelAmenitySearch(root) {
    const $root = $(root || document);
    $root.find("[data-hotel-amenity-search]").off("input.hotelAmenitySearch").on("input.hotelAmenitySearch", function () {
        filterHotelAmenityList($(this).closest(".hotel-amenities-field"));
    });
    $root.find(".hotel-amenity-check input").off("change.hotelAmenitySearch").on("change.hotelAmenitySearch", function () {
        const field = $(this).closest(".hotel-amenities-field");
        const list = field.find("[data-hotel-amenity-list]");
        const scrollTop = list.scrollTop();
        updateHotelAmenityCount(field);
        requestAnimationFrame(function () {
            list.scrollTop(scrollTop);
        });
    });
    $root.find(".hotel-amenities-field").each(function () {
        filterHotelAmenityList($(this));
        updateHotelAmenityCount($(this));
    });
}

function filterHotelAmenityList(field) {
    const keyword = String(field.find("[data-hotel-amenity-search]").val() || "").trim().toLowerCase();
    field.find(".hotel-amenity-check").each(function () {
        const text = $(this).text().toLowerCase();
        $(this).toggle(!keyword || text.includes(keyword));
    });
}

function updateHotelAmenityCount(field) {
    field.find("[data-hotel-amenity-count]").text("선택 " + field.find(".hotel-amenity-check input:checked").length + "개");
}

function readHotelPayload() {
    return {
        sid: Number($("#hotelSidInput").val()) || null,
        typeId: Number($("#hotelTypeInput").val()) || null,
        hotelName: $("#hotelNameInput").val().trim(),
        hotelPrice: Number($("#hotelPriceInput").val()) || null,
        location: $("#hotelLocationInput").val().trim(),
        starRating: Number($("#hotelStarInput").val()) || null,
        description: $("#hotelDescriptionInput").val().trim(),
        latitude: $("#hotelLatitudeInput").val() === "" ? null : Number($("#hotelLatitudeInput").val()),
        longitude: $("#hotelLongitudeInput").val() === "" ? null : Number($("#hotelLongitudeInput").val())
    };
}

function saveHotelManage() {
    const payload = readHotelPayloadFrom($("#hotelManageForm"));
    if (!payload.typeId || !payload.hotelName || !payload.hotelPrice || !payload.location) {
        alert("호텔 타입, 이름, 가격, 장소는 필수입니다.");
        return;
    }
    const submitButton = $("#hotelManageForm button[type='submit']");
    submitButton.prop("disabled", true).html('<i class="fa-solid fa-circle-notch fa-spin"></i> 저장 중');
    adminPatch("/hotel", payload).then(function (savedHotel) {
        saveHotelAmenitiesAfterSave(savedHotel.sid || payload.sid)
            .then(function () { return saveHotelPhotosAfterSave(savedHotel.sid || payload.sid); })
            .then(function () {
                showAdminNotice("호텔 정보가 저장되었습니다.", "success");
            }, function (xhr) {
                showAdminNotice(getAdminAjaxMessage(xhr, "호텔 부가 정보 저장 중 일부 실패했습니다."), "danger");
            })
            .always(function () {
                submitButton.prop("disabled", false).html('<i class="fa-solid fa-check"></i> 호텔 저장');
                loadAdminHotelManageData();
            });
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "호텔 저장에 실패했습니다."));
        submitButton.prop("disabled", false).html('<i class="fa-solid fa-check"></i> 호텔 저장');
    });
}

function readHotelPayloadFrom(root) {
    const $root = $(root);
    return {
        sid: Number($root.find("#hotelSidInput").val()) || null,
        typeId: Number($root.find("#hotelTypeInput").val()) || null,
        hotelName: $root.find("#hotelNameInput").val().trim(),
        hotelPrice: Number($root.find("#hotelPriceInput").val()) || null,
        location: $root.find("#hotelLocationInput").val().trim(),
        starRating: Number($root.find("#hotelStarInput").val()) || null,
        description: $root.find("#hotelDescriptionInput").val().trim(),
        latitude: $root.find("#hotelLatitudeInput").val() === "" ? null : Number($root.find("#hotelLatitudeInput").val()),
        longitude: $root.find("#hotelLongitudeInput").val() === "" ? null : Number($root.find("#hotelLongitudeInput").val())
    };
}

function openHotelManageModal() {
    const state = ADMIN_HOTEL_MANAGE_STATE || { hotelTypes: [], amenities: [] };
    if (!state.hotelTypes.length) {
        alert("호텔 타입을 먼저 등록해주세요.");
        return;
    }
    ADMIN_HOTEL_CREATE_PHOTO_STATE = { added: [] };
    const modalState = Object.assign({}, state, { selectedAmenities: [] });
    $("#hotelManageModalRoot").html(`
        <div class="admin-modal-backdrop">
            <form id="hotelCreateForm" class="admin-modal hotel-modal">
                <div class="admin-modal-head">
                    <div><h2>호텔 추가</h2><p>기본 정보, 편의시설, 사진을 함께 등록합니다.</p></div>
                    <button type="button" class="modal-close" data-hotel-modal-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="hotel-create-body">
                    ${renderHotelManageFields({}, modalState, true)}
                    <div class="hotel-create-photo">
                        <div class="room-photo-title"><span>호텔 사진</span><small>사진을 하나씩 또는 여러 장 추가할 수 있습니다.</small></div>
                        <div id="hotelCreatePhotoGrid" class="room-photo-grid"></div>
                        <input id="hotelCreatePhotoFilesInput" class="room-photo-native-input" type="file" accept="image/*" multiple>
                    </div>
                </div>
                <div class="admin-modal-actions">
                    <button type="button" class="admin-btn" data-hotel-modal-close>취소</button>
                    <button type="submit" class="admin-btn primary">호텔 추가</button>
                </div>
            </form>
        </div>
    `);
    $("#hotelCreateForm .admin-modal-actions.inline").remove();
    $("[data-hotel-modal-close]").on("click", closeHotelManageModal);
    $("#hotelCreateForm [data-hotel-type-manage]").on("click", openHotelTypeManager);
    $("#hotelCreateForm [data-hotel-amenity-manage]").on("click", openHotelAmenityManager);
    bindHotelAmenitySearch($("#hotelCreateForm"));
    bindHotelCreatePhotoManager();
    renderHotelCreatePhotoGrid();
    $("#hotelCreateForm").on("submit", function (event) {
        event.preventDefault();
        saveHotelCreate();
    });
}

function closeHotelManageModal() {
    clearHotelCreatePhotoObjectUrls();
    $("#hotelManageModalRoot").empty();
}

function openHotelImportModal() {
    $("#hotelManageModalRoot").html(`
        <div class="admin-modal-backdrop">
            <form id="hotelImportForm" class="admin-modal hotel-import-modal">
                <div class="admin-modal-head">
                    <div><h2>호텔 불러오기</h2><p>먼저 TourAPI 후보를 조회하고, 선택한 호텔만 저장합니다.</p></div>
                    <button type="button" class="modal-close" data-hotel-import-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="hotel-import-body">
                    <div class="hotel-import-fields">
                        <label>
                            <span><i class="fa-solid fa-magnifying-glass"></i> 검색어</span>
                            <input id="tourApiKeywordInput" class="admin-input" type="text" value="서울" placeholder="예: 서울, 부산, 라마다">
                        </label>
                        <label>
                            <span><i class="fa-regular fa-file-lines"></i> 페이지</span>
                            <input id="tourApiPageInput" class="admin-input" type="number" min="1" value="1">
                        </label>
                        <label>
                            <span><i class="fa-solid fa-list-ol"></i> 개수</span>
                            <input id="tourApiSizeInput" class="admin-input" type="number" min="1" max="100" value="10">
                        </label>
                    </div>
                    <div id="hotelImportStatus" class="hotel-import-status" hidden>
                        <i class="fa-solid fa-circle-notch fa-spin"></i>
                        <span>호텔 정보를 불러오는 중입니다.</span>
                    </div>
                    <div id="hotelImportPreview" class="hotel-import-preview">
                        <div class="empty-admin-state">검색어를 입력하고 후보 조회를 눌러주세요.</div>
                    </div>
                </div>
                <div class="admin-modal-actions">
                    <button type="button" class="admin-btn" data-hotel-import-close>취소</button>
                    <button id="hotelImportSearch" type="submit" class="admin-btn"><i class="fa-solid fa-magnifying-glass"></i> 후보 조회</button>
                    <button id="hotelImportSubmit" type="button" class="admin-btn primary" disabled><i class="fa-solid fa-cloud-arrow-down"></i> 선택 호텔 불러오기</button>
                </div>
            </form>
        </div>
    `);

    $("[data-hotel-import-close]").on("click", closeHotelManageModal);
    $("#hotelImportForm").on("submit", function (event) {
        event.preventDefault();
        previewTourApiHotelsFromAdmin();
    });
    $("#hotelImportSubmit").on("click", importSelectedTourApiHotelsFromAdmin);
    $("#hotelImportPreview").on("change", ".hotel-import-check", updateHotelImportSelectionState);
    $("#hotelImportPreview").on("click", "[data-hotel-import-select-all]", function () {
        $(".hotel-import-check:not(:disabled)").prop("checked", true);
        updateHotelImportSelectionState();
    });
}

function readTourApiImportForm() {
    const keyword = $("#tourApiKeywordInput").val().trim();
    const page = Number($("#tourApiPageInput").val()) || 1;
    const size = Number($("#tourApiSizeInput").val()) || 10;

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        $("#tourApiKeywordInput").focus();
        return;
    }
    if (page < 1 || size < 1) {
        alert("페이지와 개수는 1 이상으로 입력해주세요.");
        return null;
    }

    return { keyword, page, size };
}

function setHotelImportLoading(isLoading, message) {
    const form = $("#hotelImportForm");
    const status = $("#hotelImportStatus");
    form.find("input, button").prop("disabled", isLoading);
    $("#hotelImportSubmit").prop("disabled", isLoading || $(".hotel-import-check:checked").length === 0);
    status.find("span").text(message || "호텔 정보를 불러오는 중입니다.");
    status.prop("hidden", !isLoading);
}

function previewTourApiHotelsFromAdmin() {
    const query = readTourApiImportForm();
    if (!query) {
        return;
    }

    $("#hotelImportPreview").html("");
    setHotelImportLoading(true, "TourAPI 후보를 조회하는 중입니다.");

    $.ajax({
        url: window.StayNowConfig.apiUrl("/hotel/import/tourapi/preview")
            + "?keyword=" + encodeURIComponent(query.keyword)
            + "&page=" + encodeURIComponent(query.page)
            + "&size=" + encodeURIComponent(query.size),
        type: "GET",
        headers: adminAuthHeaders()
    }).then(function (response) {
        renderTourApiHotelPreview(unwrapApiResponse(response) || []);
    }, function (xhr) {
        $("#hotelImportPreview").html(`<div class="empty-admin-state">${escapeHtml(getAdminAjaxMessage(xhr, "호텔 후보를 불러오지 못했습니다."))}</div>`);
    }).always(function () {
        setHotelImportLoading(false);
    });
}

function renderTourApiHotelPreview(hotels) {
    if (!hotels.length) {
        $("#hotelImportPreview").html('<div class="empty-admin-state">조회된 호텔이 없습니다. 지역명만 입력하거나 다른 키워드를 사용해보세요.</div>');
        updateHotelImportSelectionState();
        return;
    }

    const availableCount = hotels.filter(function (hotel) { return !hotel.alreadyImported; }).length;
    const list = hotels.map(function (hotel) {
        const disabled = hotel.alreadyImported ? " disabled" : "";
        const checked = hotel.alreadyImported ? "" : " checked";
        const image = hotel.imagePath ? resolveAdminImagePath(hotel.imagePath) : "";

        return `<label class="hotel-import-card ${hotel.alreadyImported ? "imported" : ""}">
            <input class="hotel-import-check" type="checkbox" value="${escapeHtml(hotel.contentId || "")}"${checked}${disabled}>
            <span class="hotel-import-thumb">${image ? `<img src="${escapeHtml(image)}" alt="${escapeHtml(hotel.title || "호텔 이미지")}">` : '<i class="fa-solid fa-hotel"></i>'}</span>
            <span class="hotel-import-info">
                <strong>${escapeHtml(hotel.title || "호텔명 없음")}</strong>
                <small>${escapeHtml(hotel.location || "주소 정보 없음")}</small>
                <em>${hotel.alreadyImported ? "이미 등록됨" : "저장 가능"}</em>
            </span>
        </label>`;
    }).join("");

    $("#hotelImportPreview").html(`
        <div class="hotel-import-preview-head">
            <strong>조회 결과 ${hotels.length}건</strong>
            <button type="button" class="admin-btn small" data-hotel-import-select-all${availableCount ? "" : " disabled"}>전체 선택</button>
        </div>
        <div class="hotel-import-list">${list}</div>
    `);
    updateHotelImportSelectionState();
}

function updateHotelImportSelectionState() {
    const count = $(".hotel-import-check:checked").length;
    $("#hotelImportSubmit")
        .prop("disabled", count === 0)
        .html(`<i class="fa-solid fa-cloud-arrow-down"></i> 선택 호텔 ${count}건 불러오기`);
}

function importSelectedTourApiHotelsFromAdmin() {
    const query = readTourApiImportForm();
    if (!query) {
        return;
    }

    const contentIds = $(".hotel-import-check:checked").map(function () {
        return this.value;
    }).get().filter(Boolean);

    if (!contentIds.length) {
        alert("불러올 호텔을 선택해주세요.");
        return;
    }

    setHotelImportLoading(true, "선택한 호텔과 기본 객실을 저장하는 중입니다.");

    $.ajax({
        url: window.StayNowConfig.apiUrl("/hotel/import/tourapi/selected"),
        type: "POST",
        headers: Object.assign({ "Content-Type": "application/json" }, adminAuthHeaders()),
        data: JSON.stringify({
            keyword: query.keyword,
            page: query.page,
            size: query.size,
            contentIds: contentIds
        })
    }).then(function (response) {
        const result = unwrapApiResponse(response) || {};
        showAdminNotice(
            "호텔 불러오기 완료: 선택 " + contentIds.length + "건, 신규 " + (result.imported ?? 0) + "건, 제외 " + (result.skipped ?? 0) + "건",
            "success"
        );
        closeHotelManageModal();
        loadAdminHotelManageData();
    }, function (xhr) {
        showAdminNotice(getAdminAjaxMessage(xhr, "선택한 호텔을 저장하지 못했습니다."), "danger");
    }).always(function () {
        setHotelImportLoading(false);
    });
}

function saveHotelCreate() {
    const payload = readHotelPayloadFrom($("#hotelCreateForm"));
    if (!payload.typeId || !payload.hotelName || !payload.hotelPrice || !payload.location) {
        alert("호텔 타입, 이름, 가격, 장소는 필수입니다.");
        return;
    }
    adminPost("/hotel", payload).then(function (savedHotel) {
        const hotelId = savedHotel.sid;
        localStorage.setItem(ADMIN_SELECTED_HOTEL_KEY, String(hotelId));
        saveHotelCreateAmenities(hotelId)
            .then(function () { return uploadHotelPhotoFiles(hotelId, ADMIN_HOTEL_CREATE_PHOTO_STATE.added.map(function (photo) { return photo.file; })) || $.Deferred().resolve().promise(); })
            .always(function () {
                showAdminNotice("호텔이 추가되었습니다.", "success");
                closeHotelManageModal();
                loadAdminHotelManageData();
            });
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "호텔 추가에 실패했습니다."));
    });
}

function saveHotelCreateAmenities(hotelId) {
    const selected = $("#hotelCreateForm input[name='amenities']:checked").map(function () { return Number(this.value); }).get();
    const requests = selected.map(function (amenityId) {
        return adminPost("/hoteliname", { hotelId: Number(hotelId), amenitiesId: Number(amenityId) });
    });
    return requests.length ? $.when.apply($, requests) : $.Deferred().resolve().promise();
}

function bindHotelCreatePhotoManager() {
    $("#hotelCreatePhotoGrid")
        .off("click.hotelCreatePhotos")
        .on("click.hotelCreatePhotos", "[data-hotel-create-photo-add]", function () {
            $("#hotelCreatePhotoFilesInput").trigger("click");
        })
        .on("click.hotelCreatePhotos", "[data-hotel-create-photo-remove]", function () {
            removeHotelCreatePhoto($(this).data("hotelCreatePhotoRemove"));
        });
    $("#hotelCreatePhotoFilesInput").off("change.hotelCreatePhotos").on("change.hotelCreatePhotos", function () {
        Array.from(this.files || []).filter(function (file) {
            return file && file.type && file.type.startsWith("image/");
        }).forEach(function (file) {
            ADMIN_HOTEL_CREATE_PHOTO_STATE.added.push({
                id: "new-" + Date.now() + "-" + Math.random().toString(16).slice(2),
                file,
                previewUrl: URL.createObjectURL(file)
            });
        });
        this.value = "";
        renderHotelCreatePhotoGrid();
    });
}

function removeHotelCreatePhoto(id) {
    const index = ADMIN_HOTEL_CREATE_PHOTO_STATE.added.findIndex(function (photo) { return String(photo.id) === String(id); });
    if (index >= 0) {
        const removed = ADMIN_HOTEL_CREATE_PHOTO_STATE.added.splice(index, 1)[0];
        if (removed.previewUrl) URL.revokeObjectURL(removed.previewUrl);
        renderHotelCreatePhotoGrid();
    }
}

function renderHotelCreatePhotoGrid() {
    const addedTiles = ADMIN_HOTEL_CREATE_PHOTO_STATE.added.map(function (photo) {
        return `<div class="room-photo-tile new"><img src="${escapeHtml(photo.previewUrl)}" alt="추가할 호텔 사진"><button class="room-photo-remove" type="button" data-hotel-create-photo-remove="${escapeHtml(photo.id)}"><i class="fa-solid fa-minus"></i></button></div>`;
    });
    const addTile = `<button class="room-photo-add-tile" type="button" data-hotel-create-photo-add><i class="fa-solid fa-plus"></i><span>사진 추가</span></button>`;
    $("#hotelCreatePhotoGrid").html(addedTiles.join("") + addTile);
}

function clearHotelCreatePhotoObjectUrls() {
    ADMIN_HOTEL_CREATE_PHOTO_STATE.added.forEach(function (photo) {
        if (photo.previewUrl) URL.revokeObjectURL(photo.previewUrl);
    });
}

function openHotelTypeManager() {
    const state = ADMIN_HOTEL_MANAGE_STATE || { hotelTypes: [] };
    openHotelMasterManager({
        title: "호텔 타입 관리",
        placeholder: "호텔, 리조트, 펜션...",
        items: state.hotelTypes,
        endpoint: "/hoteltype",
        fields: ["title"],
        deleteMessage: "이 호텔 타입을 삭제할까요? 연결된 호텔이 있으면 삭제되지 않습니다."
    });
}

function openHotelAmenityManager() {
    const state = ADMIN_HOTEL_MANAGE_STATE || { amenities: [] };
    openHotelMasterManager({
        title: "편의시설 관리",
        placeholder: "무료 와이파이",
        items: state.amenities,
        endpoint: "/hotelame",
        fields: ["title", "description"],
        deleteMessage: "이 편의시설을 삭제할까요? 연결된 호텔 매핑도 함께 삭제됩니다."
    });
}

function openHotelMasterManager(config) {
    const showDescription = config.fields.includes("description");
    $("body").append(`
        <div class="admin-modal-backdrop nested hotel-master-backdrop">
            <div class="admin-modal hotel-master-modal">
                <div class="admin-modal-head">
                    <div><h2>${escapeHtml(config.title)}</h2><p>추가, 수정, 삭제 후 호텔 관리 화면에 반영됩니다.</p></div>
                    <button type="button" class="modal-close" data-hotel-master-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <form id="hotelMasterForm" class="hotel-master-form">
                    <input id="hotelMasterSidInput" type="hidden">
                    <input id="hotelMasterTitleInput" class="admin-input" placeholder="${escapeHtml(config.placeholder)}">
                    ${config.fields.includes("description") ? '<input id="hotelMasterDescriptionInput" class="admin-input" placeholder="설명">' : ""}
                    <button class="admin-btn primary" type="submit"><i class="fa-solid fa-plus"></i> 저장</button>
                </form>
                <div class="hotel-master-list">${renderHotelMasterRows(config)}</div>
            </div>
        </div>
    `);
    $("[data-hotel-master-close]").on("click", closeHotelMasterManager);
    $("#hotelMasterForm").on("submit", function (event) {
        event.preventDefault();
        saveHotelMasterItem(config);
    });
    $(".hotel-master-list")
        .on("click", "[data-hotel-master-edit]", function () {
            const item = config.items.find((target) => String(target.sid) === String($(this).data("hotelMasterEdit")));
            if (!item) return;
            $("#hotelMasterSidInput").val(item.sid);
            $("#hotelMasterTitleInput").val(item.title || "");
            $("#hotelMasterDescriptionInput").val(item.description || "");
        })
        .on("click", "[data-hotel-master-delete]", function () {
            deleteHotelMasterItem(config, $(this).data("hotelMasterDelete"));
        });
}

function renderHotelMasterRows(config) {
    const showDescription = config.fields.includes("description");
    const rows = config.items.map(function (item) {
        return `<article class="hotel-master-card">
            <div class="hotel-master-card-main">
                <div class="hotel-master-item-title"><strong>${escapeHtml(item.title || "-")}</strong><span>ID ${escapeHtml(item.sid)}</span></div>
                ${showDescription && item.description ? `<small>${escapeHtml(item.description)}</small>` : ""}
            </div>
            <div class="row-actions">
                <button class="icon-btn" type="button" data-hotel-master-edit="${escapeHtml(item.sid)}"><i class="fa-solid fa-pen"></i></button>
                <button class="icon-btn danger" type="button" data-hotel-master-delete="${escapeHtml(item.sid)}"><i class="fa-solid fa-trash"></i></button>
            </div>
        </article>`;
    }).join("");
    return rows || '<div class="empty-admin-state">등록된 항목이 없습니다.</div>';
}

function closeHotelMasterManager() {
    $(".hotel-master-backdrop").remove();
}

function saveHotelMasterItem(config) {
    const sid = $("#hotelMasterSidInput").val();
    const title = $("#hotelMasterTitleInput").val().trim();
    const description = $("#hotelMasterDescriptionInput").val() ? $("#hotelMasterDescriptionInput").val().trim() : null;
    if (!title) {
        alert("이름을 입력해주세요.");
        return;
    }
    const payload = { title };
    if (sid) payload.sid = Number(sid);
    if (config.fields.includes("description")) payload.description = description;
    const request = sid ? adminPatch(config.endpoint, payload) : adminPost(config.endpoint, payload);
    request.then(function () {
        refreshHotelMasterManager(config, true);
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "저장에 실패했습니다."));
    });
}

function deleteHotelMasterItem(config, id) {
    if (!id || !confirm(config.deleteMessage)) return;
    $.ajax({
        url: window.StayNowConfig.apiUrl(config.endpoint + "/" + id),
        type: "DELETE",
        headers: adminAuthHeaders()
    }).done(function () {
        refreshHotelMasterManager(config, true);
    }).fail(function (xhr) {
        alert(getAdminAjaxMessage(xhr, "삭제에 실패했습니다."));
    });
}

function refreshHotelMasterManager(config, clearForm) {
    adminGet(config.endpoint).then(function (result) {
        const items = asArray(result);
        config.items = items;

        if (!ADMIN_HOTEL_MANAGE_STATE) {
            ADMIN_HOTEL_MANAGE_STATE = {};
        }

        if (config.endpoint === "/hoteltype") {
            ADMIN_HOTEL_MANAGE_STATE.hotelTypes = items;
        } else if (config.endpoint === "/hotelame") {
            ADMIN_HOTEL_MANAGE_STATE.amenities = items;
        }

        renderHotelManagePage();
        $(".hotel-master-list").html(renderHotelMasterRows(config));

        if (clearForm) {
            $("#hotelMasterSidInput").val("");
            $("#hotelMasterTitleInput").val("").focus();
            $("#hotelMasterDescriptionInput").val("");
        }
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "목록을 새로고침하지 못했습니다."));
    });
}

function saveHotelAmenitiesAfterSave(hotelId) {
    const state = ADMIN_HOTEL_MANAGE_STATE || { amenityMappings: [] };
    const selected = $("input[name='amenities']:checked").map(function () { return Number(this.value); }).get();
    const current = state.amenityMappings || [];
    const deleteRequests = current
        .filter(function (mapping) { return !selected.some(function (id) { return String(id) === String(mapping.amenitiesId); }); })
        .map(function (mapping) {
            return $.ajax({ url: window.StayNowConfig.apiUrl("/hoteliname/" + mapping.sid), type: "DELETE", headers: adminAuthHeaders() });
        });
    const addRequests = selected
        .filter(function (amenityId) { return !current.some(function (mapping) { return String(mapping.amenitiesId) === String(amenityId); }); })
        .map(function (amenityId) {
            return adminPost("/hoteliname", { hotelId: Number(hotelId), amenitiesId: Number(amenityId) });
        });
    const requests = deleteRequests.concat(addRequests);
    return requests.length ? $.when.apply($, requests) : $.Deferred().resolve().promise();
}

function initHotelPhotoManager(hotel) {
    clearHotelPhotoObjectUrls();
    ADMIN_HOTEL_PHOTO_STATE = {
        existing: asArray(ADMIN_HOTEL_MANAGE_STATE.photos).map(function (photo) { return { sid: photo.sid, imagePath: photo.imagePath }; }),
        added: [],
        deleted: []
    };
    bindHotelPhotoManager();
    renderHotelPhotoGrid();
}

function bindHotelPhotoManager() {
    $("#hotelPhotoGrid").off("click.hotelPhotos")
        .on("click.hotelPhotos", "[data-hotel-photo-add]", function () { $("#hotelPhotoFilesInput").trigger("click"); })
        .on("click.hotelPhotos", "[data-hotel-photo-remove]", function () { removeHotelPhoto($(this).data("hotelPhotoRemove")); });
    $("#hotelPhotoFilesInput").off("change.hotelPhotos").on("change.hotelPhotos", function () {
        addHotelPhotoFiles(this.files);
        this.value = "";
    });
}

function addHotelPhotoFiles(fileList) {
    Array.from(fileList || []).filter(function (file) {
        return file && file.type && file.type.startsWith("image/");
    }).forEach(function (file) {
        ADMIN_HOTEL_PHOTO_STATE.added.push({
            id: "new-" + Date.now() + "-" + Math.random().toString(16).slice(2),
            file,
            previewUrl: URL.createObjectURL(file)
        });
    });
    renderHotelPhotoGrid();
}

function removeHotelPhoto(id) {
    const key = String(id);
    const existingIndex = ADMIN_HOTEL_PHOTO_STATE.existing.findIndex(function (photo) { return String(photo.sid) === key; });
    if (existingIndex >= 0) {
        const removed = ADMIN_HOTEL_PHOTO_STATE.existing.splice(existingIndex, 1)[0];
        if (removed.sid) ADMIN_HOTEL_PHOTO_STATE.deleted.push(removed.sid);
        renderHotelPhotoGrid();
        return;
    }
    const addedIndex = ADMIN_HOTEL_PHOTO_STATE.added.findIndex(function (photo) { return String(photo.id) === key; });
    if (addedIndex >= 0) {
        const removed = ADMIN_HOTEL_PHOTO_STATE.added.splice(addedIndex, 1)[0];
        if (removed.previewUrl) URL.revokeObjectURL(removed.previewUrl);
        renderHotelPhotoGrid();
    }
}

function renderHotelPhotoGrid() {
    const existingTiles = ADMIN_HOTEL_PHOTO_STATE.existing.map(function (photo) {
        return `<div class="room-photo-tile"><img src="${escapeHtml(resolveAdminImagePath(photo.imagePath))}" alt="호텔 사진"><button class="room-photo-remove" type="button" data-hotel-photo-remove="${escapeHtml(photo.sid)}"><i class="fa-solid fa-minus"></i></button></div>`;
    });
    const addedTiles = ADMIN_HOTEL_PHOTO_STATE.added.map(function (photo) {
        return `<div class="room-photo-tile new"><img src="${escapeHtml(photo.previewUrl)}" alt="추가할 호텔 사진"><button class="room-photo-remove" type="button" data-hotel-photo-remove="${escapeHtml(photo.id)}"><i class="fa-solid fa-minus"></i></button></div>`;
    });
    const addTile = `<button class="room-photo-add-tile" type="button" data-hotel-photo-add><i class="fa-solid fa-plus"></i><span>사진 추가</span></button>`;
    $("#hotelPhotoGrid").html(existingTiles.concat(addedTiles).join("") + addTile);
}

function clearHotelPhotoObjectUrls() {
    if (!ADMIN_HOTEL_PHOTO_STATE || !ADMIN_HOTEL_PHOTO_STATE.added) return;
    ADMIN_HOTEL_PHOTO_STATE.added.forEach(function (photo) {
        if (photo.previewUrl) URL.revokeObjectURL(photo.previewUrl);
    });
}

function saveHotelPhotosAfterSave(hotelId) {
    const deleteRequests = ADMIN_HOTEL_PHOTO_STATE.deleted.map(function (photoId) {
        return $.ajax({ url: window.StayNowConfig.apiUrl("/hotelphoto/" + photoId), type: "DELETE", headers: adminAuthHeaders() });
    });
    const files = ADMIN_HOTEL_PHOTO_STATE.added.map(function (photo) { return photo.file; });
    const uploadRequest = uploadHotelPhotoFiles(hotelId, files);
    const requests = deleteRequests.concat(uploadRequest ? [uploadRequest] : []);
    return requests.length ? $.when.apply($, requests) : $.Deferred().resolve().promise();
}

function uploadHotelPhotoFiles(hotelId, files) {
    if (!files || !files.length) return null;
    const formData = new FormData();
    formData.append("hotelId", Number(hotelId));
    files.forEach(function (file) { formData.append("photos", file); });
    return $.ajax({
        url: window.StayNowConfig.apiUrl("/hotelphoto/upload"),
        type: "POST",
        headers: adminAuthHeaders(),
        data: formData,
        processData: false,
        contentType: false
    });
}

function getHotelTypeIdFromHotel(hotel, hotelTypes) {
    if (hotel.typeId) return hotel.typeId;
    const matched = hotelTypes.find(function (type) { return String(type.title) === String(hotel.typeTitle); });
    return matched ? matched.sid : (hotelTypes[0] && hotelTypes[0].sid);
}

function loadAdminDashboardData() {
    const requests = {
        stats: adminGetSafe("/reservation/status", {}),
        reservations: adminGetSafe("/reservation/search?page=0&size=500&sort=createdAt,desc", { content: [] }),
        payments: adminGetSafe("/payments", []),
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", [])
    };

    $.when(requests.stats, requests.reservations, requests.payments, requests.hotels, requests.popularHotels)
        .done(function (statsResult, reservationsResult, paymentsResult, hotelsResult, popularHotelsResult) {
            const stats = normalizeAjaxResult(statsResult);
            const reservationPage = normalizeAjaxResult(reservationsResult);
            const payments = asArray(normalizeAjaxResult(paymentsResult));
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);
            const reservations = filterReservationsByHotel(asPageContent(reservationPage), selectedHotel);
            const reservationKeys = getAdminReservationKeys(reservations);
            const selectedPayments = payments.filter(function (payment) {
                return getAdminPaymentReservationKeys(payment).some(function (key) {
                    return reservationKeys.has(key);
                });
            });

            renderAdminHotelSelector(hotels, selectedHotel);

            loadDashboardRoomsAndReviews({
                stats,
                reservations,
                payments: selectedPayments,
                hotels: selectedHotel ? [selectedHotel] : [],
                selectedHotel
            });
        })
        .fail(function () {
            renderDashboardFailure("대시보드 데이터를 불러오지 못했습니다. 백엔드 서버와 관리자 로그인 상태를 확인해주세요.");
        });
}

function loadAdminReservationData(page) {
    const currentPage = Number(page || 0);
    const requests = {
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", []),
        roomTypes: adminGetSafe("/roomtype", [])
    };

    $.when(requests.hotels, requests.popularHotels, requests.roomTypes)
        .done(function (hotelsResult, popularHotelsResult, roomTypesResult) {
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            ADMIN_ROOM_TYPES = asArray(normalizeAjaxResult(roomTypesResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);

            renderAdminHotelSelector(hotels, selectedHotel);
            renderReservationRoomTypeOptions();
            bindReservationFilters();
            requestAdminReservationMetrics(selectedHotel);
            requestAdminReservations(selectedHotel, currentPage);
        })
        .fail(function () {
            renderAdminHotelSelector([], null);
            $("#reservationTableWrap").html(emptyAdminState("예약 데이터를 불러오지 못했습니다."));
        });
}

function requestAdminReservations(selectedHotel, page) {
    const query = buildReservationQuery(selectedHotel, page || 0);

    adminGet("/reservation/search?" + query)
        .then(function (reservationPage) {
            const reservations = asPageContent(reservationPage);
            ADMIN_RESERVATION_STATE = {
                selectedHotel,
                page: reservationPage,
                reservations,
                filters: getReservationFilterValues()
            };
            renderReservationActiveFilters(ADMIN_RESERVATION_STATE.filters);
            renderReservationTable(reservationPage);
        }, function () {
            $("#reservationTableWrap").html(emptyAdminState("예약 데이터를 불러오지 못했습니다."));
        });
}

function requestAdminReservationMetrics(selectedHotel) {
    const params = new URLSearchParams();
    params.set("page", 0);
    params.set("size", 500);
    params.set("sort", "createdAt,desc");
    if (selectedHotel && selectedHotel.sid) params.set("hotelId", selectedHotel.sid);

    adminGet("/reservation/search?" + params.toString())
        .then(function (reservationPage) {
            renderReservationMetrics(asPageContent(reservationPage));
        }, function () {
            renderReservationMetrics([]);
        });
}

function buildReservationQuery(selectedHotel, page) {
    const filters = getReservationFilterValues();
    const params = new URLSearchParams();

    params.set("page", page);
    params.set("size", 10);
    params.set("sort", "createdAt,desc");
    if (selectedHotel && selectedHotel.sid) params.set("hotelId", selectedHotel.sid);
    if (filters.status) params.set("status", filters.status);
    if (filters.keyword) params.set("keyword", filters.keyword);
    if (filters.roomKeyword) params.set("roomKeyword", filters.roomKeyword);
    if (filters.roomTypeId) params.set("roomTypeId", filters.roomTypeId);
    if (filters.dateFrom) params.set("dateFrom", filters.dateFrom + "T00:00:00");
    if (filters.dateTo) params.set("dateTo", addOneDay(filters.dateTo) + "T00:00:00");
    return params.toString();
}

function bindReservationFilters() {
    $("#reservationKeyword, #reservationStatusFilter, #reservationDateFrom, #reservationDateTo, #reservationRoomTypeFilter")
        .off("change.adminReservation input.adminReservation")
        .on("change.adminReservation input.adminReservation", debounce(function () {
            requestAdminReservations(getSelectedAdminHotelFromMenu(), 0);
        }, 250));

    $("#reservationFilterReset").off("click.adminReservation").on("click.adminReservation", function () {
        $("#reservationKeyword").val("");
        $("#reservationStatusFilter").val("");
        $("#reservationDateFrom").val("");
        $("#reservationDateTo").val("");
        $("#reservationRoomTypeFilter").val("");
        requestAdminReservations(getSelectedAdminHotelFromMenu(), 0);
    });

    $("#reservationTableWrap")
        .off("click.adminReservation")
        .on("click.adminReservation", "[data-reservation-page]", function () {
            requestAdminReservations(getSelectedAdminHotelFromMenu(), Number($(this).data("reservationPage")));
        });
}

function getReservationFilterValues() {
    return {
        keyword: ($("#reservationKeyword").val() || "").trim(),
        status: $("#reservationStatusFilter").val() || "",
        dateFrom: $("#reservationDateFrom").val() || "",
        dateTo: $("#reservationDateTo").val() || "",
        roomTypeId: $("#reservationRoomTypeFilter").val() || "",
        roomKeyword: ""
    };
}

function renderReservationRoomTypeOptions() {
    const options = ['<option value="">전체 객실</option>'].concat(ADMIN_ROOM_TYPES.map(function (type) {
        return `<option value="${escapeHtml(type.sid)}">${escapeHtml(type.title || ("객실 타입 " + type.sid))}</option>`;
    }));
    $("#reservationRoomTypeFilter").html(options.join(""));
}

function loadAdminGuestData() {
    const requests = {
        members: adminGetSafe("/member", []),
        reservations: adminGetSafe("/reservation/search?page=0&size=500&sort=createdAt,desc", { content: [] }),
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", []),
        coupons: adminGetSafe("/cou", [])
    };

    $.when(requests.members, requests.reservations, requests.hotels, requests.popularHotels, requests.coupons)
        .done(function (membersResult, reservationsResult, hotelsResult, popularHotelsResult, couponsResult) {
            const members = asArray(normalizeAjaxResult(membersResult))
                .filter(function (member) {
                    return member && !member.deleted && member.status !== "DELETED";
                });
            const reservationPage = normalizeAjaxResult(reservationsResult);
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);
            const reservations = filterReservationsByHotel(asPageContent(reservationPage), selectedHotel);

            renderAdminHotelSelector(hotels, selectedHotel);
            ADMIN_GUEST_STATE = {
                members,
                reservations,
                coupons: asArray(normalizeAjaxResult(couponsResult)),
                selectedHotel,
                reservationsByMember: groupReservationsByMember(reservations)
            };
            bindGuestFilters();
            renderGuestMetrics();
            renderGuestTable();
        })
        .fail(function () {
            renderAdminHotelSelector([], null);
            $("#guestMetrics").html("");
            $("#guestTableWrap").html(emptyAdminState("고객 데이터를 불러오지 못했습니다."));
        });
}

function loadAdminRoomData() {
    const requests = {
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", []),
        reservations: adminGetSafe("/reservation/search?page=0&size=500&sort=createdAt,desc", { content: [] }),
        roomTypes: adminGetSafe("/roomtype", [])
    };

    $.when(requests.hotels, requests.popularHotels, requests.reservations, requests.roomTypes)
        .done(function (hotelsResult, popularHotelsResult, reservationsResult, roomTypesResult) {
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);
            const reservations = filterReservationsByHotel(asPageContent(normalizeAjaxResult(reservationsResult)), selectedHotel);
            const roomTypes = asArray(normalizeAjaxResult(roomTypesResult));

            renderAdminHotelSelector(hotels, selectedHotel);

            if (!selectedHotel || !selectedHotel.sid) {
                renderRoomFailure("관리할 호텔을 선택해주세요.");
                return;
            }

            adminGet("/hotel/inroom/" + selectedHotel.sid)
                .then(function (rooms) {
                    const roomStates = buildAdminRoomStates(asArray(rooms), reservations, selectedHotel);
                    ADMIN_ROOM_STATE = { selectedHotel, rooms: roomStates, reservations, roomTypes };
                    renderRoomFilters(roomStates, roomTypes);
                    bindRoomFilters();
                    renderRoomPage();
                }, function () {
                    renderRoomFailure("객실 데이터를 불러오지 못했습니다.");
                });
        })
        .fail(function () {
            renderAdminHotelSelector([], null);
            renderRoomFailure("객실 관리 데이터를 불러오지 못했습니다.");
        });
}

function buildAdminRoomStates(rooms, reservations, selectedHotel) {
    const now = new Date();
    const activeReservations = reservations.filter(function (reservation) {
        if (["CANCELLED", "NO_SHOW", "CHECKED_OUT", "COMPLETED"].includes(reservation.reservationStatus)) return false;
        if (reservation.reservationStatus === "CHECKED_IN") return true;
        const checkIn = parseAdminDate(reservation.checkInDate);
        const checkOut = parseAdminDate(reservation.checkOutDate);
        if (!checkIn || !checkOut) return false;
        return checkIn <= now && checkOut > now;
    });
    const futureReservations = reservations.filter(function (reservation) {
        if (["CANCELLED", "NO_SHOW", "CHECKED_OUT", "COMPLETED"].includes(reservation.reservationStatus)) return false;
        if (reservation.reservationStatus === "CHECKED_IN") return false;
        const checkIn = parseAdminDate(reservation.checkInDate);
        return checkIn && checkIn > now;
    });

    return rooms.map(function (room) {
        const current = activeReservations.find(function (reservation) {
            return String(reservation.roomId) === String(room.sid);
        });
        const next = futureReservations
            .filter(function (reservation) { return String(reservation.roomId) === String(room.sid); })
            .sort(function (a, b) { return new Date(a.checkInDate) - new Date(b.checkInDate); })[0];
        const available = room.roomAvailable !== false;
        const status = current ? "use" : next ? "done" : available ? "available" : "blocked";

        return Object.assign({}, room, {
            hotelId: selectedHotel.sid,
            adminStatus: status,
            adminStatusLabel: formatAdminRoomStatus(status),
            adminReservation: current || next || null,
            adminAvailable: available
        });
    }).sort(function (a, b) {
        return Number(a.roomNumber || 0) - Number(b.roomNumber || 0);
    });
}

function renderRoomFilters(rooms, roomTypes) {
    const typeOptions = ['<option value="">객실 유형 전체</option>'].concat(roomTypes.map(function (type) {
        return `<option value="${escapeHtml(type.sid)}">${escapeHtml(type.title || ("타입 " + type.sid))}</option>`;
    }));
    const floors = Array.from(new Set(rooms.map(function (room) {
        return room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || 1;
    }))).sort(function (a, b) { return Number(b) - Number(a); });
    const floorOptions = ['<option value="">층수 전체</option>'].concat(floors.map(function (floor) {
        return `<option value="${escapeHtml(floor)}">${escapeHtml(floor)}층</option>`;
    }));

    $("#roomTypeFilter").html(typeOptions.join(""));
    $("#roomFloorFilter").html(floorOptions.join(""));
}

function bindRoomFilters() {
    $("#roomKeywordFilter, #roomStatusFilter, #roomTypeFilter, #roomFloorFilter, #roomSortFilter")
        .off("input.adminRooms change.adminRooms")
        .on("input.adminRooms change.adminRooms", debounce(renderRoomPage, 180));

    $("#roomFilterReset").off("click.adminRooms").on("click.adminRooms", function () {
        $("#roomKeywordFilter").val("");
        $("#roomStatusFilter").val("");
        $("#roomTypeFilter").val("");
        $("#roomFloorFilter").val("");
        $("#roomSortFilter").val("numberAsc");
        renderRoomPage();
    });

    $(".room-view-switch").off("click.adminRooms").on("click.adminRooms", "[data-room-view]", function () {
        ADMIN_ROOM_VIEW = $(this).data("roomView") || "card";
        $(".room-view-switch [data-room-view]").removeClass("active");
        $(this).addClass("active");
        renderRoomPage();
    });

    $(document)
        .off("click.adminRoomAvailability")
        .on("click.adminRoomAvailability", "[data-room-availability]", function (event) {
            event.preventDefault();
            event.stopPropagation();
            const roomId = $(this).attr("data-room-availability");
            const available = String($(this).attr("data-available")) === "true";
            updateRoomAvailability(roomId, available, this);
        })
        .off("click.adminRoomEdit")
        .on("click.adminRoomEdit", "[data-room-edit]", function (event) {
            event.preventDefault();
            const roomId = $(this).attr("data-room-edit");
            const room = (ADMIN_ROOM_STATE ? ADMIN_ROOM_STATE.rooms : []).find(function (item) {
                return String(item.sid) === String(roomId);
            });
            if (room) openRoomModal(room);
        });
}

function getRoomFilterValues() {
    return {
        keyword: ($("#roomKeywordFilter").val() || "").trim().toLowerCase(),
        status: $("#roomStatusFilter").val() || "",
        roomTypeId: $("#roomTypeFilter").val() || "",
        floor: $("#roomFloorFilter").val() || "",
        sort: $("#roomSortFilter").val() || "numberAsc"
    };
}

function getFilteredAdminRooms() {
    const state = ADMIN_ROOM_STATE || { rooms: [] };
    const filters = getRoomFilterValues();

    const filtered = state.rooms.filter(function (room) {
        const floor = String(room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || 1);
        const text = [room.roomName, room.roomNumber, room.roomTypeTitle, room.adminStatusLabel].join(" ").toLowerCase();
        const matchesKeyword = !filters.keyword || text.includes(filters.keyword);
        const matchesStatus = !filters.status || room.adminStatus === filters.status;
        const matchesType = !filters.roomTypeId || String(room.roomTypeId || "") === String(filters.roomTypeId);
        const matchesFloor = !filters.floor || floor === String(filters.floor);
        return matchesKeyword && matchesStatus && matchesType && matchesFloor;
    });

    return filtered.sort(function (a, b) {
        if (filters.sort === "numberDesc") return Number(b.roomNumber || 0) - Number(a.roomNumber || 0);
        if (filters.sort === "priceDesc") return Number(b.roomPrice || 0) - Number(a.roomPrice || 0);
        if (filters.sort === "priceAsc") return Number(a.roomPrice || 0) - Number(b.roomPrice || 0);
        return Number(a.roomNumber || 0) - Number(b.roomNumber || 0);
    });
}

function renderRoomPage() {
    const rooms = getFilteredAdminRooms();
    const allRooms = ADMIN_ROOM_STATE ? ADMIN_ROOM_STATE.rooms : [];
    syncRoomViewButtons();
    renderRoomMetrics(allRooms);
    if (ADMIN_ROOM_VIEW === "list") {
        renderRoomListView(rooms);
    } else {
        renderRoomFloorPanel(rooms);
    }
    renderRoomTypeSummaryPanel(allRooms);
}

function syncRoomViewButtons() {
    $(".room-view-switch [data-room-view]").removeClass("active");
    $(`.room-view-switch [data-room-view="${ADMIN_ROOM_VIEW}"]`).addClass("active");
}

function renderRoomMetrics(rooms) {
    const counts = {
        total: rooms.length,
        use: rooms.filter(function (room) { return room.adminStatus === "use"; }).length,
        done: rooms.filter(function (room) { return room.adminStatus === "done"; }).length,
        available: rooms.filter(function (room) { return room.adminStatus === "available"; }).length,
        blocked: rooms.filter(function (room) { return room.adminStatus === "blocked"; }).length
    };
    const percent = function (value) {
        return counts.total ? Math.round((value / counts.total) * 100) + "%" : "0%";
    };
    const cards = [
        ["전체 객실", counts.total + "실", "fa-building", "전체", "blue"],
        ["사용중", counts.use + "실", "fa-users", percent(counts.use), "blue"],
        ["예약완료", counts.done + "실", "fa-calendar-check", percent(counts.done), "green"],
        ["공실", counts.available + "실", "fa-door-open", percent(counts.available), "orange"],
        ["예약 불가", counts.blocked + "실", "fa-ban", percent(counts.blocked), "red"]
    ];

    $("#roomMetrics").html(cards.map(function ([label, value, icon, badge, tone]) {
        return `<article class="room-stat-card ${tone}">
            <div class="room-stat-top">
                <span class="room-stat-icon"><i class="fa-solid ${icon}"></i></span>
                <span class="room-stat-badge">${escapeHtml(badge)}</span>
            </div>
            <div class="room-stat-value">${escapeHtml(value)}</div>
            <div class="room-stat-label">${escapeHtml(label)}</div>
        </article>`;
    }).join(""));
}

function renderRoomFloorPanel(rooms) {
    if (!rooms.length) {
        $("#roomFloorPanel").html(emptyAdminState("조건에 맞는 객실이 없습니다."));
        return;
    }

    const grouped = {};
    rooms.forEach(function (room) {
        const floor = room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || 1;
        if (!grouped[floor]) grouped[floor] = [];
        grouped[floor].push(room);
    });

    $("#roomFloorPanel").html(Object.keys(grouped).sort(function (a, b) { return Number(b) - Number(a); }).map(function (floor) {
        const floorRooms = grouped[floor];
        const counts = countRoomsByStatus(floorRooms);
        return `<section class="admin-room-floor-section">
            <header class="admin-room-floor-head">
                <div class="floor-title"><span><i class="fa-solid fa-layer-group"></i></span><strong>${escapeHtml(floor)}층</strong><em>${floorRooms.length}실</em></div>
                <div class="floor-legend">
                    <span class="use">사용 ${counts.use}</span>
                    <span class="done">예약 ${counts.done}</span>
                    <span class="available">공실 ${counts.available}</span>
                    <span class="blocked">예약 불가 ${counts.blocked}</span>
                </div>
            </header>
            <div class="admin-room-card-grid">${floorRooms.map(renderRoomCard).join("")}</div>
        </section>`;
    }).join(""));
}

function renderRoomListView(rooms) {
    if (!rooms.length) {
        $("#roomFloorPanel").html(emptyAdminState("조건에 맞는 객실이 없습니다."));
        return;
    }

    const rows = rooms.map(function (room) {
        const reservation = room.adminReservation;
        const canManage = room.adminStatus === "available" || room.adminStatus === "blocked";
        const price = Number(room.discountedRoomPrice || room.roomPrice || 0);
        const guest = reservation ? (reservation.memberName || reservation.guestName || "예약자") : "-";
        return `<tr>
            <td class="room-number-cell"><strong>${escapeHtml(room.roomNumber || "-")}호</strong><small>${escapeHtml((room.floor || "-") + "층")}</small></td>
            <td>${escapeHtml(room.roomName || "-")}</td>
            <td>${escapeHtml(room.roomTypeTitle || "-")}</td>
            <td><span class="status ${roomStatusTone(room.adminStatus)}">${escapeHtml(room.adminStatusLabel)}</span></td>
            <td>${escapeHtml(guest)}</td>
            <td>${price ? formatAdminWon(price) : "-"}</td>
            <td>${canManage ? `<div class="row-actions">
                <button class="icon-btn" type="button" title="${room.adminAvailable ? "예약 불가" : "예약 가능"}" data-room-availability="${escapeHtml(room.sid)}" data-available="${!room.adminAvailable}"><i class="fa-solid ${room.adminAvailable ? "fa-ban" : "fa-check"}"></i></button>
                <button class="icon-btn" type="button" title="수정" data-room-edit="${escapeHtml(room.sid)}"><i class="fa-solid fa-pen"></i></button>
            </div>` : `<span class="muted">변경 불가</span>`}</td>
        </tr>`;
    }).join("");

    $("#roomFloorPanel").html(`
        <div class="panel room-list-view-panel">
            ${panelHead("객실 리스트", "현재 필터와 정렬 기준")}
            <div class="admin-table-wrap">
                <table class="admin-table admin-room-table">
                    <thead><tr><th>객실번호</th><th>객실 이름</th><th>객실 타입</th><th>상태</th><th>예약자</th><th>가격</th><th>액션</th></tr></thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        </div>
    `);
}

function renderRoomCard(room) {
    const floor = room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || 1;
    const reservation = room.adminReservation;
    const canToggle = room.adminStatus === "available" || room.adminStatus === "blocked";
    const guestName = reservation ? (reservation.memberName || reservation.guestName || "예약자") : "";
    const dateText = reservation
        ? (room.adminStatus === "use" ? ("~ " + formatAdminShortDate(reservation.checkOutDate) + " 체크아웃") : (formatAdminShortDate(reservation.checkInDate) + " 체크인 예정"))
        : "";
    const price = Number(room.discountedRoomPrice || room.roomPrice || 0);
    const toggleText = room.adminAvailable ? "예약 불가" : "예약 가능";
    const actionIcon = room.adminAvailable ? "fa-ban" : "fa-check";
    const sizeText = room.roomSize || room.size ? escapeHtml(room.roomSize || room.size) + "㎡" : "-";

    const roomTitle = room.roomName || room.roomTypeTitle || "객실";

    return `<article class="admin-room-card ${escapeHtml(room.adminStatus)}">
        <div class="room-card-head">
            <div class="room-card-title-stack">
                <small>${escapeHtml(roomTitle)}</small>
                <span><span class="room-dot"></span><strong>${escapeHtml(room.roomNumber || "-")}호</strong></span>
            </div>
            <span class="room-status-chip">${escapeHtml(room.adminStatusLabel)}</span>
        </div>
        <div class="room-type-line"><i class="fa-solid fa-bed"></i>${escapeHtml(room.roomTypeTitle || room.roomName || "객실")}</div>
        ${reservation ? `<div class="room-guest-box">
            <span class="guest-avatar">${escapeHtml((guestName || "예").slice(0, 1))}</span>
            <div><strong>${escapeHtml(guestName)}</strong><small>${escapeHtml(dateText)}</small></div>
        </div>` : `<div class="room-empty-box"><i class="fa-regular fa-moon"></i><span>${room.adminStatus === "blocked" ? "예약 불가" : "공실"}</span></div>`}
        <div class="room-card-meta">
            <span><i class="fa-solid fa-arrow-up-right-dots"></i>${sizeText}</span>
            <span><i class="fa-solid fa-layer-group"></i>${escapeHtml(floor)}층</span>
            <strong><i class="fa-solid fa-tag"></i>${price ? formatAdminWon(price) : "-"}</strong>
        </div>
        <div class="room-card-actions">
            ${canToggle ? `<button type="button" data-room-availability="${escapeHtml(room.sid)}" data-available="${!room.adminAvailable}">
                <i class="fa-solid ${actionIcon}"></i>${escapeHtml(toggleText)}
            </button><button type="button" class="soft" data-room-edit="${escapeHtml(room.sid)}">
                <i class="fa-solid fa-pen"></i>수정
            </button>` : `<span class="room-action-lock">예약 진행 중인 객실은 변경할 수 없습니다</span>`}
        </div>
    </article>`;
}

function renderRoomTypeSummaryPanel(rooms) {
    const grouped = {};
    rooms.forEach(function (room) {
        const key = room.roomTypeTitle || room.roomName || "기타 객실";
        if (!grouped[key]) grouped[key] = { total: 0, available: 0, sum: 0, count: 0, revenue: 0 };
        grouped[key].total += 1;
        if (room.adminStatus === "available") grouped[key].available += 1;
        if (Number(room.roomPrice || 0) > 0) {
            grouped[key].sum += Number(room.roomPrice);
            grouped[key].count += 1;
        }
        if (room.adminStatus === "use" || room.adminStatus === "done") grouped[key].revenue += Number(room.discountedRoomPrice || room.roomPrice || 0);
    });
    const rows = Object.keys(grouped).map(function (name) {
        const item = grouped[name];
        const occupancy = item.total ? Math.round(((item.total - item.available) / item.total) * 100) : 0;
        const avg = item.count ? Math.round(item.sum / item.count) : 0;
        return `<tr>
            <td><span class="room-type-icon"><i class="fa-solid fa-star"></i></span><strong>${escapeHtml(name)}</strong></td>
            <td>${item.total}실</td>
            <td><span class="mini-progress"><i style="width:${occupancy}%"></i></span> ${occupancy}%</td>
            <td>${avg ? formatAdminWon(avg) : "-"}</td>
            <td>${item.revenue ? formatAdminCompactWon(item.revenue) : "-"}</td>
        </tr>`;
    }).join("");
    $("#roomTypeSummaryPanel").html(panelHead("객실 유형별 현황", "선택 호텔 객실 기준") + `
        <div class="admin-table-wrap compact">
            <table class="admin-table room-type-table">
                <thead><tr><th>객실 유형</th><th>총 객실</th><th>점유율</th><th>평균 단가</th><th>예상 매출</th></tr></thead>
                <tbody>${rows || `<tr><td colspan="5">${emptyAdminState("객실 데이터가 없습니다.")}</td></tr>`}</tbody>
            </table>
        </div>
    `);
}

function countRoomsByStatus(rooms) {
    return {
        use: rooms.filter(function (room) { return room.adminStatus === "use"; }).length,
        done: rooms.filter(function (room) { return room.adminStatus === "done"; }).length,
        available: rooms.filter(function (room) { return room.adminStatus === "available"; }).length,
        blocked: rooms.filter(function (room) { return room.adminStatus === "blocked"; }).length
    };
}

function openRoomModal(room) {
    const isEdit = Boolean(room);
    const state = ADMIN_ROOM_STATE || {};
    const selectedHotel = state.selectedHotel || {};
    const roomTypes = state.roomTypes || [];
    if (!selectedHotel.sid) {
        alert("호텔을 먼저 선택해주세요.");
        return;
    }

    const typeOptions = roomTypes.map(function (type) {
        const selected = String(type.sid) === String(room && room.roomTypeId) ? " selected" : "";
        return `<option value="${escapeHtml(type.sid)}"${selected}>${escapeHtml(type.title || ("타입 " + type.sid))}</option>`;
    }).join("");

    $("#roomModalRoot").html(`
        <div class="admin-modal-backdrop">
            <form id="roomForm" class="admin-modal room-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>${isEdit ? "객실 수정" : "객실 추가"}</h2>
                        <p>${escapeHtml(selectedHotel.hotelName || "선택 호텔")}의 객실 정보를 관리합니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-room-modal-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="admin-form-grid room-form-grid">
                    <label class="room-type-field admin-form-full"><span>객실 타입</span><div class="room-type-select-row"><select id="roomTypeInput" required>${typeOptions}</select><button type="button" class="admin-btn" data-room-type-manage><i class="fa-solid fa-gear"></i> 타입 관리</button></div></label>
                    <label><span>객실 이름</span><input id="roomNameInput" type="text" maxlength="30" value="${escapeHtml(room ? room.roomName || "" : "")}" required></label>
                    <label><span>객실 가격</span><input id="roomPriceInput" type="number" min="0" step="1000" value="${escapeHtml(room ? room.roomPrice || "" : "")}" required></label>
                    <label><span>예약 가능 여부</span><select id="roomAvailableInput"><option value="true"${!room || room.adminAvailable !== false ? " selected" : ""}>예약 가능</option><option value="false"${room && room.adminAvailable === false ? " selected" : ""}>예약 불가</option></select></label>
                    <label><span>객실 번호</span><input id="roomNumberInput" type="number" min="1" value="${escapeHtml(room ? room.roomNumber || "" : "")}" required></label>
                    <label><span>층</span><input id="roomFloorInput" type="number" min="1" value="${escapeHtml(room ? room.floor || "" : "")}" required></label>
                    <label><span>넓이(m²)</span><input id="roomAreaInput" type="number" min="1" value="${escapeHtml(room ? room.area || "" : "")}" required></label>
                    <label><span>최대 인원 수</span><input id="roomMaximumPeopleInput" type="number" min="1" value="${escapeHtml(room ? room.maximumPeople || "" : "")}" required></label>
                    <label><span>체크인 시간</span><input id="roomCheckInInput" type="number" min="0" max="23" value="${escapeHtml(room && room.checkInTime != null ? room.checkInTime : 15)}"></label>
                    <label><span>체크아웃 시간</span><input id="roomCheckOutInput" type="number" min="0" max="23" value="${escapeHtml(room && room.checkOutTime != null ? room.checkOutTime : 11)}"></label>
                    <label><span>주차 가능 여부</span><select id="roomParkingInput">${roomOption("AVAILABLE", "주차 가능", room && room.parking)}${roomOption("UNAVAILABLE", "주차 불가", room && room.parking)}</select></label>
                    <label><span>반려동물 동반</span><select id="roomPetInput">${petSmokeOptions(room && room.pet)}</select></label>
                    <label><span>흡연 가능 여부</span><select id="roomSmokeInput">${petSmokeOptions(room && room.smoke)}</select></label>
                    <label><span>신분증 검사</span><select id="roomIdCardInput">${roomOption("ESSENTIAL", "필수", room && room.idCard)}${roomOption("OPTIONAL", "선택", room && room.idCard)}</select></label>
                    <div class="admin-form-full room-photo-field">
                        <div class="room-photo-title">
                            <span>객실 사진</span>
                            <small>사진 오른쪽 위의 - 버튼으로 제거하고, + 타일로 한 장 또는 여러 장을 추가할 수 있습니다.</small>
                        </div>
                        <div id="roomPhotoGrid" class="room-photo-grid"></div>
                        <input id="roomPhotoFilesInput" class="room-photo-native-input" type="file" accept="image/*" multiple>
                    </div>
                </div>
                <div class="admin-modal-actions">
                    <button type="button" class="admin-btn" data-room-modal-close>취소</button>
                    <button type="submit" class="admin-btn primary">${isEdit ? "저장" : "추가"}</button>
                </div>
            </form>
        </div>
    `);

    $("[data-room-modal-close]").on("click", closeRoomModal);
    $("[data-room-type-manage]").on("click", openRoomTypeManager);
    initRoomPhotoManager(room);
    $("#roomForm").on("submit", function (event) {
        event.preventDefault();
        saveRoom(room);
    });
}

function closeRoomModal() {
    clearRoomPhotoObjectUrls();
    $("#roomModalRoot").empty();
}

function initRoomPhotoManager(room) {
    clearRoomPhotoObjectUrls();
    ADMIN_ROOM_PHOTO_STATE = { existing: [], added: [], deleted: [] };
    bindRoomPhotoManager();
    renderRoomPhotoGrid();
    if (room && room.sid) {
        adminGetSafe("/room/inimage/" + room.sid, []).then(function (photos) {
            ADMIN_ROOM_PHOTO_STATE.existing = asArray(photos).map(function (photo) {
                return {
                    sid: photo.sid,
                    imagePath: photo.imagePath
                };
            });
            renderRoomPhotoGrid();
        });
    }
}

function bindRoomPhotoManager() {
    $("#roomPhotoGrid")
        .off("click.roomPhotos")
        .on("click.roomPhotos", "[data-room-photo-add]", function () {
            $("#roomPhotoFilesInput").trigger("click");
        })
        .on("click.roomPhotos", "[data-room-photo-remove]", function () {
            removeRoomPhoto($(this).data("roomPhotoRemove"));
        });
    $("#roomPhotoFilesInput")
        .off("change.roomPhotos")
        .on("change.roomPhotos", function () {
            addRoomPhotoFiles(this.files);
            this.value = "";
        });
}

function addRoomPhotoFiles(fileList) {
    const files = Array.from(fileList || []).filter(function (file) {
        return file && file.type && file.type.startsWith("image/");
    });
    if (!files.length) return;
    files.forEach(function (file) {
        ADMIN_ROOM_PHOTO_STATE.added.push({
            id: "new-" + Date.now() + "-" + Math.random().toString(16).slice(2),
            file,
            previewUrl: URL.createObjectURL(file)
        });
    });
    renderRoomPhotoGrid();
}

function removeRoomPhoto(id) {
    const key = String(id);
    const existingIndex = ADMIN_ROOM_PHOTO_STATE.existing.findIndex(function (photo) {
        return String(photo.sid) === key;
    });
    if (existingIndex >= 0) {
        const removed = ADMIN_ROOM_PHOTO_STATE.existing.splice(existingIndex, 1)[0];
        if (removed && removed.sid) ADMIN_ROOM_PHOTO_STATE.deleted.push(removed.sid);
        renderRoomPhotoGrid();
        return;
    }
    const addedIndex = ADMIN_ROOM_PHOTO_STATE.added.findIndex(function (photo) {
        return String(photo.id) === key;
    });
    if (addedIndex >= 0) {
        const removed = ADMIN_ROOM_PHOTO_STATE.added.splice(addedIndex, 1)[0];
        if (removed && removed.previewUrl) URL.revokeObjectURL(removed.previewUrl);
        renderRoomPhotoGrid();
    }
}

function renderRoomPhotoGrid() {
    const grid = $("#roomPhotoGrid");
    if (!grid.length) return;
    const existingTiles = ADMIN_ROOM_PHOTO_STATE.existing.map(function (photo) {
        return `<div class="room-photo-tile">
            <img src="${escapeHtml(resolveAdminImagePath(photo.imagePath))}" alt="객실 사진">
            <button class="room-photo-remove" type="button" data-room-photo-remove="${escapeHtml(photo.sid)}" aria-label="사진 제거"><i class="fa-solid fa-minus"></i></button>
        </div>`;
    });
    const addedTiles = ADMIN_ROOM_PHOTO_STATE.added.map(function (photo) {
        return `<div class="room-photo-tile new">
            <img src="${escapeHtml(photo.previewUrl)}" alt="추가할 객실 사진">
            <button class="room-photo-remove" type="button" data-room-photo-remove="${escapeHtml(photo.id)}" aria-label="사진 제거"><i class="fa-solid fa-minus"></i></button>
        </div>`;
    });
    const addTile = `<button class="room-photo-add-tile" type="button" data-room-photo-add>
        <i class="fa-solid fa-plus"></i>
        <span>사진 추가</span>
    </button>`;
    grid.html(existingTiles.concat(addedTiles).join("") + addTile);
}

function clearRoomPhotoObjectUrls() {
    if (!ADMIN_ROOM_PHOTO_STATE || !ADMIN_ROOM_PHOTO_STATE.added) return;
    ADMIN_ROOM_PHOTO_STATE.added.forEach(function (photo) {
        if (photo.previewUrl) URL.revokeObjectURL(photo.previewUrl);
    });
}

function resolveAdminImagePath(path) {
    if (!path) return "";
    if (/^(https?:)?\/\//.test(path) || path.startsWith("data:") || path.startsWith("blob:")) return path;
    if (path.startsWith("/")) return window.StayNowConfig.assetUrl(path);
    return path;
}

function openRoomTypeManager() {
    closeRoomTypeManager();
    renderRoomTypeManager();
}

function renderRoomTypeManager() {
    const roomTypes = ADMIN_ROOM_STATE ? ADMIN_ROOM_STATE.roomTypes || [] : [];
    const rows = roomTypes.map(function (type) {
        return `<tr>
            <td><strong>${escapeHtml(type.title || "-")}</strong><small>ID ${escapeHtml(type.sid)}</small></td>
            <td>
                <div class="row-actions">
                    <button class="icon-btn" type="button" title="수정" data-room-type-edit="${escapeHtml(type.sid)}"><i class="fa-solid fa-pen"></i></button>
                    <button class="icon-btn danger" type="button" title="삭제" data-room-type-delete="${escapeHtml(type.sid)}"><i class="fa-solid fa-trash"></i></button>
                </div>
            </td>
        </tr>`;
    }).join("");

    $("#roomModalRoot").append(`
        <div class="admin-modal-backdrop nested room-type-manager-backdrop">
            <div class="admin-modal room-type-manager-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>객실 타입 관리</h2>
                        <p>객실 생성과 수정에서 사용할 타입을 관리합니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-room-type-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <form id="roomTypeForm" class="room-type-create-row">
                    <input id="roomTypeTitleInput" type="text" maxlength="40" placeholder="새 객실 타입 이름" required>
                    <input id="roomTypeSidInput" type="hidden">
                    <button type="submit" class="admin-btn primary"><i class="fa-solid fa-plus"></i> 저장</button>
                    <button type="button" class="admin-btn" data-room-type-reset>초기화</button>
                </form>
                <div class="admin-table-wrap room-type-manager-table">
                    <table class="admin-table">
                        <thead><tr><th>객실 타입</th><th>액션</th></tr></thead>
                        <tbody>${rows || `<tr><td colspan="2">${emptyAdminState("등록된 객실 타입이 없습니다.")}</td></tr>`}</tbody>
                    </table>
                </div>
            </div>
        </div>
    `);

    $("[data-room-type-close]").off("click.roomType").on("click.roomType", closeRoomTypeManager);
    $("[data-room-type-reset]").off("click.roomType").on("click.roomType", resetRoomTypeForm);
    $("#roomTypeForm").off("submit.roomType").on("submit.roomType", function (event) {
        event.preventDefault();
        saveRoomType();
    });
    $(".room-type-manager-table")
        .off("click.roomType")
        .on("click.roomType", "[data-room-type-edit]", function () {
            const type = findRoomType($(this).attr("data-room-type-edit"));
            if (!type) return;
            $("#roomTypeSidInput").val(type.sid);
            $("#roomTypeTitleInput").val(type.title || "").trigger("focus");
            $("#roomTypeForm .primary").html('<i class="fa-solid fa-check"></i> 저장');
        })
        .on("click.roomType", "[data-room-type-delete]", function () {
            deleteRoomType($(this).attr("data-room-type-delete"));
        });
}

function closeRoomTypeManager() {
    $(".room-type-manager-backdrop").remove();
}

function resetRoomTypeForm() {
    $("#roomTypeSidInput").val("");
    $("#roomTypeTitleInput").val("").trigger("focus");
    $("#roomTypeForm .primary").html('<i class="fa-solid fa-plus"></i> 저장');
}

function findRoomType(id) {
    return (ADMIN_ROOM_STATE ? ADMIN_ROOM_STATE.roomTypes || [] : []).find(function (type) {
        return String(type.sid) === String(id);
    });
}

function saveRoomType() {
    const sid = $("#roomTypeSidInput").val();
    const title = $("#roomTypeTitleInput").val().trim();
    if (!title) {
        alert("객실 타입 이름을 입력해주세요.");
        return;
    }
    const payload = sid ? { sid: Number(sid), title } : { title };
    const request = sid ? adminPatch("/roomtype", payload) : adminPost("/roomtype", payload);
    request.then(function () {
        reloadRoomTypesForRoomModal(function () {
            closeRoomTypeManager();
            renderRoomTypeManager();
        });
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "객실 타입 저장에 실패했습니다."));
    });
}

function deleteRoomType(id) {
    if (!id || !confirm("이 객실 타입을 삭제할까요? 연결된 객실이나 프로모션이 있으면 삭제되지 않습니다.")) return;
    $.ajax({
        url: window.StayNowConfig.apiUrl("/roomtype/" + id),
        type: "DELETE",
        headers: adminAuthHeaders()
    }).done(function () {
        reloadRoomTypesForRoomModal(function () {
            closeRoomTypeManager();
            renderRoomTypeManager();
        });
    }).fail(function (xhr) {
        alert(getAdminAjaxMessage(xhr, "객실 타입 삭제에 실패했습니다. 연결된 객실 또는 프로모션이 있으면 삭제할 수 없습니다."));
    });
}

function reloadRoomTypesForRoomModal(callback) {
    adminGet("/roomtype").then(function (roomTypes) {
        const list = asArray(roomTypes);
        if (ADMIN_ROOM_STATE) {
            ADMIN_ROOM_STATE.roomTypes = list;
        }
        renderRoomFilters(ADMIN_ROOM_STATE ? ADMIN_ROOM_STATE.rooms : [], list);
        updateRoomTypeSelectOptions(list);
        if (typeof callback === "function") callback();
    }, function (xhr) {
        alert(getAdminAjaxMessage(xhr, "객실 타입을 다시 불러오지 못했습니다."));
    });
}

function updateRoomTypeSelectOptions(roomTypes) {
    const current = $("#roomTypeInput").val();
    const options = roomTypes.map(function (type) {
        const selected = String(type.sid) === String(current) ? " selected" : "";
        return `<option value="${escapeHtml(type.sid)}"${selected}>${escapeHtml(type.title || ("타입 " + type.sid))}</option>`;
    }).join("");
    $("#roomTypeInput").html(options);
    if (current && !roomTypes.some(function (type) { return String(type.sid) === String(current); })) {
        $("#roomTypeInput").prop("selectedIndex", 0);
    }
}

function roomOption(value, label, current) {
    return `<option value="${value}"${String(current || "").toUpperCase() === value ? " selected" : ""}>${label}</option>`;
}

function petSmokeOptions(current) {
    return [
        roomOption("POSSIBLE", "가능", current),
        roomOption("LIMITED", "제한", current),
        roomOption("BAN", "불가", current || "BAN")
    ].join("");
}

function readRoomPayload(original) {
    const state = ADMIN_ROOM_STATE || {};
    const hotel = state.selectedHotel || {};
    return {
        sid: original ? original.sid : undefined,
        hotelId: hotel.sid,
        roomTypeId: Number($("#roomTypeInput").val()),
        roomName: $("#roomNameInput").val().trim(),
        roomPrice: Number($("#roomPriceInput").val()),
        roomAvailable: $("#roomAvailableInput").val() === "true",
        roomNumber: Number($("#roomNumberInput").val()),
        floor: Number($("#roomFloorInput").val()),
        area: Number($("#roomAreaInput").val()),
        maximumPeople: Number($("#roomMaximumPeopleInput").val()),
        checkInTime: Number($("#roomCheckInInput").val()),
        checkOutTime: Number($("#roomCheckOutInput").val()),
        parking: $("#roomParkingInput").val(),
        pet: $("#roomPetInput").val(),
        smoke: $("#roomSmokeInput").val(),
        idCard: $("#roomIdCardInput").val()
    };
}

function saveRoom(original) {
    const payload = readRoomPayload(original);
    if (!payload.hotelId || !payload.roomTypeId || !payload.roomName || !payload.roomPrice || !payload.roomNumber || !payload.floor || !payload.area || !payload.maximumPeople) {
        alert("필수 객실 정보를 모두 입력해주세요.");
        return;
    }

    const request = original ? adminPatch("/room", payload) : adminPost("/room", payload);
    request.then(function (savedRoom) {
        saveRoomPhotosAfterRoomSave(savedRoom && savedRoom.sid ? savedRoom.sid : payload.sid)
            .always(function () {
                closeRoomModal();
                loadAdminRoomData();
            });
    }, function (xhr) {
        console.error("Room save failed", { payload, response: xhr && (xhr.responseJSON || xhr.responseText) });
        alert(getAdminAjaxMessage(xhr, "객실 저장에 실패했습니다."));
    });
}

function saveRoomPhotosAfterRoomSave(roomId) {
    if (!roomId) {
        return $.Deferred().resolve().promise();
    }
    const deleteRequests = ADMIN_ROOM_PHOTO_STATE.deleted.map(function (photoId) {
        return $.ajax({
            url: window.StayNowConfig.apiUrl("/roomphoto/" + photoId),
            type: "DELETE",
            headers: adminAuthHeaders()
        });
    });
    const uploadRequest = uploadRoomPhotoFiles(roomId, ADMIN_ROOM_PHOTO_STATE.added.map(function (photo) {
        return photo.file;
    }));
    const requests = deleteRequests.concat(uploadRequest ? [uploadRequest] : []);
    if (!requests.length) {
        return $.Deferred().resolve().promise();
    }
    return $.when.apply($, requests);
}

function uploadRoomPhotoFiles(roomId, files) {
    if (!files || !files.length) {
        return null;
    }
    const formData = new FormData();
    formData.append("roomId", Number(roomId));
    files.forEach(function (file) {
        formData.append("photos", file);
    });
    return $.ajax({
        url: window.StayNowConfig.apiUrl("/roomphoto/upload"),
        type: "POST",
        headers: adminAuthHeaders(),
        data: formData,
        processData: false,
        contentType: false
    });
}

function updateRoomAvailability(roomId, available, button) {
    const $button = $(button);
    const originalHtml = $button.html();
    const loadingHtml = $button.hasClass("icon-btn")
        ? '<i class="fa-solid fa-spinner fa-spin"></i>'
        : '<i class="fa-solid fa-spinner fa-spin"></i>처리 중';
    $button.prop("disabled", true).html(loadingHtml);
    adminPatch("/room/" + roomId + "/availability", { roomAvailable: available })
        .then(function () {
            loadAdminRoomData();
        }, function (xhr) {
            $button.prop("disabled", false).html(originalHtml);
            console.error("Room availability update failed", { roomId, available, response: xhr && (xhr.responseJSON || xhr.responseText) });
            alert(getAdminAjaxMessage(xhr, "객실 상태 변경에 실패했습니다."));
        });
}

function renderRoomFailure(message) {
    $("#roomMetrics").html("");
    $("#roomFloorPanel").html(emptyAdminState(message));
    $("#roomTypeSummaryPanel").html(emptyAdminState(message));
}

function formatAdminRoomStatus(status) {
    return {
        use: "사용중",
        done: "예약완료",
        available: "공실",
        blocked: "예약 불가"
    }[status] || "공실";
}

function roomStatusTone(status) {
    if (status === "use") return "blue";
    if (status === "done") return "";
    if (status === "blocked") return "red";
    return "green";
}

function loadAdminSalesData() {
    const monthValue = $("#salesMonthPicker").val() || getCurrentMonthValue();
    const requests = {
        reservations: adminGetSafe("/reservation/search?page=0&size=500&sort=createdAt,desc", { content: [] }),
        payments: adminGetSafe("/payments", []),
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", [])
    };

    $.when(requests.reservations, requests.payments, requests.hotels, requests.popularHotels)
        .done(function (reservationsResult, paymentsResult, hotelsResult, popularHotelsResult) {
            const reservationPage = normalizeAjaxResult(reservationsResult);
            const payments = asArray(normalizeAjaxResult(paymentsResult));
            const hotelPage = normalizeAjaxResult(hotelsResult);
            const popularHotels = asArray(normalizeAjaxResult(popularHotelsResult));
            const hotels = mergeAdminHotels(asPageContent(hotelPage), popularHotels);
            const selectedHotel = getSelectedAdminHotel(hotels);
            const reservations = filterReservationsByHotel(asPageContent(reservationPage), selectedHotel);
            const reservationKeys = getAdminReservationKeys(reservations);
            const selectedPayments = payments.filter(function (payment) {
                return getAdminPaymentReservationKeys(payment).some(function (key) {
                    return reservationKeys.has(String(key));
                });
            });

            renderAdminHotelSelector(hotels, selectedHotel);
            renderAdminSalesPage({
                selectedHotel,
                reservations,
                payments: selectedPayments,
                monthValue
            });
            bindSalesMonthPicker();
        })
        .fail(function () {
            renderAdminSalesFailure("매출 데이터를 불러오지 못했습니다.");
        });
}

function bindSalesMonthPicker() {
    $("#salesMonthPicker").off("change.adminSales").on("change.adminSales", function () {
        loadAdminSalesData();
    });

    $(".admin-month-picker").off("click.adminSalesMonth").on("click.adminSalesMonth", function (event) {
        if (event.target && event.target.id === "salesMonthPicker") return;
        const input = document.getElementById("salesMonthPicker");
        if (!input) return;
        if (typeof input.showPicker === "function") {
            input.showPicker();
        } else {
            input.focus();
            input.click();
        }
    });
}

function renderAdminSalesFailure(message) {
    $("#salesMetrics").html("");
    $("#salesMonthlyPanel, #salesRoomTypePanel, #salesTopReservationPanel, #salesDailyTablePanel").html(emptyAdminState(message));
}

function renderAdminSalesPage(data) {
    const summary = buildAdminSalesSummary(data.reservations, data.payments, data.monthValue);
    ADMIN_SALES_STATE = Object.assign({}, data, { summary });
    renderSalesMetrics(summary);
    renderSalesMonthlyTrend(summary);
    renderSalesRoomTypes(summary);
    renderSalesTopReservations(summary);
    renderSalesDailyTable(summary);
}

function buildAdminSalesSummary(reservations, payments, monthValue) {
    const target = parseMonthValue(monthValue);
    const validPayments = payments.filter(isRevenuePayment);
    const monthPayments = validPayments.filter(function (payment) {
        return isSameAdminMonth(getPaymentRevenueDate(payment), target.year, target.month);
    });
    const monthReservations = reservations.filter(function (reservation) {
        return isSameAdminMonth(reservation.createdAt || reservation.checkInDate, target.year, target.month);
    });
    const validReservations = monthReservations.filter(function (reservation) {
        return !["CANCELLED", "NO_SHOW"].includes(reservation.reservationStatus);
    });
    const paymentReservationMap = buildReservationMap(reservations);
    const totalRevenue = monthPayments.reduce(function (sum, payment) {
        return sum + getPaymentRevenueAmount(payment);
    }, 0);
    const cancelledAmount = monthReservations
        .filter(function (reservation) { return ["CANCELLED", "NO_SHOW"].includes(reservation.reservationStatus); })
        .reduce(function (sum, reservation) { return sum + Number(reservation.totalAmount || 0); }, 0);
    const monthlyTrend = buildSalesMonthlyTrend(validPayments, target);
    const roomTypes = buildSalesRoomTypeSummary(monthPayments, paymentReservationMap);
    const topReservations = buildSalesTopReservations(monthPayments, paymentReservationMap);
    const dailyRows = buildSalesDailyRows(monthPayments, paymentReservationMap, target);

    return {
        target,
        monthLabel: target.year + "년 " + (target.month + 1) + "월",
        totalRevenue,
        paymentCount: monthPayments.length,
        validReservationCount: validReservations.length,
        cancelledAmount,
        netRevenue: totalRevenue,
        monthlyTrend,
        roomTypes,
        topReservations,
        dailyRows
    };
}

function renderSalesMetrics(summary) {
    const cards = [
        ["선택 월 총 매출", formatAdminWon(summary.totalRevenue), "fa-money-bill", summary.monthLabel, ""],
        ["결제 완료 건수", formatAdminNumber(summary.paymentCount) + "건", "fa-receipt", "환불 반영 결제", ""],
        ["취소 제외 예약", formatAdminNumber(summary.validReservationCount) + "건", "fa-calendar-check", "취소/노쇼 제외", ""],
        ["순매출", formatAdminWon(summary.netRevenue), "fa-chart-line", "결제 기준", ""]
    ];

    $("#salesMetrics").html(cards.map(function ([label, value, icon, trend, tone]) {
        return `<article class="metric-card">
            <div class="metric-top"><span class="metric-icon ${tone || ""}"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${escapeHtml(trend)}</span></div>
            <div class="metric-label">${escapeHtml(label)}</div>
            <div class="metric-value">${escapeHtml(value)}</div>
        </article>`;
    }).join(""));
}

function renderSalesMonthlyTrend(summary) {
    const max = Math.max.apply(null, summary.monthlyTrend.map(function (item) { return item.total; }).concat([1]));
    const bars = summary.monthlyTrend.map(function (item) {
        const height = item.total > 0 ? Math.max(8, Math.round((item.total / max) * 100)) : 0;
        const active = item.year === summary.target.year && item.month === summary.target.month;
        return `<div class="admin-sales-month ${active ? "active" : ""}">
            <div class="admin-sales-bar" style="height:${height}%"></div>
            <strong>${escapeHtml(item.label)}</strong>
            <span>${formatAdminCompactWon(item.total)}</span>
        </div>`;
    }).join("");

    $("#salesMonthlyPanel").html(
        panelHead("월별 매출 추이", "선택 월 포함 최근 6개월 · 결제 완료 및 부분 환불 반영") +
        `<div class="admin-sales-chart">${bars}</div>`
    );
}

function renderSalesRoomTypes(summary) {
    if (!summary.roomTypes.length) {
        $("#salesRoomTypePanel").html(panelHead("객실 유형별 매출", summary.monthLabel) + emptyAdminState("객실 유형별 매출이 없습니다."));
        return;
    }

    $("#salesRoomTypePanel").html(
        panelHead("객실 유형별 매출", "총 " + formatAdminWon(summary.totalRevenue) + " · " + summary.monthLabel) +
        barLines(summary.roomTypes.map(function (item) {
            return [item.label, item.percent, formatAdminWon(item.total)];
        }))
    );
}

function renderSalesTopReservations(summary) {
    if (!summary.topReservations.length) {
        $("#salesTopReservationPanel").html(panelHead("매출 상위 예약", summary.monthLabel) + emptyAdminState("매출 예약이 없습니다."));
        return;
    }

    const rows = summary.topReservations.map(function (item, index) {
        return `<div class="sales-top-item">
            <span>${index + 1}</span>
            <div>
                <strong>${escapeHtml(item.guestName)} · ${escapeHtml(item.roomName)}</strong>
                <small>${escapeHtml(item.reservationNumber)} · ${escapeHtml(formatAdminShortDate(item.checkInDate))} ~ ${escapeHtml(formatAdminShortDate(item.checkOutDate))}</small>
            </div>
            <b>${formatAdminWon(item.amount)}</b>
        </div>`;
    }).join("");

    $("#salesTopReservationPanel").html(
        panelHead("매출 상위 예약", summary.monthLabel + " 최고 매출 예약") +
        `<div class="sales-top-list">${rows}</div>`
    );
}

function renderSalesDailyTable(summary) {
    const rows = summary.dailyRows.map(function (row) {
        return [
            row.dateLabel,
            row.paymentCount + "건",
            row.reservationCount + "건",
            formatAdminWon(row.total),
            formatAdminWon(row.cancelledAmount),
            formatAdminWon(row.netTotal)
        ];
    });

    $("#salesDailyTablePanel").html(
        panelHead("일별 매출 표", summary.monthLabel + " · 결제일 기준") +
        (rows.length
            ? simpleTable(["날짜", "결제 건수", "예약 건수", "총 매출", "취소 예약액", "순매출"], rows)
            : emptyAdminState("선택 월의 매출 데이터가 없습니다."))
    );
}

function buildReservationMap(reservations) {
    const map = new Map();
    reservations.forEach(function (reservation) {
        getAdminReservationKeys([reservation]).forEach(function (key) {
            map.set(String(key), reservation);
        });
    });
    return map;
}

function getPaymentReservation(payment, reservationMap) {
    const keys = getAdminPaymentReservationKeys(payment);
    for (let i = 0; i < keys.length; i++) {
        if (reservationMap.has(String(keys[i]))) {
            return reservationMap.get(String(keys[i]));
        }
    }
    return null;
}

function buildSalesMonthlyTrend(payments, target) {
    const months = [];
    for (let i = 5; i >= 0; i--) {
        const date = new Date(target.year, target.month - i, 1);
        months.push({
            year: date.getFullYear(),
            month: date.getMonth(),
            label: String(date.getMonth() + 1) + "월",
            total: 0
        });
    }

    payments.forEach(function (payment) {
        const date = parseAdminDate(getPaymentRevenueDate(payment));
        if (!date) return;
        const found = months.find(function (item) {
            return item.year === date.getFullYear() && item.month === date.getMonth();
        });
        if (found) found.total += getPaymentRevenueAmount(payment);
    });

    return months;
}

function buildSalesRoomTypeSummary(payments, reservationMap) {
    const groups = new Map();
    payments.forEach(function (payment) {
        const reservation = getPaymentReservation(payment, reservationMap);
        const label = reservation ? (reservation.roomTypeTitle || reservation.roomName || "객실") : "객실 정보 없음";
        const current = groups.get(label) || { label, total: 0, count: 0 };
        current.total += getPaymentRevenueAmount(payment);
        current.count += 1;
        groups.set(label, current);
    });

    const total = Array.from(groups.values()).reduce(function (sum, item) { return sum + item.total; }, 0);
    return Array.from(groups.values())
        .map(function (item) {
            return Object.assign(item, {
                percent: total > 0 ? Math.round((item.total / total) * 100) : 0
            });
        })
        .sort(function (a, b) { return b.total - a.total; })
        .slice(0, 6);
}

function buildSalesTopReservations(payments, reservationMap) {
    return payments.map(function (payment) {
        const reservation = getPaymentReservation(payment, reservationMap) || {};
        return {
            reservationNumber: reservation.reservationNumber || ("RSV-" + (reservation.sid || payment.reservationId || payment.sid)),
            guestName: reservation.memberName || reservation.guestName || "고객",
            roomName: makeAdminRoomName(reservation),
            checkInDate: reservation.checkInDate,
            checkOutDate: reservation.checkOutDate,
            amount: getPaymentRevenueAmount(payment)
        };
    }).sort(function (a, b) {
        return b.amount - a.amount;
    }).slice(0, 5);
}

function buildSalesDailyRows(payments, reservationMap, target) {
    const groups = new Map();
    payments.forEach(function (payment) {
        const date = parseAdminDate(getPaymentRevenueDate(payment));
        if (!date || date.getFullYear() !== target.year || date.getMonth() !== target.month) return;
        const key = date.toISOString().slice(0, 10);
        const reservation = getPaymentReservation(payment, reservationMap) || {};
        const current = groups.get(key) || {
            date: key,
            dateLabel: String(date.getMonth() + 1).padStart(2, "0") + "." + String(date.getDate()).padStart(2, "0") + " (" + ADMIN_WEEKDAYS[date.getDay()] + ")",
            paymentCount: 0,
            reservationIds: new Set(),
            total: 0,
            cancelledAmount: 0
        };
        current.paymentCount += 1;
        current.total += getPaymentRevenueAmount(payment);
        if (reservation.sid) current.reservationIds.add(String(reservation.sid));
        if (reservation.reservationStatus === "CANCELLED" || reservation.reservationStatus === "NO_SHOW") {
            current.cancelledAmount += Number(reservation.totalAmount || 0);
        }
        groups.set(key, current);
    });

    return Array.from(groups.values())
        .sort(function (a, b) { return a.date.localeCompare(b.date); })
        .map(function (row) {
            return Object.assign(row, {
                reservationCount: row.reservationIds.size,
                netTotal: row.total
            });
        });
}

function bindGuestFilters() {
    $("#guestKeyword, #guestStatusFilter, #guestPointFilter")
        .off("change.adminGuest input.adminGuest")
        .on("change.adminGuest input.adminGuest", debounce(function () {
            renderGuestTable();
            $("#guestReservationPanel").prop("hidden", true).empty();
        }, 200));

    $("#guestFilterReset").off("click.adminGuest").on("click.adminGuest", function () {
        $("#guestKeyword").val("");
        $("#guestStatusFilter").val("");
        $("#guestPointFilter").val("");
        renderGuestTable();
        $("#guestReservationPanel").prop("hidden", true).empty();
    });

    $("#guestTableWrap")
        .off("click.adminGuest")
        .on("click.adminGuest", "[data-guest-reservations]", function () {
            renderGuestReservationPanel(Number($(this).data("guestReservations")));
        });
}

function getGuestFilterValues() {
    return {
        keyword: ($("#guestKeyword").val() || "").trim().toLowerCase(),
        status: $("#guestStatusFilter").val() || "",
        point: $("#guestPointFilter").val() || ""
    };
}

function renderGuestMetrics() {
    if (!ADMIN_GUEST_STATE) return;
    const members = ADMIN_GUEST_STATE.members;
    const reservationsByMember = ADMIN_GUEST_STATE.reservationsByMember;
    const activeCount = members.filter(function (member) { return member.status === "ACTIVE"; }).length;
    const reservedMemberCount = members.filter(function (member) {
        return (reservationsByMember.get(String(member.sid)) || []).length > 0;
    }).length;
    const totalPoint = members.reduce(function (sum, member) {
        return sum + Number(member.point || 0);
    }, 0);
    const cards = [
        ["총 고객", formatAdminNumber(members.length) + "명", "fa-users", "사이트 가입자"],
        ["활성 고객", formatAdminNumber(activeCount) + "명", "fa-user-check", "정상 사용 가능"],
        ["예약 고객", formatAdminNumber(reservedMemberCount) + "명", "fa-calendar-check", "현재 호텔 예약자"],
        ["포인트 합계", formatAdminNumber(totalPoint) + "P", "fa-coins", "포인트 총합"]
    ];

    $("#guestMetrics").html(cards.map(function ([label, value, icon, trend]) {
        return `<article class="metric-card">
            <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend">${escapeHtml(trend)}</span></div>
            <div class="metric-label">${escapeHtml(label)}</div>
            <div class="metric-value">${escapeHtml(value)}</div>
        </article>`;
    }).join(""));
}

function renderGuestTable() {
    if (!ADMIN_GUEST_STATE) return;
    const filters = getGuestFilterValues();
    const members = ADMIN_GUEST_STATE.members.filter(function (member) {
        const text = [member.name, member.email, member.phone].join(" ").toLowerCase();
        const point = Number(member.point || 0);
        if (filters.keyword && !text.includes(filters.keyword)) return false;
        if (filters.status && member.status !== filters.status) return false;
        if (filters.point === "HAS_POINT" && point <= 0) return false;
        if (filters.point === "NO_POINT" && point > 0) return false;
        return true;
    });

    if (!members.length) {
        $("#guestTableWrap").html(emptyAdminState("조건에 맞는 고객이 없습니다."));
        return;
    }

    const rows = members.map(function (member) {
        const reservations = ADMIN_GUEST_STATE.reservationsByMember.get(String(member.sid)) || [];
        const lastReservation = reservations[0] || null;
        return `<tr>
            <td>${renderAdminGuestProfile(member)}</td>
            <td>${escapeHtml(member.email || "-")}</td>
            <td>${escapeHtml(member.phone || "-")}</td>
            <td>${renderGuestReservationButton(member, reservations)}</td>
            <td>${lastReservation ? formatAdminShortDate(lastReservation.checkInDate || lastReservation.createdAt) : "-"}</td>
            <td><strong>${formatAdminNumber(member.point || 0)}P</strong></td>
            <td><span class="status ${guestStatusTone(member.status)}">${escapeHtml(formatGuestStatus(member.status))}</span></td>
        </tr>`;
    }).join("");

    $("#guestTableWrap").html(`
        <table class="admin-table admin-guest-table">
            <thead>
                <tr>
                    <th>고객</th>
                    <th>이메일</th>
                    <th>연락처</th>
                    <th>예약</th>
                    <th>최근 예약</th>
                    <th>포인트</th>
                    <th>상태</th>
                </tr>
            </thead>
            <tbody>${rows}</tbody>
        </table>
    `);
}

function renderAdminGuestProfile(member) {
    const name = member.name || "고객";
    return `<div class="admin-guest-cell"><span>${escapeHtml(name.slice(0, 1))}</span><strong>${escapeHtml(name)}</strong></div>`;
}

function renderGuestReservationButton(member, reservations) {
    const count = reservations.length;
    if (!count) return '<span class="guest-reservation-empty">0건</span>';
    return `<button class="guest-reservation-link" type="button" data-guest-reservations="${escapeHtml(member.sid)}">${formatAdminNumber(count)}건</button>`;
}

function renderGuestReservationPanel(memberId) {
    if (!ADMIN_GUEST_STATE) return;
    const member = ADMIN_GUEST_STATE.members.find(function (item) {
        return Number(item.sid) === Number(memberId);
    });
    const reservations = ADMIN_GUEST_STATE.reservationsByMember.get(String(memberId)) || [];
    const panel = $("#guestReservationPanel");

    if (!member) {
        panel.prop("hidden", false).html(emptyAdminState("고객 정보를 찾을 수 없습니다."));
        return;
    }

    const rows = reservations.map(function (reservation) {
        return `<tr>
            <td><strong>${escapeHtml(reservation.reservationNumber || ("RSV-" + reservation.sid))}</strong></td>
            <td>${escapeHtml(reservation.hotelName || "-")}</td>
            <td>${escapeHtml(makeAdminRoomName(reservation))}</td>
            <td>${formatAdminShortDate(reservation.checkInDate)}</td>
            <td>${formatAdminShortDate(reservation.checkOutDate)}</td>
            <td>${formatAdminNights(reservation)}</td>
            <td><strong>${formatAdminWon(reservation.totalAmount)}</strong></td>
            <td><span class="status ${adminStatusTone(reservation.reservationStatus)}">${escapeHtml(formatAdminStatus(reservation.reservationStatus))}</span></td>
        </tr>`;
    }).join("");

    panel.prop("hidden", false).html(
        panelHead((member.name || "고객") + " 예약 목록", "선택 호텔 기준 " + reservations.length + "건") +
        (reservations.length ? `<div class="admin-table-wrap">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>예약번호</th>
                        <th>호텔</th>
                        <th>객실</th>
                        <th>체크인</th>
                        <th>체크아웃</th>
                        <th>박수</th>
                        <th>금액</th>
                        <th>상태</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>` : emptyAdminState("예약 내역이 없습니다."))
    );
    panel[0].scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function openGuestCouponModal() {
    $("#guestCouponModalRoot").html(`
        <div class="admin-modal-backdrop">
            <div class="admin-modal guest-coupon-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>쿠폰 추가</h2>
                        <p>예약 결제에서 사용할 수 있는 쿠폰 정책을 만듭니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-guest-coupon-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <form id="guestCouponForm">
                    <div class="admin-form-grid">
                        <label class="admin-form-full">
                            쿠폰 이름
                            <input id="guestCouponName" type="text" maxlength="100" placeholder="예: 신규회원 1만원 쿠폰" required>
                        </label>
                        <label>
                            할인 타입
                            <select id="guestCouponDiscountType" required>
                                <option value="PERCENT">퍼센트 할인</option>
                                <option value="FIXED">금액 할인</option>
                            </select>
                        </label>
                        <label>
                            할인값
                            <input id="guestCouponDiscountValue" type="number" min="0" step="1" placeholder="예: 10 또는 10000" required>
                        </label>
                        <label>
                            최소 주문 금액
                            <input id="guestCouponMinOrder" type="number" min="0" step="1000" placeholder="0">
                        </label>
                        <label>
                            총 수량
                            <input id="guestCouponTotalQuantity" type="number" min="1" step="1" placeholder="제한 없음">
                        </label>
                        <label>
                            시작일
                            <input id="guestCouponStartDate" type="date">
                        </label>
                        <label>
                            종료일
                            <input id="guestCouponEndDate" type="date">
                        </label>
                        <div class="admin-form-full guest-coupon-help">
                            <i class="fa-solid fa-circle-info"></i>
                            여기서는 쿠폰 정책만 생성합니다. 생성한 쿠폰은 상단의 쿠폰 발급 버튼에서 특정 회원 또는 조건 대상에게 발급할 수 있습니다.
                        </div>
                    </div>
                    <div class="admin-modal-actions">
                        <button class="admin-btn" type="button" data-guest-coupon-close>취소</button>
                        <button class="admin-btn primary" type="submit"><i class="fa-solid fa-ticket"></i> 저장</button>
                    </div>
                </form>
            </div>
        </div>
    `);

    $("#guestCouponForm").off("submit.guestCoupon").on("submit.guestCoupon", function (event) {
        event.preventDefault();
        saveGuestCoupon();
    });
    $("[data-guest-coupon-close]").off("click.guestCoupon").on("click.guestCoupon", closeGuestCouponModal);
}

function closeGuestCouponModal() {
    $("#guestCouponModalRoot").empty();
}

function saveGuestCoupon() {
    const startDate = $("#guestCouponStartDate").val();
    const endDate = $("#guestCouponEndDate").val();
    const totalQuantity = Number($("#guestCouponTotalQuantity").val() || 0);
    const payload = {
        couponName: ($("#guestCouponName").val() || "").trim(),
        discountType: $("#guestCouponDiscountType").val(),
        discountValue: Number($("#guestCouponDiscountValue").val() || 0),
        minOrderAmount: Number($("#guestCouponMinOrder").val() || 0),
        startDate: startDate ? startDate + "T00:00:00" : null,
        endDate: endDate ? endDate + "T23:59:59" : null,
        totalQuantity: totalQuantity > 0 ? totalQuantity : null
    };

    if (!payload.couponName) {
        alert("쿠폰 이름을 입력해주세요.");
        return;
    }
    if (!payload.discountValue || payload.discountValue <= 0) {
        alert("할인값을 입력해주세요.");
        return;
    }
    if (payload.discountType === "PERCENT" && payload.discountValue > 100) {
        alert("퍼센트 할인은 100 이하로 입력해주세요.");
        return;
    }
    if (payload.startDate && payload.endDate && payload.startDate > payload.endDate) {
        alert("종료일은 시작일 이후로 선택해주세요.");
        return;
    }

    const button = $("#guestCouponForm .admin-btn.primary");
    button.prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 저장 중');

    adminPost("/cou", payload).then(function (coupon) {
        if (ADMIN_GUEST_STATE && coupon && coupon.sid) {
            ADMIN_GUEST_STATE.coupons = asArray(ADMIN_GUEST_STATE.coupons)
                .filter(function (item) { return String(item.sid) !== String(coupon.sid); })
                .concat([coupon]);
        }
        closeGuestCouponModal();
        alert("쿠폰 생성이 완료되었습니다.");
        loadAdminGuestData();
    }, function (xhr) {
        console.error("Coupon save failed", { payload, response: xhr && (xhr.responseJSON || xhr.responseText) });
        alert(getAdminAjaxMessage(xhr, "쿠폰 처리에 실패했습니다."));
        button.prop("disabled", false).html('<i class="fa-solid fa-ticket"></i> 저장');
    });
}

function openGuestCouponIssueModal() {
    $("#guestCouponModalRoot").html(`
        <div class="admin-modal-backdrop">
            <div class="admin-modal guest-coupon-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>쿠폰 발급</h2>
                        <p>생성된 쿠폰 목록을 불러오는 중입니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-guest-coupon-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="admin-loading">쿠폰 데이터를 불러오고 있습니다.</div>
            </div>
        </div>
    `);
    $("[data-guest-coupon-close]").off("click.guestCoupon").on("click.guestCoupon", closeGuestCouponModal);

    adminGet("/cou").then(function (coupons) {
        if (!ADMIN_GUEST_STATE) {
            ADMIN_GUEST_STATE = { members: [], coupons: [], reservationsByMember: new Map() };
        }
        ADMIN_GUEST_STATE.coupons = asArray(coupons);
        renderGuestCouponIssueModal();
    }, function (xhr) {
        $("#guestCouponModalRoot .admin-modal").html(`
            <div class="admin-modal-head">
                <div>
                    <h2>쿠폰 발급</h2>
                    <p>쿠폰 목록을 불러오지 못했습니다.</p>
                </div>
                <button type="button" class="modal-close" data-guest-coupon-close><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="admin-form-grid">
                <div class="admin-form-full guest-coupon-help">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    ${escapeHtml(getAdminAjaxMessage(xhr, "쿠폰 목록을 불러오지 못했습니다."))}
                </div>
            </div>
        `);
        $("[data-guest-coupon-close]").off("click.guestCoupon").on("click.guestCoupon", closeGuestCouponModal);
    });
}

function renderGuestCouponIssueModal() {
    const state = ADMIN_GUEST_STATE || { members: [], coupons: [], reservationsByMember: new Map() };
    const couponOptions = asArray(state.coupons).map(function (coupon) {
        return `<option value="${escapeHtml(coupon.sid)}">${escapeHtml(formatAdminCouponOption(coupon))}</option>`;
    });
    const memberOptions = asArray(state.members).map(function (member) {
        return `<option value="${escapeHtml(member.sid)}">${escapeHtml(formatAdminMemberOption(member))}</option>`;
    });

    $("#guestCouponModalRoot").html(`
        <div class="admin-modal-backdrop">
            <div class="admin-modal guest-coupon-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>쿠폰 발급</h2>
                        <p>생성된 쿠폰을 특정 회원 또는 조건에 맞는 회원에게 발급합니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-guest-coupon-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <form id="guestCouponIssueForm">
                    <div class="admin-form-grid">
                        <label class="admin-form-full">
                            발급할 쿠폰
                            <select id="guestCouponIssueCoupon" required>
                                ${couponOptions.length ? couponOptions.join("") : '<option value="">생성된 쿠폰이 없습니다</option>'}
                            </select>
                        </label>
                        <label>
                            발급 방식
                            <select id="guestCouponIssueMode">
                                <option value="member">특정 대상</option>
                                <option value="condition">조건 대상 전체</option>
                            </select>
                        </label>
                        <label id="guestCouponMemberField">
                            특정 대상
                            <select id="guestCouponIssueMember">
                                ${memberOptions.length ? memberOptions.join("") : '<option value="">회원이 없습니다</option>'}
                            </select>
                        </label>
                        <label class="guest-coupon-condition-field" hidden>
                            회원 상태
                            <select id="guestCouponIssueStatus">
                                <option value="">전체 상태</option>
                                <option value="ACTIVE">활성</option>
                                <option value="INACTIVE">비활성</option>
                                <option value="STOP">정지</option>
                            </select>
                        </label>
                        <label class="guest-coupon-condition-field" hidden>
                            권한
                            <select id="guestCouponIssueRole">
                                <option value="">전체 권한</option>
                                <option value="USER">사용자</option>
                                <option value="ADMIN">관리자</option>
                            </select>
                        </label>
                        <label class="guest-coupon-condition-field" hidden>
                            예약 이력
                            <select id="guestCouponIssueReservation">
                                <option value="">전체</option>
                                <option value="HAS">예약 있음</option>
                                <option value="NONE">예약 없음</option>
                            </select>
                        </label>
                        <label class="guest-coupon-condition-field" hidden>
                            최소 포인트
                            <input id="guestCouponIssueMinPoint" type="number" min="0" step="100" placeholder="제한 없음">
                        </label>
                        <div class="admin-form-full guest-coupon-help">
                            <i class="fa-solid fa-circle-info"></i>
                            조건 대상 전체 발급은 현재 관리자 화면에서 불러온 회원 목록 기준으로 처리됩니다. 관리자 계정도 대상에 포함할 수 있습니다.
                        </div>
                        <div id="guestCouponIssuePreview" class="admin-form-full guest-coupon-preview"></div>
                    </div>
                    <div class="admin-modal-actions">
                        <button class="admin-btn" type="button" data-guest-coupon-close>취소</button>
                        <button class="admin-btn primary" type="submit"><i class="fa-solid fa-paper-plane"></i> 발급</button>
                    </div>
                </form>
            </div>
        </div>
    `);

    $("#guestCouponIssueForm").off("submit.guestCouponIssue").on("submit.guestCouponIssue", function (event) {
        event.preventDefault();
        issueGuestCoupon();
    });
    $("#guestCouponIssueMode, #guestCouponIssueMember, #guestCouponIssueStatus, #guestCouponIssueRole, #guestCouponIssueReservation, #guestCouponIssueMinPoint")
        .off("input.guestCouponIssue change.guestCouponIssue")
        .on("input.guestCouponIssue change.guestCouponIssue", updateGuestCouponIssuePreview);
    $("[data-guest-coupon-close]").off("click.guestCoupon").on("click.guestCoupon", closeGuestCouponModal);
    updateGuestCouponIssuePreview();
}

function updateGuestCouponIssuePreview() {
    const mode = $("#guestCouponIssueMode").val();
    const isCondition = mode === "condition";
    $("#guestCouponMemberField").prop("hidden", isCondition);
    $(".guest-coupon-condition-field").prop("hidden", !isCondition);

    const targets = getGuestCouponIssueTargets();
    $("#guestCouponIssuePreview").html(`
        <strong>${formatAdminNumber(targets.length)}명에게 발급 예정</strong>
        <span>${targets.slice(0, 5).map(formatAdminMemberOption).map(escapeHtml).join(" / ") || "대상을 선택해주세요."}${targets.length > 5 ? " 외 " + (targets.length - 5) + "명" : ""}</span>
    `);
}

function getGuestCouponIssueTargets() {
    const state = ADMIN_GUEST_STATE || { members: [], reservationsByMember: new Map() };
    const members = asArray(state.members);
    if ($("#guestCouponIssueMode").val() !== "condition") {
        const memberId = $("#guestCouponIssueMember").val();
        return members.filter(function (member) {
            return String(member.sid) === String(memberId);
        });
    }

    const status = $("#guestCouponIssueStatus").val();
    const role = $("#guestCouponIssueRole").val();
    const reservationFilter = $("#guestCouponIssueReservation").val();
    const minPoint = Number($("#guestCouponIssueMinPoint").val() || 0);
    return members.filter(function (member) {
        const reservationCount = (state.reservationsByMember.get(String(member.sid)) || []).length;
        if (status && member.status !== status) return false;
        if (role && member.role !== role) return false;
        if (reservationFilter === "HAS" && reservationCount <= 0) return false;
        if (reservationFilter === "NONE" && reservationCount > 0) return false;
        if (minPoint > 0 && Number(member.point || 0) < minPoint) return false;
        return true;
    });
}

function issueGuestCoupon() {
    const couponId = Number($("#guestCouponIssueCoupon").val() || 0);
    const targets = getGuestCouponIssueTargets();
    if (!couponId) {
        alert("발급할 쿠폰을 선택해주세요.");
        return;
    }
    if (!targets.length) {
        alert("발급 대상이 없습니다.");
        return;
    }
    if (targets.length > 1 && !confirm(formatAdminNumber(targets.length) + "명에게 쿠폰을 발급할까요?")) {
        return;
    }

    const button = $("#guestCouponIssueForm .admin-btn.primary");
    button.prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 발급 중');
    issueGuestCouponSequentially(couponId, targets, 0, { success: 0, fail: 0 }, button);
}

function issueGuestCouponSequentially(couponId, targets, index, result, button) {
    if (index >= targets.length) {
        closeGuestCouponModal();
        alert("쿠폰 발급 완료: 성공 " + result.success + "명 / 실패 " + result.fail + "명");
        loadAdminGuestData();
        return;
    }

    adminPost("/cou/issue", { couponId, memberId: Number(targets[index].sid) })
        .then(function () {
            result.success += 1;
        }, function (xhr) {
            result.fail += 1;
            console.warn("Coupon issue failed", targets[index], xhr && (xhr.responseJSON || xhr.responseText));
        })
        .always(function () {
            button.html('<i class="fa-solid fa-spinner fa-spin"></i> ' + (index + 1) + "/" + targets.length);
            issueGuestCouponSequentially(couponId, targets, index + 1, result, button);
        });
}

function formatAdminCouponOption(coupon) {
    const type = String(coupon.discountType || "").toUpperCase() === "PERCENT"
        ? Number(coupon.discountValue || 0) + "%"
        : formatAdminWon(coupon.discountValue || 0);
    return (coupon.couponName || "쿠폰") + " · " + type + " · " + formatAdminCouponPeriod(coupon);
}

function formatAdminCouponPeriod(coupon) {
    const start = formatAdminFullDate(coupon.startDate);
    const end = formatAdminFullDate(coupon.endDate);
    if (start === "-" && end === "-") return "상시";
    if (start === "-") return end + "까지";
    if (end === "-") return start + "부터";
    return start + " ~ " + end;
}

function formatAdminFullDate(value) {
    if (!value) return "-";
    const text = String(value).slice(0, 10);
    if (!/^\d{4}-\d{2}-\d{2}$/.test(text)) return "-";
    return text.replaceAll("-", ".");
}

function formatAdminMemberOption(member) {
    const role = member.role === "ADMIN" ? "관리자" : "사용자";
    return [member.name || "이름 없음", role, member.email || member.phone || ("ID " + member.sid)]
        .filter(Boolean)
        .join(" · ");
}

function groupReservationsByMember(reservations) {
    const grouped = new Map();
    reservations.forEach(function (reservation) {
        if (reservation.memberId == null) return;
        const key = String(reservation.memberId);
        if (!grouped.has(key)) grouped.set(key, []);
        grouped.get(key).push(reservation);
    });
    grouped.forEach(function (items) {
        items.sort(function (a, b) {
            return (parseAdminDate(b.checkInDate || b.createdAt) || 0) - (parseAdminDate(a.checkInDate || a.createdAt) || 0);
        });
    });
    return grouped;
}

function formatGuestStatus(status) {
    const labels = {
        ACTIVE: "활성",
        INACTIVE: "비활성",
        STOP: "정지",
        DELETED: "탈퇴"
    };
    return labels[status] || status || "상태 없음";
}

function guestStatusTone(status) {
    if (status === "ACTIVE") return "";
    if (status === "INACTIVE") return "orange";
    if (status === "STOP" || status === "DELETED") return "red";
    return "blue";
}

function loadAdminPromotionData() {
    const requests = {
        promotions: adminGetSafe("/prom/search?page=0&size=200&sort=createdAt,desc", { content: [] }),
        roomTypes: adminGetSafe("/roomtype", [])
    };

    $.when(requests.promotions, requests.roomTypes)
        .done(function (promotionsResult, roomTypesResult) {
            const promotionPage = normalizeAjaxResult(promotionsResult);
            const roomTypes = asArray(normalizeAjaxResult(roomTypesResult));
            const promotions = asPageContent(promotionPage);
            ADMIN_PROMOTION_STATE = { promotions, roomTypes };
            renderPromotionMetrics(promotions);
            renderPromotionFilters(roomTypes);
            renderPromotionTable(filterPromotions(promotions), roomTypes);
        })
        .fail(function () {
            $("#promotionTableWrap").html(emptyAdminState("프로모션 데이터를 불러오지 못했습니다."));
        });
}

function renderPromotionMetrics(promotions) {
    const active = promotions.filter(function (promotion) { return getPromotionStatusKey(promotion) === "ACTIVE"; }).length;
    const expected = promotions.filter(function (promotion) { return getPromotionStatusKey(promotion) === "EXPECTED"; }).length;
    const cards = [
        ["진행중 프로모션", active + "개", "fa-percent", "현재 적용 중", ""],
        ["예정 프로모션", expected + "개", "fa-calendar-plus", "시작 대기", "warn"],
        ["총 할인 정책", promotions.length + "개", "fa-tags", "객실 타입 기준", ""]
    ];

    $("#promotionMetrics").html(cards.map(function ([label, value, icon, trend, tone]) {
        return `<article class="metric-card">
            <div class="metric-top"><span class="metric-icon ${tone || ""}"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${escapeHtml(trend)}</span></div>
            <div class="metric-label">${escapeHtml(label)}</div>
            <div class="metric-value">${escapeHtml(value)}</div>
        </article>`;
    }).join(""));
}

function renderPromotionFilters(roomTypes) {
    $("#promotionFilterBar").html(`
        <div class="admin-filter-row promotion-filters">
            <label class="admin-search-field">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input id="promotionKeywordFilter" type="search" placeholder="프로모션명 검색">
            </label>
            <select id="promotionStatusFilter" class="admin-filter-select">
                <option value="">전체 상태</option>
                <option value="ACTIVE">진행중</option>
                <option value="EXPECTED">예정</option>
                <option value="STOP">일시정지</option>
                <option value="END">종료</option>
            </select>
            <select id="promotionRoomTypeFilter" class="admin-filter-select">
                <option value="">전체 객실 타입</option>
                ${(roomTypes || []).map(function (roomType) {
                    return `<option value="${escapeHtml(roomType.sid)}">${escapeHtml(roomType.title)}</option>`;
                }).join("")}
            </select>
            <button type="button" class="admin-btn ghost" id="promotionFilterReset">필터 초기화</button>
        </div>
    `);

    $("#promotionKeywordFilter, #promotionStatusFilter, #promotionRoomTypeFilter")
        .off("input.adminPromotionFilter change.adminPromotionFilter")
        .on("input.adminPromotionFilter change.adminPromotionFilter", function () {
            const state = ADMIN_PROMOTION_STATE || { promotions: [], roomTypes: [] };
            renderPromotionTable(filterPromotions(state.promotions), state.roomTypes);
        });

    $("#promotionFilterReset").off("click.adminPromotionFilter").on("click.adminPromotionFilter", function () {
        $("#promotionKeywordFilter").val("");
        $("#promotionStatusFilter").val("");
        $("#promotionRoomTypeFilter").val("");
        const state = ADMIN_PROMOTION_STATE || { promotions: [], roomTypes: [] };
        renderPromotionTable(state.promotions, state.roomTypes);
    });
}

function filterPromotions(promotions) {
    const keyword = String($("#promotionKeywordFilter").val() || "").trim().toLowerCase();
    const status = $("#promotionStatusFilter").val();
    const roomTypeId = $("#promotionRoomTypeFilter").val();

    return (promotions || []).filter(function (promotion) {
        const matchesKeyword = !keyword || String(promotion.promotionName || "").toLowerCase().includes(keyword);
        const matchesStatus = !status || getPromotionStatusKey(promotion) === status;
        const matchesRoomType = !roomTypeId || String(promotion.roomTypeId) === String(roomTypeId);
        return matchesKeyword && matchesStatus && matchesRoomType;
    });
}

function renderPromotionTable(promotions, roomTypes) {
    if (!promotions.length) {
        $("#promotionTableWrap").html(emptyAdminState("조건에 맞는 프로모션이 없습니다."));
        return;
    }

    const rows = promotions.map(function (promotion) {
        const status = getPromotionStatusKey(promotion);
        const roomType = findPromotionRoomType(promotion.roomTypeId, roomTypes);
        return `<tr>
            <td>
                <div class="promotion-name-cell">
                    <strong>${escapeHtml(promotion.promotionName || "프로모션명 없음")}</strong>
                    <small>전체 호텔 · ${escapeHtml(roomType ? roomType.title : "객실 타입 없음")} 객실</small>
                </div>
            </td>
            <td>${escapeHtml(formatPromotionDiscount(promotion.discountContent))}</td>
            <td>${escapeHtml(roomType ? roomType.title : "-")}</td>
            <td>${escapeHtml(formatPromotionPeriod(promotion))}</td>
            <td><span class="status ${promotionStatusTone(status)}">${escapeHtml(formatPromotionStatus(status))}</span></td>
            <td>${renderPromotionActions(promotion.sid)}</td>
        </tr>`;
    }).join("");

    $("#promotionTableWrap").html(`
        <div class="admin-table-wrap">
            <table class="admin-table promotion-table">
                <thead>
                    <tr>
                        <th>프로모션명</th>
                        <th>할인율/금액</th>
                        <th>적용 객실 타입</th>
                        <th>기간</th>
                        <th>상태</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>
    `);

    bindPromotionTableActions();
}

function bindPromotionTableActions() {
    $("#promotionTableWrap").off("click.adminPromotionEdit").on("click.adminPromotionEdit", "[data-promotion-edit]", function () {
        const promotion = findPromotionById($(this).data("promotionEdit"));
        if (promotion) openPromotionModal(promotion);
    });

    $("#promotionTableWrap").off("click.adminPromotionDelete").on("click.adminPromotionDelete", "[data-promotion-delete]", function () {
        deletePromotion($(this).data("promotionDelete"));
    });
}

function renderPromotionActions(id) {
    return `<div class="row-actions">
        <button class="icon-btn" type="button" title="수정" data-promotion-edit="${escapeHtml(id)}"><i class="fa-solid fa-pen"></i></button>
        <button class="icon-btn danger" type="button" title="삭제" data-promotion-delete="${escapeHtml(id)}"><i class="fa-solid fa-trash"></i></button>
    </div>`;
}

function openPromotionModal(promotion) {
    const isEdit = Boolean(promotion);
    const roomTypes = ADMIN_PROMOTION_STATE ? ADMIN_PROMOTION_STATE.roomTypes : [];
    const status = promotion ? getPromotionStatusKey(promotion) : "ACTIVE";
    const discount = parsePromotionDiscount(promotion && promotion.discountContent);

    $("#promotionModalRoot").html(`
        <div class="admin-modal-backdrop">
            <form id="promotionForm" class="admin-modal promotion-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>${isEdit ? "프로모션 수정" : "프로모션 생성"}</h2>
                        <p>${isEdit ? "이름과 상태만 변경할 수 있습니다." : "전체 호텔의 선택 객실 타입에 할인을 적용합니다."}</p>
                    </div>
                    <button type="button" class="modal-close" data-promotion-modal-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="admin-form-grid">
                    <label>
                        <span>프로모션 이름</span>
                        <input id="promotionNameInput" type="text" maxlength="50" value="${escapeHtml(promotion ? promotion.promotionName || "" : "")}" required>
                    </label>
                    <label>
                        <span>상태</span>
                        <select id="promotionStatusInput">${promotionStatusOptions(status)}</select>
                    </label>
                    <label>
                        <span>시작일</span>
                        <input id="promotionStartInput" type="date" value="${promotionDateValue(promotion && promotion.startDate)}" ${isEdit ? "disabled" : "required"}>
                    </label>
                    <label>
                        <span>종료일</span>
                        <input id="promotionEndInput" type="date" value="${promotionDateValue(promotion && promotion.endDate)}" ${isEdit ? "disabled" : "required"}>
                    </label>
                    <label>
                        <span>적용할 객실 타입</span>
                        <select id="promotionRoomTypeInput" ${isEdit ? "disabled" : "required"}>
                            <option value="">객실 타입 선택</option>
                            ${roomTypes.map(function (roomType) {
                                const selected = promotion && String(promotion.roomTypeId) === String(roomType.sid) ? "selected" : "";
                                return `<option value="${escapeHtml(roomType.sid)}" ${selected}>${escapeHtml(roomType.title)}</option>`;
                            }).join("")}
                        </select>
                    </label>
                    <label>
                        <span>할인 타입</span>
                        <select id="promotionDiscountTypeInput" ${isEdit ? "disabled" : ""}>
                            <option value="RATE" ${discount.type === "RATE" ? "selected" : ""}>퍼센트 할인</option>
                            <option value="FLAT" ${discount.type === "FLAT" ? "selected" : ""}>지정 금액 할인</option>
                        </select>
                    </label>
                    <label class="admin-form-full">
                        <span>할인 값</span>
                        <input id="promotionDiscountValueInput" type="number" min="1" value="${escapeHtml(discount.value || "")}" ${isEdit ? "disabled" : "required"} placeholder="예: 30 또는 30000">
                    </label>
                </div>
                <div class="admin-modal-actions">
                    <button type="button" class="admin-btn" data-promotion-modal-close>취소</button>
                    <button type="submit" class="admin-btn primary">${isEdit ? "저장" : "생성"}</button>
                </div>
            </form>
        </div>
    `);

    $("[data-promotion-modal-close]").on("click", closePromotionModal);
    $("#promotionForm").on("submit", function (event) {
        event.preventDefault();
        savePromotion(promotion);
    });
}

function closePromotionModal() {
    $("#promotionModalRoot").empty();
}

function savePromotion(original) {
    const isEdit = Boolean(original);
    const name = $("#promotionNameInput").val().trim();
    const status = $("#promotionStatusInput").val();

    if (!name) {
        alert("프로모션 이름을 입력해주세요.");
        return;
    }

    const payload = isEdit
        ? {
            sid: original.sid,
            promotionName: name,
            status
        }
        : {
            roomTypeId: Number($("#promotionRoomTypeInput").val()),
            promotionName: name,
            discountContent: makePromotionDiscountContent($("#promotionDiscountTypeInput").val(), $("#promotionDiscountValueInput").val()),
            startDate: $("#promotionStartInput").val() + "T00:00:00",
            endDate: $("#promotionEndInput").val() + "T23:59:59",
            status
        };

    if (!isEdit && (!payload.roomTypeId || !$("#promotionStartInput").val() || !$("#promotionEndInput").val() || !$("#promotionDiscountValueInput").val())) {
        alert("프로모션 생성 정보를 모두 입력해주세요.");
        return;
    }

    const request = isEdit ? adminPatch("/prom", payload) : adminPost("/prom", payload);
    request.then(function () {
        closePromotionModal();
        loadAdminPromotionData();
    }, function (xhr) {
        console.error("Promotion save failed", {
            payload,
            status: xhr && xhr.status,
            response: xhr && (xhr.responseJSON || xhr.responseText)
        });
        alert(getAdminAjaxMessage(xhr, "프로모션 저장에 실패했습니다."));
    });
}

function deletePromotion(id) {
    if (!id || !confirm("이 프로모션을 삭제할까요?")) return;

    $.ajax({
        url: window.StayNowConfig.apiUrl("/prom/" + id),
        type: "DELETE",
        headers: adminAuthHeaders()
    }).done(function () {
        loadAdminPromotionData();
    }).fail(function (xhr) {
        alert(getAdminAjaxMessage(xhr, "프로모션 삭제에 실패했습니다."));
    });
}

function findPromotionById(id) {
    const promotions = ADMIN_PROMOTION_STATE ? ADMIN_PROMOTION_STATE.promotions : [];
    return promotions.find(function (promotion) {
        return String(promotion.sid) === String(id);
    });
}

function findPromotionRoomType(roomTypeId, roomTypes) {
    return (roomTypes || []).find(function (roomType) {
        return String(roomType.sid) === String(roomTypeId);
    });
}

function getPromotionStatusKey(promotion) {
    const status = String((promotion && promotion.status) || "").trim();
    if (status === "예정") return "EXPECTED";
    if (status === "진행중" || status === "Active") return "ACTIVE";
    if (status === "일시정지" || status === "중지") return "STOP";
    if (status === "종료") return "END";
    if (["EXPECTED", "ACTIVE", "STOP", "END"].includes(status.toUpperCase())) return status.toUpperCase();

    if (promotion && promotion.startDate && promotion.endDate) {
        const now = new Date();
        const start = parseAdminDate(promotion.startDate);
        const end = parseAdminDate(promotion.endDate);
        if (start && now < start) return "EXPECTED";
        if (end && now > end) return "END";
    }

    return "ACTIVE";
}

function formatPromotionStatus(status) {
    const labels = {
        EXPECTED: "예정",
        ACTIVE: "진행중",
        STOP: "일시정지",
        END: "종료"
    };
    return labels[status] || status || "상태 없음";
}

function promotionStatusTone(status) {
    if (status === "EXPECTED") return "orange";
    if (status === "STOP" || status === "END") return "red";
    return "";
}

function promotionStatusOptions(selected) {
    return [
        ["EXPECTED", "예정"],
        ["ACTIVE", "진행중"],
        ["STOP", "일시정지"],
        ["END", "종료"]
    ].map(function ([value, label]) {
        return `<option value="${value}" ${value === selected ? "selected" : ""}>${label}</option>`;
    }).join("");
}

function promotionDateValue(value) {
    if (!value) return "";
    return String(value).slice(0, 10);
}

function formatPromotionPeriod(promotion) {
    return promotionDateValue(promotion.startDate).replaceAll("-", ".") + " ~ " + promotionDateValue(promotion.endDate).replaceAll("-", ".");
}

function makePromotionDiscountContent(type, value) {
    const amount = Number(value || 0);
    if (type === "FLAT") {
        return amount.toLocaleString() + "원";
    }
    return amount + "%";
}

function parsePromotionDiscount(content) {
    const text = String(content || "");
    const valueMatch = text.match(/\d[\d,]*/);
    const value = valueMatch ? Number(valueMatch[0].replaceAll(",", "")) : "";
    return {
        type: text.includes("원") ? "FLAT" : "RATE",
        value
    };
}

function formatPromotionDiscount(content) {
    return content || "-";
}


function renderReservationMetrics(reservations) {
    const todayNew = reservations.filter(function (item) { return isToday(item.createdAt); }).length;
    const checkIns = reservations.filter(function (item) { return isToday(item.checkInDate); }).length;
    const cancelled = reservations.filter(function (item) { return item.reservationStatus === "CANCELLED" || item.reservationStatus === "NO_SHOW"; }).length;
    const monthCount = reservations.filter(function (item) { return isThisMonth(item.createdAt || item.checkInDate); }).length;
    const cards = [
        ["오늘 신규 예약", todayNew + "건", "fa-calendar-check", "선택 호텔", ""],
        ["체크인 예정", checkIns + "팀", "fa-right-to-bracket", "오늘 체크인", "warn"],
        ["취소된 건", cancelled + "건", "fa-circle-xmark", "취소/노쇼", "danger"],
        ["이번 달 예약", monthCount + "건", "fa-calendar-days", "선택 호텔", ""]
    ];

    $("#reservationMetrics").html(cards.map(function ([label, value, icon, trend, tone]) {
        return `<article class="metric-card">
            <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${escapeHtml(trend)}</span></div>
            <div class="metric-label">${escapeHtml(label)}</div>
            <div class="metric-value">${escapeHtml(value)}</div>
        </article>`;
    }).join(""));
}

function renderReservationActiveFilters(filters) {
    const roomType = ADMIN_ROOM_TYPES.find(function (type) {
        return String(type.sid) === String(filters.roomTypeId || "");
    });
    const chips = [
        filters.dateFrom || filters.dateTo ? "기간 적용" : "",
        filters.status ? formatAdminStatus(filters.status) : "",
        filters.keyword ? "검색: " + filters.keyword : "",
        roomType ? "객실: " + (roomType.title || roomType.sid) : ""
    ].filter(Boolean);

    $("#reservationActiveFilters").html(
        '<span>활성 필터:</span>' +
        (chips.length ? chips.map(function (chip) { return `<button class="chip active" type="button">${escapeHtml(chip)}</button>`; }).join("") : '<button class="chip" type="button">없음</button>') +
        '<button class="chip danger" id="reservationInlineReset" type="button">필터 초기화</button>'
    );

    $("#reservationInlineReset").off("click.adminReservation").on("click.adminReservation", function () {
        $("#reservationFilterReset").trigger("click");
    });
}

function renderReservationTable(page) {
    const rows = asPageContent(page);
    if (!rows.length) {
        $("#reservationTableWrap").html(emptyAdminState("예약 데이터가 없습니다."));
        return;
    }

    const body = rows.map(function (reservation) {
        const status = reservation.reservationStatus;
        const canCheckIn = status === "CONFIRMED" || status === "UPCOMING";
        return `<tr class="${status === "CANCELLED" || status === "NO_SHOW" ? "danger-row" : ""}">
            <td><strong>${escapeHtml(reservation.reservationNumber || ("RSV-" + reservation.sid))}</strong></td>
            <td>${renderAdminGuestCell(reservation)}</td>
            <td>${escapeHtml(makeAdminRoomName(reservation))}</td>
            <td>${formatAdminShortDate(reservation.checkInDate)}</td>
            <td>${formatAdminShortDate(reservation.checkOutDate)}</td>
            <td>${formatAdminNights(reservation)}</td>
            <td><strong>${formatAdminWon(reservation.totalAmount)}</strong></td>
            <td><span class="status ${adminStatusTone(status)}">${escapeHtml(formatAdminStatus(status))}</span></td>
            <td>${renderReservationActions(reservation, canCheckIn)}</td>
        </tr>`;
    }).join("");

    $("#reservationTableWrap").html(`
        <table class="admin-table admin-reservation-table">
            <thead>
                <tr>
                    <th>예약번호</th>
                    <th>고객명</th>
                    <th>객실</th>
                    <th>체크인</th>
                    <th>체크아웃</th>
                    <th>박수</th>
                    <th>금액</th>
                    <th>상태</th>
                    <th>액션</th>
                </tr>
            </thead>
            <tbody>${body}</tbody>
        </table>
        ${renderAdminReservationPagination(page)}
    `);
}

function renderAdminGuestCell(reservation) {
    const name = reservation.memberName || reservation.guestName || "고객";
    const initial = name.slice(0, 1);
    return `<div class="admin-guest-cell"><span>${escapeHtml(initial)}</span><strong>${escapeHtml(name)}</strong></div>`;
}

function renderReservationActions(reservation, canCheckIn) {
    return `<span class="inline-actions">
        <button class="icon-btn ${canCheckIn ? "" : "disabled"}" type="button" ${canCheckIn ? `data-admin-checkin="${escapeHtml(reservation.sid)}"` : "disabled"} title="체크인"><i class="fa-solid fa-check"></i></button>
    </span>`;
}

function loadAdminCheckinData() {
    const requests = {
        hotels: adminGetSafe("/hotel/all", []),
        popularHotels: adminGetSafe("/hotel/pop4", []),
        reservations: adminGetSafe("/reservation/search?page=0&size=500&sort=checkInDate,asc", { content: [] })
    };

    $.when(requests.hotels, requests.popularHotels, requests.reservations)
        .done(function (hotelsResult, popularHotelsResult, reservationsResult) {
            const hotels = mergeAdminHotels(asPageContent(normalizeAjaxResult(hotelsResult)), asArray(normalizeAjaxResult(popularHotelsResult)));
            const selectedHotel = getSelectedAdminHotel(hotels);
            const reservations = filterReservationsByHotel(asPageContent(normalizeAjaxResult(reservationsResult)), selectedHotel);
            renderAdminHotelSelector(hotels, selectedHotel);
            window.ADMIN_CHECKIN_STATE = { selectedHotel, reservations };
            bindAdminCheckinPage();
            renderAdminCheckinPage();
        })
        .fail(function () {
            $("#checkinTableWrap").html(emptyAdminState("체크인 데이터를 불러오지 못했습니다."));
        });
}

function bindAdminCheckinPage() {
    $("#checkinKeyword, #checkinStatusFilter")
        .off("input.adminCheckin change.adminCheckin")
        .on("input.adminCheckin change.adminCheckin", debounce(renderAdminCheckinPage, 160));

    $(".checkin-admin-panel")
        .off("change.adminCheckinSelect")
        .on("change.adminCheckinSelect", "#checkinSelectAll", function () {
            $(".checkin-row-check").prop("checked", $(this).is(":checked"));
        })
        .off("click.adminCheckinBulk")
        .on("click.adminCheckinBulk", "[data-checkin-bulk]", function () {
            bulkAdminCheckinAction($(this).data("checkinBulk"));
        });

    $("#todayCheckinPanel")
        .off("click.adminTodayMore")
        .on("click.adminTodayMore", "[data-open-today-checkins]", openTodayCheckinModal);

    $("#checkinModalRoot")
        .off("click.adminCheckinModal")
        .on("click.adminCheckinModal", "[data-checkin-modal-close]", function () {
            $("#checkinModalRoot").empty();
        });
}

function getFilteredCheckinReservations() {
    const state = window.ADMIN_CHECKIN_STATE || { reservations: [] };
    const keyword = ($("#checkinKeyword").val() || "").trim().toLowerCase();
    const status = $("#checkinStatusFilter").val() || "";
    return state.reservations.filter(function (reservation) {
        const text = [
            reservation.reservationNumber,
            reservation.memberName,
            reservation.guestName,
            makeAdminRoomName(reservation)
        ].join(" ").toLowerCase();
        const matchesKeyword = !keyword || text.includes(keyword);
        const matchesStatus = !status || reservation.reservationStatus === status;
        return matchesKeyword && matchesStatus;
    });
}

function renderAdminCheckinPage() {
    const state = window.ADMIN_CHECKIN_STATE || { reservations: [] };
    const reservations = getFilteredCheckinReservations();
    const todayCheckIns = state.reservations.filter(function (reservation) { return isToday(reservation.checkInDate); });
    const todayCheckOuts = state.reservations.filter(function (reservation) { return isToday(reservation.checkOutDate); });
    const inUse = state.reservations.filter(function (reservation) { return reservation.reservationStatus === "CHECKED_IN"; });
    const todayTotal = state.reservations.filter(function (reservation) {
        return isToday(reservation.checkInDate) || isToday(reservation.checkOutDate) || isToday(reservation.createdAt);
    });

    renderCheckinMetrics({
        todayTotal: todayTotal.length,
        todayCheckIns: todayCheckIns.length,
        todayCheckOuts: todayCheckOuts.length,
        inUse: inUse.length
    });
    renderCheckinTable(reservations);
    renderTodayCheckinPanel(todayCheckIns);
    renderCheckinCalendar(state.reservations);
}

function renderCheckinMetrics(summary) {
    const cards = [
        ["오늘 전체 예약", summary.todayTotal + "건", "fa-calendar-check", "오늘 기준"],
        ["오늘 체크인", summary.todayCheckIns + "건", "fa-right-to-bracket", "도착 예정"],
        ["오늘 체크아웃", summary.todayCheckOuts + "건", "fa-right-from-bracket", "퇴실 예정", "warn"],
        ["현재 사용중", summary.inUse + "실", "fa-bed", "체크인 완료"]
    ];
    $("#checkinMetrics").html(cards.map(function ([label, value, icon, trend, tone]) {
        return `<article class="metric-card">
            <div class="metric-top"><span class="metric-icon ${tone || ""}"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${escapeHtml(trend)}</span></div>
            <div class="metric-label">${escapeHtml(label)}</div>
            <div class="metric-value">${escapeHtml(value)}</div>
        </article>`;
    }).join(""));
}

function renderCheckinTable(reservations) {
    if (!reservations.length) {
        $("#checkinTableWrap").html(emptyAdminState("조건에 맞는 예약이 없습니다."));
        return;
    }

    const rows = reservations.map(function (reservation) {
        const status = reservation.reservationStatus;
        const canCheckIn = status === "CONFIRMED" || status === "UPCOMING";
        const canCheckOut = status === "CHECKED_IN";
        const canCancel = !["CANCELLED", "NO_SHOW", "CHECKED_OUT", "COMPLETED"].includes(status);
        return `<tr>
            <td><input class="checkin-row-check" type="checkbox" value="${escapeHtml(reservation.sid)}"></td>
            <td><strong>${escapeHtml(reservation.reservationNumber || ("RSV-" + reservation.sid))}</strong></td>
            <td>${renderAdminGuestCell(reservation)}</td>
            <td>${escapeHtml(makeAdminRoomName(reservation))}</td>
            <td>${formatAdminShortDate(reservation.checkInDate)}</td>
            <td>${formatAdminShortDate(reservation.checkOutDate)}</td>
            <td><span class="status ${adminStatusTone(status)}">${escapeHtml(formatAdminStatus(status))}</span></td>
            <td><div class="row-actions">
                <button class="icon-btn ${canCheckIn ? "" : "disabled"}" type="button" ${canCheckIn ? `data-admin-checkin="${escapeHtml(reservation.sid)}"` : "disabled"} title="체크인"><i class="fa-solid fa-right-to-bracket"></i></button>
                <button class="icon-btn ${canCheckOut ? "" : "disabled"}" type="button" ${canCheckOut ? `data-admin-checkout="${escapeHtml(reservation.sid)}"` : "disabled"} title="체크아웃"><i class="fa-solid fa-right-from-bracket"></i></button>
                <button class="icon-btn danger ${canCancel ? "" : "disabled"}" type="button" ${canCancel ? `data-admin-cancel-reservation="${escapeHtml(reservation.sid)}"` : "disabled"} title="취소"><i class="fa-solid fa-xmark"></i></button>
            </div></td>
        </tr>`;
    }).join("");

    $("#checkinTableWrap").html(`
        <table class="admin-table admin-checkin-table">
            <thead><tr><th><input id="checkinSelectAll" type="checkbox"></th><th>예약번호</th><th>고객명</th><th>객실</th><th>체크인</th><th>체크아웃</th><th>상태</th><th>액션</th></tr></thead>
            <tbody>${rows}</tbody>
        </table>
    `);
}

function renderTodayCheckinPanel(todayCheckIns) {
    const visible = todayCheckIns.slice(0, 2);
    const rows = visible.map(function (reservation) {
        return `<div class="schedule-item">
            <span class="schedule-avatar">${escapeHtml((reservation.memberName || reservation.guestName || "고").slice(0, 1))}</span>
            <div><strong>${escapeHtml(reservation.memberName || reservation.guestName || "고객")}</strong><small>${escapeHtml(makeAdminRoomName(reservation))} · ${formatAdminShortDate(reservation.checkInDate)}</small></div>
            <span class="status">${escapeHtml(formatAdminStatus(reservation.reservationStatus))}</span>
        </div>`;
    }).join("");
    const more = Math.max(0, todayCheckIns.length - visible.length);
    $("#todayCheckinPanel").html(panelHead("오늘 체크인 현황", todayCheckIns.length + "건") + `
        <div class="schedule-list">${rows || emptyAdminState("오늘 체크인 예정이 없습니다.")}</div>
        ${more ? `<button class="admin-btn wide" type="button" data-open-today-checkins>나머지 ${more}건 더 보기</button>` : ""}
    `);
}

function renderCheckinCalendar(reservations) {
    const today = new Date();
    const days = Array.from({ length: 7 }, function (_, index) {
        const date = new Date(today);
        date.setDate(today.getDate() + index);
        const count = reservations.filter(function (reservation) {
            return isSameAdminDate(reservation.checkInDate, date);
        }).length;
        return `<div class="calendar-day ${index === 0 ? "today" : ""}">
            <strong>${ADMIN_WEEKDAYS[date.getDay()]}</strong>
            <span>${date.getDate()}</span>
            <em>${count}건</em>
        </div>`;
    }).join("");
    $("#checkinCalendarPanel").html(panelHead("체크인 캘린더", "오늘부터 7일") + `<div class="checkin-calendar">${days}</div>`);
}

function isSameAdminDate(value, date) {
    const parsed = parseAdminDate(value);
    return parsed && parsed.getFullYear() === date.getFullYear() && parsed.getMonth() === date.getMonth() && parsed.getDate() === date.getDate();
}

function openTodayCheckinModal() {
    const state = window.ADMIN_CHECKIN_STATE || { reservations: [] };
    const todayCheckIns = state.reservations.filter(function (reservation) { return isToday(reservation.checkInDate); });
    const rows = todayCheckIns.map(function (reservation) {
        return `<tr><td>${escapeHtml(reservation.reservationNumber || "")}</td><td>${escapeHtml(reservation.memberName || reservation.guestName || "고객")}</td><td>${escapeHtml(makeAdminRoomName(reservation))}</td><td>${formatAdminShortDate(reservation.checkInDate)}</td><td><span class="status ${adminStatusTone(reservation.reservationStatus)}">${escapeHtml(formatAdminStatus(reservation.reservationStatus))}</span></td></tr>`;
    }).join("");
    $("#checkinModalRoot").html(`
        <div class="admin-modal-backdrop">
            <div class="admin-modal">
                <div class="admin-modal-head">
                    <div><h2>오늘 체크인 전체</h2><p>${todayCheckIns.length}건의 체크인 예정/진행 예약</p></div>
                    <button type="button" class="modal-close" data-checkin-modal-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="admin-table-wrap modal-table-wrap">
                    <table class="admin-table"><thead><tr><th>예약번호</th><th>고객명</th><th>객실</th><th>체크인</th><th>상태</th></tr></thead><tbody>${rows}</tbody></table>
                </div>
            </div>
        </div>
    `);
}

function bulkAdminCheckinAction(action) {
    const ids = $(".checkin-row-check:checked").map(function () { return $(this).val(); }).get();
    if (!ids.length) {
        alert("처리할 예약을 선택해주세요.");
        return;
    }
    const label = action === "check-in" ? "체크인" : action === "check-out" ? "체크아웃" : "취소";
    const reservations = (window.ADMIN_CHECKIN_STATE && window.ADMIN_CHECKIN_STATE.reservations) || [];
    const selected = ids.map(function (id) {
        return reservations.find(function (reservation) { return String(reservation.sid) === String(id); });
    }).filter(Boolean);
    const processable = selected.filter(function (reservation) {
        return canBulkProcessReservation(reservation, action);
    });
    const skippedCount = selected.length - processable.length;
    if (!processable.length) {
        alert("선택된 예약 중 " + label + " 처리 가능한 예약이 없습니다.");
        return;
    }
    const confirmText = skippedCount
        ? skippedCount + "건은 " + label + " 처리할 수 없어 제외됩니다. 가능한 " + processable.length + "건만 처리할까요?"
        : processable.length + "건을 " + label + " 처리할까요?";
    if (!confirm(confirmText)) return;
    processAdminReservationBatch(processable.map(function (reservation) { return reservation.sid; }), action, 0, { success: 0, fail: 0 });
}

function canBulkProcessReservation(reservation, action) {
    const status = String(reservation && reservation.reservationStatus ? reservation.reservationStatus : "").toUpperCase();
    if (action === "check-in") {
        return status === "CONFIRMED" || status === "UPCOMING";
    }
    if (action === "check-out") {
        return status === "CHECKED_IN";
    }
    return status !== "CANCELLED" && status !== "NO_SHOW" && status !== "CHECKED_OUT" && status !== "COMPLETED";
}

function processAdminReservationBatch(ids, action, index, result) {
    if (index >= ids.length) {
        if (result && result.fail) {
            alert("처리 완료: 성공 " + result.success + "건, 실패 " + result.fail + "건");
        }
        loadAdminCheckinData();
        return;
    }
    const id = ids[index];
    let request;
    if (action === "check-in") {
        request = adminPatch("/reservation/" + id + "/check-in", {});
    } else if (action === "check-out") {
        request = adminPatch("/reservation/" + id + "/check-out", {});
    } else {
        request = adminPost("/reservation/cancel", { sid: Number(id), cancelReason: "관리자 일괄 취소" });
    }
    request.then(function () {
        result.success += 1;
    }, function () {
        result.fail += 1;
    }).always(function () {
        processAdminReservationBatch(ids, action, index + 1, result);
    });
}

function highlightCheckinCalendarPanel() {
    const panel = $("#checkinCalendarPanel");
    if (!panel.length) return;
    panel.removeClass("calendar-pulse");
    void panel[0].offsetWidth;
    panel.addClass("calendar-pulse");
    setTimeout(function () {
        panel.removeClass("calendar-pulse");
    }, 1800);
}

function renderAdminReservationPagination(page) {
    const totalPages = Number(page.totalPages || 1);
    const number = Number(page.number || 0);
    if (totalPages <= 1) return "";

    const buttons = [];
    for (let i = 0; i < totalPages; i++) {
        if (i > 4 && i < totalPages - 1) {
            if (i === 5) buttons.push('<span class="page-ellipsis">...</span>');
            continue;
        }
        buttons.push(`<button class="page-btn ${i === number ? "active" : ""}" type="button" data-reservation-page="${i}">${i + 1}</button>`);
    }
    return `<div class="admin-pagination">${buttons.join("")}</div>`;
}

function checkInAdminReservation(reservationId) {
    if (!reservationId || !confirm("이 예약을 체크인 처리할까요?")) return;
    $.ajax({
        url: window.StayNowConfig.apiUrl("/reservation/" + reservationId + "/check-in"),
        type: "PATCH",
        headers: adminAuthHeaders()
    }).done(function () {
        const selectedHotel = getSelectedAdminHotelFromMenu();
        if (ADMIN_CURRENT_PAGE === "dashboard") {
            loadAdminDashboardData();
            return;
        }
        if (ADMIN_CURRENT_PAGE === "checkin") {
            loadAdminCheckinData();
            return;
        }
        requestAdminReservationMetrics(selectedHotel);
        requestAdminReservations(selectedHotel, 0);
    }).fail(function (xhr) {
        alert((xhr.responseJSON && xhr.responseJSON.message) || "체크인 처리에 실패했습니다.");
    });
}

function checkOutAdminReservation(reservationId) {
    if (!reservationId || !confirm("이 예약을 체크아웃 처리할까요?")) return;
    adminPatch("/reservation/" + reservationId + "/check-out", {})
        .then(function () {
            if (ADMIN_CURRENT_PAGE === "checkin") {
                loadAdminCheckinData();
                return;
            }
            reloadCurrentAdminPage();
        }, function (xhr) {
            alert(getAdminAjaxMessage(xhr, "체크아웃 처리에 실패했습니다."));
        });
}

function cancelAdminReservation(reservationId) {
    if (!reservationId) return;
    const reason = prompt("예약 취소 사유를 입력해주세요.", "관리자 취소");
    if (!reason) return;
    adminPost("/reservation/cancel", { sid: Number(reservationId), cancelReason: reason })
        .then(function () {
            if (ADMIN_CURRENT_PAGE === "checkin") {
                loadAdminCheckinData();
                return;
            }
            reloadCurrentAdminPage();
        }, function (xhr) {
            alert(getAdminAjaxMessage(xhr, "예약 취소에 실패했습니다."));
        });
}

function downloadCheckinReport() {
    const state = window.ADMIN_CHECKIN_STATE;
    if (!state) {
        alert("아직 다운로드할 체크인 데이터가 없습니다.");
        return;
    }
    const rows = [
        ["예약번호", "고객명", "객실", "체크인", "체크아웃", "상태"],
        ...state.reservations.map(function (reservation) {
            return [
                reservation.reservationNumber || "",
                reservation.memberName || reservation.guestName || "고객",
                makeAdminRoomName(reservation),
                reservation.checkInDate || "",
                reservation.checkOutDate || "",
                formatAdminStatus(reservation.reservationStatus)
            ];
        })
    ];
    const today = new Date();
    downloadAdminCsv(rows, "staynow-checkin-" + today.getFullYear() + String(today.getMonth() + 1).padStart(2, "0") + String(today.getDate()).padStart(2, "0") + ".csv");
}

function openAdminQrCheckinModal() {
    stopAdminQrScanner();
    ADMIN_QR_PROCESSING = false;
    $("#checkinModalRoot").html(`
        <div class="admin-modal-backdrop">
            <div class="admin-modal admin-qr-modal">
                <div class="admin-modal-head">
                    <div>
                        <h2>QR 체크인</h2>
                        <p>예약 완료 화면의 QR 코드를 카메라로 스캔해 체크인 처리합니다.</p>
                    </div>
                    <button type="button" class="modal-close" data-admin-qr-close><i class="fa-solid fa-xmark"></i></button>
                </div>
                <div class="admin-qr-body">
                    <div class="admin-qr-status">
                        <span id="adminQrSupport"><i class="fa-solid fa-circle-notch fa-spin"></i> 카메라 확인 중</span>
                    </div>
                    <div class="admin-qr-camera">
                        <video id="adminQrVideo" playsinline muted></video>
                        <div class="admin-qr-placeholder">
                            <i class="fa-solid fa-camera"></i>
                            <strong>스캔 시작을 눌러주세요</strong>
                            <span>QR이 프레임 안에 들어오면 자동으로 체크인됩니다.</span>
                        </div>
                        <div class="admin-qr-frame" aria-hidden="true"></div>
                    </div>
                    <div id="adminQrResult" class="admin-qr-result empty">아직 스캔된 QR이 없습니다.</div>
                </div>
                <div class="admin-modal-actions">
                    <button type="button" class="admin-btn" id="adminQrStopBtn"><i class="fa-solid fa-stop"></i> 중지</button>
                    <button type="button" class="admin-btn primary" id="adminQrStartBtn"><i class="fa-solid fa-camera"></i> 스캔 시작</button>
                </div>
            </div>
        </div>
    `);
    initAdminQrSupport();
    $("#adminQrStartBtn").off("click.adminQr").on("click.adminQr", startAdminQrScanner);
    $("#adminQrStopBtn").off("click.adminQr").on("click.adminQr", stopAdminQrScanner);
    $("[data-admin-qr-close]").off("click.adminQr").on("click.adminQr", closeAdminQrCheckinModal);
}

function closeAdminQrCheckinModal() {
    stopAdminQrScanner();
    $("#checkinModalRoot").empty();
}

function initAdminQrSupport() {
    if ("BarcodeDetector" in window) {
        ADMIN_QR_DETECTOR = new BarcodeDetector({ formats: ["qr_code"] });
        $("#adminQrSupport").html('<i class="fa-solid fa-check"></i> 카메라 스캔 가능');
        return;
    }
    ADMIN_QR_DETECTOR = null;
    $("#adminQrSupport").html('<i class="fa-solid fa-triangle-exclamation"></i> 이 브라우저는 QR 카메라 스캔을 지원하지 않습니다.');
    $("#adminQrStartBtn").prop("disabled", true);
}

function startAdminQrScanner() {
    if (!ADMIN_QR_DETECTOR) {
        showAdminQrResult("이 브라우저는 QR 카메라 스캔을 지원하지 않습니다.", true);
        return;
    }
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        showAdminQrResult("카메라 API를 사용할 수 없습니다. 브라우저 권한과 HTTPS/localhost 환경을 확인해주세요.", true);
        return;
    }
    $("#adminQrStartBtn").prop("disabled", true).html('<i class="fa-solid fa-spinner fa-spin"></i> 카메라 여는 중');
    navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" }, audio: false })
        .then(function (stream) {
            ADMIN_QR_STREAM = stream;
            const video = document.getElementById("adminQrVideo");
            video.srcObject = stream;
            video.play();
            $(".admin-qr-camera").addClass("active");
            $("#adminQrStartBtn").html('<i class="fa-solid fa-camera"></i> 스캔 중');
            showAdminQrResult("QR 코드를 카메라 중앙에 맞춰주세요.", false, true);
            ADMIN_QR_TIMER = window.setInterval(scanAdminQrFrame, 650);
        })
        .catch(function () {
            $("#adminQrStartBtn").prop("disabled", false).html('<i class="fa-solid fa-camera"></i> 스캔 시작');
            showAdminQrResult("카메라를 시작하지 못했습니다. 브라우저 카메라 권한을 확인해주세요.", true);
        });
}

function stopAdminQrScanner() {
    if (ADMIN_QR_TIMER) {
        window.clearInterval(ADMIN_QR_TIMER);
        ADMIN_QR_TIMER = null;
    }
    if (ADMIN_QR_STREAM) {
        ADMIN_QR_STREAM.getTracks().forEach(function (track) { track.stop(); });
        ADMIN_QR_STREAM = null;
    }
    $(".admin-qr-camera").removeClass("active");
    $("#adminQrStartBtn").prop("disabled", false).html('<i class="fa-solid fa-camera"></i> 스캔 시작');
}

function scanAdminQrFrame() {
    if (ADMIN_QR_PROCESSING || !ADMIN_QR_DETECTOR) return;
    const video = document.getElementById("adminQrVideo");
    if (!video || video.readyState < 2) return;
    ADMIN_QR_DETECTOR.detect(video).then(function (codes) {
        if (!codes.length) return;
        processAdminQrCheckin(codes[0].rawValue);
    }).catch(function () {
        showAdminQrResult("QR 인식 중 오류가 발생했습니다. 카메라를 다시 시작해주세요.", true);
    });
}

function processAdminQrCheckin(qrValue) {
    const value = String(qrValue || "").trim();
    if (!value) return;
    ADMIN_QR_PROCESSING = true;
    showAdminQrResult("QR을 인식했습니다. 체크인 처리 중입니다.", false, true);
    adminPatch("/reservation/check-in/qr", { qrValue: value })
        .then(function (reservation) {
            stopAdminQrScanner();
            showAdminQrResult(
                "<strong>체크인이 완료되었습니다.</strong><br>" +
                "예약번호: " + escapeHtml(reservation.reservationNumber || "-") + "<br>" +
                "호텔: " + escapeHtml(reservation.hotelName || "호텔명 없음") + "<br>" +
                "투숙객: " + escapeHtml(reservation.guestName || reservation.memberName || "투숙객"),
                false
            );
            loadAdminCheckinData();
        }, function (xhr) {
            showAdminQrResult(getAdminAjaxMessage(xhr, "체크인 처리에 실패했습니다."), true);
        })
        .always(function () {
            ADMIN_QR_PROCESSING = false;
        });
}

function showAdminQrResult(message, isError, isMuted) {
    $("#adminQrResult")
        .removeClass("empty error muted")
        .toggleClass("error", Boolean(isError))
        .toggleClass("muted", Boolean(isMuted))
        .html(message);
}

function loadDashboardRoomsAndReviews(baseData) {
    const hotels = baseData.hotels.slice(0, 1);
    const roomRequests = hotels.map(function (hotel) {
        return adminGet("/hotel/inroom/" + hotel.sid).then(function (rooms) {
            return {
                hotel,
                rooms: asArray(rooms)
            };
        }, function () {
            return {
                hotel,
                rooms: []
            };
        });
    });
    const reviewRequests = hotels.slice(0, 8).map(function (hotel) {
        return adminGet("/review/search?hotelId=" + hotel.sid + "&page=0&size=50&sort=createdAt,desc")
            .then(function (page) {
                return asPageContent(page);
            }, function () {
                return [];
            });
    });

    $.when.apply($, roomRequests.concat(reviewRequests)).always(function () {
        const values = Array.prototype.slice.call(arguments);
        const roomBundles = values.slice(0, roomRequests.length).map(normalizeDeferredValue);
        const reviewGroups = values.slice(roomRequests.length).map(normalizeDeferredValue);
        const rooms = roomBundles.flatMap(function (bundle) {
            return (bundle.rooms || []).map(function (room) {
                return Object.assign({}, room, {
                    hotelId: bundle.hotel.sid,
                    hotelName: bundle.hotel.hotelName
                });
            });
        });
        const reviews = reviewGroups.flat();

        renderDashboardData(Object.assign({}, baseData, { rooms, reviews }));
    });
}

function renderDashboardData(data) {
    const reservations = data.reservations || [];
    const payments = data.payments || [];
    const rooms = data.rooms || [];
    const reviews = data.reviews || [];
    const roomSummary = buildRoomSummary(rooms, reservations);
    const revenueSummary = buildPaymentRevenueSummary(payments);
    const reviewSummary = buildReviewSummary(reviews);
    const statSummary = buildReservationStats(data.stats, reservations);
    ADMIN_DASHBOARD_STATE = {
        selectedHotel: data.selectedHotel || null,
        reservations,
        payments,
        rooms,
        reviews,
        roomSummary,
        revenueSummary,
        reviewSummary,
        statSummary
    };

    renderDashboardMetrics(statSummary, roomSummary, revenueSummary, reviewSummary);
    renderDashboardSales(revenueSummary);
    renderDashboardRoomPanel(roomSummary);
    renderDashboardReservationPanel(reservations);
    renderDashboardSchedule(reservations);
    renderDashboardTasks(statSummary, reviews, reservations);
    renderDashboardRoomMap(roomSummary.rooms);
}

function renderDashboardMetrics(stats, rooms, revenue, reviews) {
    const occupancy = rooms.total > 0 ? Math.round((rooms.occupied / rooms.total) * 1000) / 10 : 0;
    const cards = [
        ["오늘 예약", formatAdminNumber(stats.todayNewReservations) + "건", "fa-calendar-check", "이번 달 " + formatAdminNumber(stats.monthlyReservations) + "건 중", ""],
        ["체크인 예정", formatAdminNumber(stats.todayCheckIns) + "팀", "fa-right-to-bracket", "객실 " + rooms.total + "실 기준", "warn"],
        ["객실 점유율", occupancy.toFixed(1) + "%", "fa-bed", rooms.total + "실 중 " + rooms.occupied + "실", "", occupancy],
        ["이번 달 매출", formatAdminCompactWon(revenue.monthTotal), "fa-wallet", "결제 " + revenue.monthCount + "건", ""],
        ["평균 평점", reviews.average ? reviews.average.toFixed(1) + "점" : "0.0점", "fa-star", "리뷰 " + reviews.count + "개", ""]
    ];

    $("#dashboardMetrics").html(cards.map(function ([label, value, icon, trend, tone, progressPercent]) {
        return `
            <article class="metric-card">
                <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${escapeHtml(trend)}</span></div>
                <div class="metric-label">${escapeHtml(label)}</div>
                <div class="metric-value">${escapeHtml(value)}</div>
                ${renderMetricProgress(progressPercent)}
            </article>
        `;
    }).join(""));
}

function renderDashboardSales(summary) {
    const months = summary.months;
    const max = Math.max.apply(null, months.map(function (item) { return item.total; }).concat([1]));
    const bars = months.map(function (item) {
        const height = item.total > 0 ? Math.max(8, Math.round((item.total / max) * 100)) : 0;
        return `
            <div class="admin-sales-month">
                <div class="admin-sales-bar" style="height:${height}%"></div>
                <strong>${item.label}</strong>
                <span>${formatAdminCompactWon(item.total)}</span>
            </div>
        `;
    }).join("");

    $("#dashboardSalesPanel").html(
        panelHead("월별 매출 현황", "결제 완료 및 환불 상태 반영 기준") +
        `<div class="admin-sales-chart">${bars}</div>`
    );
}

function renderDashboardRoomPanel(summary) {
    const occupancy = summary.total > 0 ? Math.round((summary.occupied / summary.total) * 100) : 0;
    $("#dashboardRoomPanel").html(
        panelHead("객실 현황", "예약 상태 기준") +
        `<div class="ring" style="background:conic-gradient(var(--admin-blue) 0 ${occupancy}%, var(--admin-green) ${occupancy}% ${Math.min(100, occupancy + Math.round((summary.reserved / Math.max(1, summary.total)) * 100))}%, #e7eef6 0 100%)"><strong>${occupancy}%</strong></div>` +
        `<div class="task-list">
            ${task("사용중 " + summary.inUse + "실", "")}
            ${task("예약완료 " + summary.reserved + "실", "")}
            ${task("공실 " + summary.available + "실", "")}
        </div>`
    );
}

function renderDashboardReservationPanel(reservations) {
    const rows = reservations.slice(0, 8).map(function (reservation) {
        return [
            "<strong>" + escapeHtml(reservation.reservationNumber || ("RSV-" + reservation.sid)) + "</strong>",
            escapeHtml(reservation.memberName || reservation.guestName || "고객"),
            escapeHtml(makeAdminRoomName(reservation)),
            formatAdminShortDate(reservation.checkInDate),
            formatAdminShortDate(reservation.checkOutDate),
            "<strong>" + formatAdminWon(reservation.totalAmount) + "</strong>",
            '<span class="status ' + adminStatusTone(reservation.reservationStatus) + '">' + escapeHtml(formatAdminStatus(reservation.reservationStatus)) + "</span>",
            renderReservationActions(reservation, reservation.reservationStatus === "CONFIRMED" || reservation.reservationStatus === "UPCOMING")
        ];
    });

    $("#dashboardReservationPanel").html(
        panelHead("최근 예약 현황", "최신 예약 " + reservations.length + "건 중 상위 8건") +
        (rows.length ? simpleTable(["예약번호", "고객명", "객실", "체크인", "체크아웃", "금액", "상태", "액션"], rows, true) : emptyAdminState("예약 데이터가 없습니다."))
    );
}

function renderDashboardSchedule(reservations) {
    const todayItems = reservations.filter(function (reservation) {
        return isToday(reservation.checkInDate) || isToday(reservation.checkOutDate);
    }).slice(0, 5);
    const rows = todayItems.map(function (reservation) {
        const isCheckout = isToday(reservation.checkOutDate);
        const label = isCheckout ? "체크아웃" : "체크인";
        const time = isCheckout ? "오전 11:00" : "오후 3:00";

        return `<div class="timeline-item admin-schedule-row">
            <span class="mini-icon"><i class="fa-solid ${isCheckout ? "fa-right-from-bracket" : "fa-right-to-bracket"}"></i></span>
            <div class="schedule-copy">
                <strong>${escapeHtml(reservation.guestName || reservation.memberName || "고객")} · ${escapeHtml(makeAdminRoomName(reservation))}</strong>
                <span>${time} · ${label}</span>
            </div>
            <span class="status ${isCheckout ? "orange" : "blue"}">${label}</span>
        </div>`;
    }).join("");

    $("#dashboardSchedulePanel").html(
        panelHead("오늘 일정", "체크인 · 체크아웃") +
        `<div class="timeline">${rows || emptyAdminState("오늘 체크인/체크아웃 일정이 없습니다.")}</div>`
    );
}

function renderDashboardTasks(stats, reviews, reservations) {
    const cancelRequests = reservations.filter(function (reservation) {
        return reservation.reservationStatus === "CANCELLED" || reservation.reservationStatus === "NO_SHOW";
    }).length;
    const pending = Number(stats.pendingCount || 0);
    const lowReviews = reviews.filter(function (review) {
        return Number(review.rating || 0) <= 2;
    }).length;
    const items = [
        pending > 0 ? task("승인 대기 예약 " + pending + "건", "warn") : task("승인 대기 예약 없음", ""),
        cancelRequests > 0 ? task("취소/노쇼 예약 " + cancelRequests + "건 확인", "danger") : task("취소 처리 대기 없음", ""),
        lowReviews > 0 ? task("낮은 평점 리뷰 " + lowReviews + "건 확인", "warn") : task("낮은 평점 리뷰 없음", "")
    ].join("");

    $("#dashboardTaskPanel").html(panelHead("처리 필요 항목", "예약/리뷰 기준") + `<div class="task-list">${items}</div>`);
}

function renderDashboardRoomMap(rooms) {
    $("#dashboardRoomMapPanel").html(
        panelHead("객실 현황 맵", "사용중/예약완료/공실") +
        (rooms.length ? buildDynamicRoomMap(rooms) : emptyAdminState("객실 데이터가 없습니다."))
    );
}

function buildReservationStats(stats, reservations) {
    return {
        todayNewReservations: reservations.filter(function (item) { return isToday(item.createdAt); }).length,
        todayCheckIns: reservations.filter(function (item) { return isToday(item.checkInDate); }).length,
        todayCheckOuts: reservations.filter(function (item) { return isToday(item.checkOutDate); }).length,
        monthlyReservations: reservations.filter(function (item) { return isThisMonth(item.createdAt || item.checkInDate); }).length,
        pendingCount: reservations.filter(function (item) { return item.reservationStatus === "PENDING"; }).length
    };
}

function renderAdminHotelSelector(hotels, selectedHotel) {
    const switcher = $("#adminHotelSwitcher");
    const toggle = $("#adminHotelToggle");
    const label = $("#adminHotelName");
    const menu = $("#adminHotelMenu");
    const search = $("#adminHotelSearch");
    const options = $("#adminHotelOptions");

    if (!switcher.length || !toggle.length || !label.length || !menu.length || !search.length || !options.length) {
        return;
    }

    if (ADMIN_CURRENT_PAGE === "promotions") {
        renderPromotionHotelScope();
        return;
    }

    switcher.removeClass("global-scope");
    toggle.prop("disabled", false);

    if (!hotels.length) {
        label.text("등록된 호텔 없음");
        switcher.attr("aria-disabled", "true");
        toggle.attr("aria-disabled", "true");
        search.val("");
        options.empty();
        menu.prop("hidden", true);
        return;
    }

    label.text((selectedHotel && selectedHotel.hotelName) || "호텔명 없음");
    switcher.attr("aria-disabled", "false");
    toggle.attr("aria-disabled", "false");
    renderAdminHotelOptions(hotels, selectedHotel, search.val());

    menu.off("click.adminHotel").on("click.adminHotel", ".admin-hotel-option", function (event) {
        event.stopPropagation();
        localStorage.setItem(ADMIN_SELECTED_HOTEL_KEY, String($(this).data("hotelId")));
        switcher.removeClass("open");
        menu.prop("hidden", true);
        reloadCurrentAdminPage();
    });

    search.off("click.adminHotelSearch keydown.adminHotelSearch input.adminHotelSearch")
        .on("click.adminHotelSearch keydown.adminHotelSearch", function (event) {
            event.stopPropagation();
        })
        .on("input.adminHotelSearch", function (event) {
            event.stopPropagation();
            renderAdminHotelOptions(hotels, selectedHotel, $(this).val());
        });

    switcher.off("click.adminHotel").on("click.adminHotel", function (event) {
        event.stopPropagation();
        if ($(event.target).closest(".admin-hotel-menu").length || switcher.attr("aria-disabled") === "true") {
            return;
        }
        const willOpen = !switcher.hasClass("open");
        switcher.toggleClass("open", willOpen);
        menu.prop("hidden", !willOpen);
        if (willOpen) {
            setTimeout(function () {
                search.trigger("focus");
            }, 0);
        }
    });

    $(document).off("click.adminHotelMenu").on("click.adminHotelMenu", function () {
        switcher.removeClass("open");
        menu.prop("hidden", true);
    });
}

function renderPromotionHotelScope() {
    const switcher = $("#adminHotelSwitcher");
    const toggle = $("#adminHotelToggle");
    const label = $("#adminHotelName");
    const menu = $("#adminHotelMenu");
    const search = $("#adminHotelSearch");
    const options = $("#adminHotelOptions");

    if (!switcher.length || !toggle.length || !label.length) {
        return;
    }

    label.text("호텔 선택 불가");
    switcher.attr("aria-disabled", "true").addClass("global-scope");
    toggle.attr("aria-disabled", "true").prop("disabled", true);
    search.val("");
    options.empty();
    menu.prop("hidden", true);
    switcher.off("click.adminHotel");
    menu.off("click.adminHotel");
    search.off("click.adminHotelSearch keydown.adminHotelSearch input.adminHotelSearch");
}

function getHotelLocationText(hotel) {
    return hotel.location || hotel.address || hotel.region || hotel.hotelLocation || "위치 정보 없음";
}

function mergeAdminHotels(primaryHotels, fallbackHotels) {
    const map = new Map();

    primaryHotels.concat(fallbackHotels).forEach(function (hotel) {
        if (!hotel || hotel.sid == null) {
            return;
        }
        const key = String(hotel.sid);
        const prev = map.get(key) || {};
        map.set(key, Object.assign({}, prev, hotel));
    });

    return Array.from(map.values());
}

function getSelectedAdminHotel(hotels) {
    if (!hotels.length) {
        return null;
    }

    const storedId = localStorage.getItem(ADMIN_SELECTED_HOTEL_KEY);
    const selected = hotels.find(function (hotel) {
        return String(hotel.sid) === String(storedId);
    }) || hotels[0];

    localStorage.setItem(ADMIN_SELECTED_HOTEL_KEY, String(selected.sid));
    return selected;
}

function renderAdminHotelOptions(hotels, selectedHotel, keyword) {
    const query = String(keyword || "").trim().toLowerCase();
    const filtered = hotels.filter(function (hotel) {
        const text = [
            hotel.hotelName,
            getHotelLocationText(hotel)
        ].join(" ").toLowerCase();
        return !query || text.includes(query);
    });

    $("#adminHotelOptions").html(
        filtered.length
            ? filtered.map(function (hotel) {
                const isActive = selectedHotel && String(selectedHotel.sid) === String(hotel.sid);
                return `<button class="admin-hotel-option ${isActive ? "active" : ""}" type="button" data-hotel-id="${escapeHtml(hotel.sid)}">
                    <strong>${escapeHtml(hotel.hotelName || "호텔명 없음")}</strong>
                    <span>${escapeHtml(getHotelLocationText(hotel))}</span>
                </button>`;
            }).join("")
            : '<div class="admin-hotel-empty">검색 결과가 없습니다.</div>'
    );
}

function getSelectedAdminHotelFromMenu() {
    const selectedId = localStorage.getItem(ADMIN_SELECTED_HOTEL_KEY);
    const selectedName = $("#adminHotelName").text();
    const selected = ADMIN_RESERVATION_STATE && ADMIN_RESERVATION_STATE.selectedHotel;

    if (selected && String(selected.sid) === String(selectedId)) {
        return selected;
    }
    return selectedId ? { sid: selectedId, hotelName: selectedName } : null;
}

function reloadCurrentAdminPage() {
    if (ADMIN_CURRENT_PAGE === "reservations") {
        loadAdminReservationData(0);
        return;
    }
    if (ADMIN_CURRENT_PAGE === "dashboard") {
        loadAdminDashboardData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "guests") {
        loadAdminGuestData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "rooms") {
        loadAdminRoomData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "checkin") {
        loadAdminCheckinData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "sales") {
        loadAdminSalesData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "promotions") {
        loadAdminPromotionData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "reviews") {
        loadAdminReviewData();
        return;
    }
    if (ADMIN_CURRENT_PAGE === "settings") {
        loadAdminHotelManageData();
    }
}

function filterReservationsByHotel(reservations, hotel) {
    if (!hotel) {
        return [];
    }

    return reservations.filter(function (reservation) {
        const hotelIds = [
            reservation.hotelId,
            reservation.hotelSid,
            reservation.hotel && reservation.hotel.sid,
            reservation.hotel && reservation.hotel.hotelId,
            reservation.roomHotelId,
            reservation.room && reservation.room.hotelId,
            reservation.room && reservation.room.hotel && reservation.room.hotel.sid
        ].filter(function (value) { return value != null; });

        return hotelIds.some(function (hotelId) {
            return String(hotelId) === String(hotel.sid);
        });
    });
}

function buildPaymentRevenueSummary(payments) {
    const validPayments = payments.filter(function (payment) {
        return isRevenuePayment(payment);
    });
    const monthPayments = validPayments.filter(function (payment) {
        return isThisMonth(getPaymentRevenueDate(payment));
    });
    const months = [];
    const now = new Date();

    for (let i = 5; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
        months.push({
            year: date.getFullYear(),
            month: date.getMonth(),
            label: String(date.getMonth() + 1) + "월",
            total: 0
        });
    }

    validPayments.forEach(function (payment) {
        const revenueDate = parseAdminDate(getPaymentRevenueDate(payment));
        if (!revenueDate) return;
        const target = months.find(function (item) {
            return item.year === revenueDate.getFullYear() && item.month === revenueDate.getMonth();
        });
        if (target) target.total += getPaymentRevenueAmount(payment);
    });

    return {
        monthTotal: monthPayments.reduce(function (sum, payment) { return sum + getPaymentRevenueAmount(payment); }, 0),
        monthCount: monthPayments.length,
        monthMaxTotal: Math.max.apply(null, months.map(function (item) { return item.total; }).concat([0])),
        months
    };
}

function isRevenuePayment(payment) {
    if (!payment || payment.deleted) return false;
    return payment.paymentStatus === "COMPLETED" || payment.paymentStatus === "PARTIALLY_REFUNDED";
}

function getPaymentRevenueDate(payment) {
    return payment.paidAt || payment.createdAt;
}

function getPaymentRevenueAmount(payment) {
    if (!isRevenuePayment(payment)) return 0;
    return Number(payment.paymentAmount || payment.amount || 0);
}

function getAdminReservationKeys(reservations) {
    const keys = new Set();
    reservations.forEach(function (reservation) {
        [
            reservation.sid,
            reservation.reservationId,
            reservation.reservationSid,
            reservation.reservationNumber
        ].forEach(function (value) {
            if (value != null) keys.add(String(value));
        });
    });
    return keys;
}

function getAdminPaymentReservationKeys(payment) {
    return [
        payment.reservationId,
        payment.reservationSid,
        payment.reservationNumber,
        payment.reservation && payment.reservation.sid,
        payment.reservation && payment.reservation.reservationNumber
    ].filter(function (value) { return value != null; }).map(String);
}

function buildReviewSummary(reviews) {
    const valid = reviews.filter(function (review) {
        return !review.deleted && Number(review.rating || 0) > 0;
    });
    const sum = valid.reduce(function (total, review) {
        return total + Number(review.rating || 0);
    }, 0);

    return {
        count: valid.length,
        average: valid.length ? sum / valid.length : 0
    };
}

function buildRoomSummary(rooms, reservations) {
    const todayReservations = reservations.filter(function (reservation) {
        return overlapsToday(reservation.checkInDate, reservation.checkOutDate) &&
            !["CANCELLED", "NO_SHOW", "CHECKED_OUT", "COMPLETED"].includes(reservation.reservationStatus);
    });
    const roomStates = rooms.map(function (room) {
        const matched = todayReservations.find(function (reservation) {
            return String(reservation.roomId) === String(room.sid);
        });
        const status = matched && matched.reservationStatus === "CHECKED_IN"
            ? "use"
            : matched
                ? "done"
                : "";

        return Object.assign({}, room, {
            adminStatus: status,
            adminGuestName: matched ? (matched.guestName || matched.memberName || "") : "",
            adminReservationStatus: matched ? matched.reservationStatus : null
        });
    });
    const inUse = roomStates.filter(function (room) { return room.adminStatus === "use"; }).length;
    const reserved = roomStates.filter(function (room) { return room.adminStatus === "done"; }).length;

    return {
        total: roomStates.length,
        inUse,
        reserved,
        occupied: inUse + reserved,
        available: Math.max(0, roomStates.length - inUse - reserved),
        rooms: roomStates
    };
}

function buildDynamicRoomMap(rooms) {
    const grouped = {};
    rooms.forEach(function (room) {
        const floor = room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || 1;
        if (!grouped[floor]) grouped[floor] = [];
        grouped[floor].push(room);
    });

    return `<div class="room-map">${Object.keys(grouped).sort(function (a, b) { return Number(b) - Number(a); }).map(function (floor) {
        const cells = grouped[floor].sort(function (a, b) {
            return Number(a.roomNumber || 0) - Number(b.roomNumber || 0);
        }).map(function (room) {
            const cls = room.adminStatus || "";
            const label = cls === "use" ? "사용중" : cls === "done" ? "예약완료" : "공실";
            return `<div class="room-cell ${cls}">
                ${escapeHtml(room.roomNumber || room.roomName || "-")}
                <span>${label}${room.adminGuestName ? " · " + escapeHtml(room.adminGuestName) : ""}</span>
            </div>`;
        }).join("");
        return `<div class="floor-row"><div class="floor-label">${floor}F</div>${cells}</div>`;
    }).join("")}</div>`;
}

function adminGet(path) {
    return $.ajax({
        url: window.StayNowConfig.apiUrl(path),
        type: "GET",
        headers: adminAuthHeaders()
    }).then(unwrapApiResponse);
}

function adminPost(path, payload) {
    return $.ajax({
        url: window.StayNowConfig.apiUrl(path),
        type: "POST",
        contentType: "application/json",
        headers: adminAuthHeaders(),
        data: JSON.stringify(payload || {})
    }).then(unwrapApiResponse);
}

function adminPatch(path, payload) {
    return $.ajax({
        url: window.StayNowConfig.apiUrl(path),
        type: "PATCH",
        contentType: "application/json",
        headers: adminAuthHeaders(),
        data: JSON.stringify(payload || {})
    }).then(unwrapApiResponse);
}

function adminGetSafe(path, fallback) {
    return adminGet(path).then(null, function () {
        return fallback;
    });
}

function adminPostSafe(path, payload, fallback) {
    return adminPost(path, payload).then(null, function () {
        return fallback;
    });
}

function adminAuthHeaders() {
    const auth = getAdminAuth();
    return auth && auth.token ? { Authorization: auth.token } : {};
}

function unwrapApiResponse(response) {
    return response && Object.prototype.hasOwnProperty.call(response, "data") ? response.data : response;
}

function getAdminAjaxMessage(xhr, fallback) {
    if (xhr && xhr.status === 0) {
        return fallback + " 서버 연결 또는 CORS 설정을 확인해주세요.";
    }
    if (xhr && xhr.responseJSON) {
        return xhr.responseJSON.data || xhr.responseJSON.message || fallback;
    }
    if (xhr && xhr.responseText) {
        try {
            const parsed = JSON.parse(xhr.responseText);
            return parsed.data || parsed.message || parsed.error || parsed.detail || xhr.responseText;
        } catch (e) {
            return xhr.responseText;
        }
    }
    return fallback;
}

function normalizeAjaxResult(result) {
    if (Array.isArray(result) && result.length === 3 && typeof result[1] === "string" && result[2] && typeof result[2] === "object") {
        return result[0];
    }
    return result;
}

function normalizeDeferredValue(value) {
    if (Array.isArray(value) && value.length === 3 && typeof value[1] === "string" && value[2] && typeof value[2] === "object") {
        return value[0];
    }
    return value;
}

function asArray(value) {
    return Array.isArray(value) ? value : [];
}

function asPageContent(value) {
    if (!value) return [];
    if (Array.isArray(value)) return value;
    if (value.data) return asPageContent(value.data);
    return Array.isArray(value.content) ? value.content : [];
}

function makeAdminHotelSearchRequest() {
    return {
        location: null,
        checkIn: null,
        checkOut: null,
        adult: null,
        child: null,
        minPrice: null,
        maxPrice: null,
        star: null,
        roomTypeIds: []
    };
}

function renderDashboardFailure(message) {
    $("#dashboardError").prop("hidden", false).text(message);
    $(".admin-loading").text("데이터를 불러오지 못했습니다.");
}

function downloadRoomReport() {
    if (!ADMIN_ROOM_STATE) {
        alert("아직 다운로드할 객실 데이터가 없습니다.");
        return;
    }

    const state = ADMIN_ROOM_STATE;
    const rows = [
        ["호텔", state.selectedHotel ? state.selectedHotel.hotelName : ""],
        ["다운로드 시각", getAdminNowLabel()],
        [],
        ["객실번호", "객실명", "객실 유형", "층", "상태", "예약자", "체크인", "체크아웃", "예약 가능", "기본 가격"],
        ...state.rooms.map(function (room) {
            const reservation = room.adminReservation || {};
            return [
                room.roomNumber || "",
                room.roomName || "",
                room.roomTypeTitle || "",
                room.floor || Math.floor(Number(room.roomNumber || 0) / 100) || "",
                room.adminStatusLabel || "",
                reservation.memberName || reservation.guestName || "",
                reservation.checkInDate || "",
                reservation.checkOutDate || "",
                room.adminAvailable ? "가능" : "불가",
                room.roomPrice || 0
            ];
        })
    ];

    const today = new Date();
    downloadAdminCsv(rows, "staynow-rooms-" + today.getFullYear() + String(today.getMonth() + 1).padStart(2, "0") + String(today.getDate()).padStart(2, "0") + ".csv");
}

function downloadDashboardReport() {
    if (!ADMIN_DASHBOARD_STATE) {
        alert("아직 다운로드할 대시보드 데이터가 없습니다. 새로고침 후 다시 시도해주세요.");
        return;
    }

    const state = ADMIN_DASHBOARD_STATE;
    const hotelName = state.selectedHotel ? state.selectedHotel.hotelName : "전체 호텔";
    const rows = [
        ["항목", "값"],
        ["호텔", hotelName],
        ["기준 시각", getAdminNowLabel()],
        ["오늘 예약", state.statSummary.todayNewReservations + "건"],
        ["체크인 예정", state.statSummary.todayCheckIns + "팀"],
        ["체크아웃 예정", state.statSummary.todayCheckOuts + "팀"],
        ["이번 달 결제 건수", state.revenueSummary.monthCount + "건"],
        ["이번 달 매출", state.revenueSummary.monthTotal],
        ["전체 객실", state.roomSummary.total + "실"],
        ["사용/예약 객실", state.roomSummary.occupied + "실"],
        ["공실", state.roomSummary.available + "실"],
        ["리뷰 수", state.reviewSummary.count + "개"],
        ["평균 평점", (state.reviewSummary.average || 0).toFixed(1)]
    ];
    const reservationRows = [
        [],
        ["예약번호", "고객명", "객실", "체크인", "체크아웃", "상태", "금액"],
        ...state.reservations.map(function (reservation) {
            return [
                reservation.reservationNumber || ("RSV-" + reservation.sid),
                reservation.memberName || reservation.guestName || "고객",
                makeAdminRoomName(reservation),
                reservation.checkInDate || "",
                reservation.checkOutDate || "",
                formatAdminStatus(reservation.reservationStatus),
                reservation.totalAmount || 0
            ];
        })
    ];
    const csv = "\uFEFF" + rows.concat(reservationRows).map(function (row) {
        return row.map(escapeCsvCell).join(",");
    }).join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
    const link = document.createElement("a");
    const date = new Date();

    link.href = URL.createObjectURL(blob);
    link.download = `staynow-dashboard-${String(date.getFullYear())}${String(date.getMonth() + 1).padStart(2, "0")}${String(date.getDate()).padStart(2, "0")}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
}

function downloadReservationReport() {
    if (!ADMIN_RESERVATION_STATE) {
        alert("아직 다운로드할 예약 데이터가 없습니다.");
        return;
    }

    const state = ADMIN_RESERVATION_STATE;
    const rows = [
        ["예약번호", "고객명", "호텔", "객실", "체크인", "체크아웃", "박수", "인원", "상태", "금액"],
        ...state.reservations.map(function (reservation) {
            return [
                reservation.reservationNumber || ("RSV-" + reservation.sid),
                reservation.memberName || reservation.guestName || "고객",
                reservation.hotelName || "",
                makeAdminRoomName(reservation),
                reservation.checkInDate || "",
                reservation.checkOutDate || "",
                reservation.totalNights || "",
                "성인 " + (reservation.adults || 0) + " / 아동 " + (reservation.children || 0),
                formatAdminStatus(reservation.reservationStatus),
                reservation.totalAmount || 0
            ];
        })
    ];
    const csv = "\uFEFF" + rows.map(function (row) {
        return row.map(escapeCsvCell).join(",");
    }).join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
    const link = document.createElement("a");
    const date = new Date();

    link.href = URL.createObjectURL(blob);
    link.download = `staynow-reservations-${String(date.getFullYear())}${String(date.getMonth() + 1).padStart(2, "0")}${String(date.getDate()).padStart(2, "0")}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
}

function downloadSalesReport() {
    if (!ADMIN_SALES_STATE || !ADMIN_SALES_STATE.summary) {
        alert("아직 다운로드할 매출 데이터가 없습니다.");
        return;
    }

    const state = ADMIN_SALES_STATE;
    const summary = state.summary;
    const hotelName = state.selectedHotel ? state.selectedHotel.hotelName : "전체 호텔";
    const rows = [
        ["StayNow 매출 분석 리포트"],
        ["호텔", hotelName],
        ["기준 월", summary.monthLabel],
        ["다운로드 시각", getAdminNowLabel()],
        [],
        ["요약 항목", "값"],
        ["선택 월 총 매출", summary.totalRevenue],
        ["결제 완료 건수", summary.paymentCount],
        ["취소 제외 예약", summary.validReservationCount],
        ["순매출", summary.netRevenue],
        [],
        ["일별 매출"],
        ["날짜", "결제 건수", "예약 건수", "총 매출", "취소 예약액", "순매출"],
        ...summary.dailyRows.map(function (row) {
            return [row.dateLabel, row.paymentCount, row.reservationCount, row.total, row.cancelledAmount, row.netTotal];
        }),
        [],
        ["객실 유형별 매출"],
        ["객실 유형", "매출", "비중", "결제 건수"],
        ...summary.roomTypes.map(function (item) {
            return [item.label, item.total, item.percent + "%", item.count];
        }),
        [],
        ["매출 상위 예약"],
        ["예약번호", "고객명", "객실", "체크인", "체크아웃", "금액"],
        ...summary.topReservations.map(function (item) {
            return [item.reservationNumber, item.guestName, item.roomName, item.checkInDate || "", item.checkOutDate || "", item.amount];
        })
    ];

    downloadAdminCsv(rows, "staynow-sales-" + summary.target.year + String(summary.target.month + 1).padStart(2, "0") + ".csv");
}

function downloadAdminCsv(rows, filename) {
    const csv = "\ufeff" + rows.map(function (row) {
        return row.map(escapeCsvCell).join(",");
    }).join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
}

function escapeCsvCell(value) {
    const text = String(value == null ? "" : value);
    return /[",\n]/.test(text) ? '"' + text.replaceAll('"', '""') + '"' : text;
}

function emptyAdminState(message) {
    return `<div class="empty-admin-state">${escapeHtml(message)}</div>`;
}

function makeAdminRoomName(reservation) {
    return [
        reservation.roomName || reservation.roomTypeTitle || "객실",
        reservation.roomNumber ? reservation.roomNumber + "호" : ""
    ].filter(Boolean).join(" · ");
}

function formatAdminStatus(status) {
    const labels = {
        PENDING: "승인대기",
        CONFIRMED: "예약확정",
        UPCOMING: "예약확정",
        CHECKED_IN: "체크인",
        CHECKED_OUT: "체크아웃",
        COMPLETED: "완료",
        CANCELLED: "취소됨",
        NO_SHOW: "노쇼"
    };
    return labels[status] || status || "상태 없음";
}

function adminStatusTone(status) {
    if (status === "CHECKED_IN" || status === "CONFIRMED" || status === "UPCOMING") return "";
    if (status === "CHECKED_OUT" || status === "COMPLETED") return "blue";
    if (status === "PENDING") return "orange";
    if (status === "CANCELLED" || status === "NO_SHOW") return "red";
    return "";
}

function formatAdminWon(value) {
    return "₩" + formatAdminNumber(value || 0);
}

function formatAdminCompactWon(value) {
    const number = Number(value || 0);
    if (number >= 100000000) {
        return "₩" + (number / 100000000).toFixed(1).replace(/\.0$/, "") + "억";
    }
    if (number >= 10000) {
        return "₩" + Math.round(number / 10000).toLocaleString() + "만";
    }
    return formatAdminWon(number);
}

function formatAdminNumber(value) {
    return Number(value || 0).toLocaleString();
}

function formatAdminShortDate(value) {
    if (!value) return "-";
    const text = String(value).slice(5, 10).replace("-", ".");
    return text || "-";
}

function formatAdminNights(reservation) {
    const nights = Number(reservation.totalNights || 0);
    if (nights > 0) return nights + "박";
    const start = parseAdminDate(reservation.checkInDate);
    const end = parseAdminDate(reservation.checkOutDate);
    if (!start || !end) return "-";
    return Math.max(1, Math.round((end - start) / 86400000)) + "박";
}

function parseAdminDate(value) {
    if (!value) return null;
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
}

function isToday(value) {
    const date = parseAdminDate(value);
    const today = new Date();
    return Boolean(date) &&
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth() &&
        date.getDate() === today.getDate();
}

function isThisMonth(value) {
    const date = parseAdminDate(value);
    const today = new Date();
    return Boolean(date) &&
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth();
}

function isSameAdminMonth(value, year, month) {
    const date = parseAdminDate(value);
    return Boolean(date) &&
        date.getFullYear() === Number(year) &&
        date.getMonth() === Number(month);
}

function overlapsToday(startValue, endValue) {
    const start = parseAdminDate(startValue);
    const end = parseAdminDate(endValue);
    if (!start || !end) return false;
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    const todayEnd = new Date(todayStart);
    todayEnd.setDate(todayEnd.getDate() + 1);
    return start < todayEnd && end > todayStart;
}
