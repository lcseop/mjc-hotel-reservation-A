const ADMIN_TODAY_LABEL = "2025년 7월 14일 (월) · 오전 10:42";

const ADMIN_PAGES = [
    { id: "dashboard", label: "대시보드", file: "admin-dashboard.html", icon: "fa-table-cells-large", group: "메인" },
    { id: "reservations", label: "예약 관리", file: "admin-reservations.html", icon: "fa-calendar-check", group: "메인", badge: "12" },
    { id: "checkin", label: "체크인 현황", file: "admin-checkin.html", icon: "fa-clock", group: "메인", badge: "3" },
    { id: "guests", label: "고객 관리", file: "admin-guests.html", icon: "fa-users", group: "메인" },
    { id: "rooms", label: "객실 관리", file: "admin-rooms.html", icon: "fa-bed", group: "객실 · 요금" },
    { id: "rates", label: "요금 설정", file: "admin-rates.html", icon: "fa-tags", group: "객실 · 요금" },
    { id: "promotions", label: "프로모션", file: "admin-promotions.html", icon: "fa-percent", group: "객실 · 요금" },
    { id: "sales", label: "매출 분석", file: "admin-sales.html", icon: "fa-chart-bar", group: "분석 · 리포트" },
    { id: "reviews", label: "리뷰 관리", file: "admin-reviews.html", icon: "fa-star", group: "분석 · 리포트", badge: "8" },
    { id: "settlement", label: "정산 리포트", file: "admin-settlement.html", icon: "fa-file-invoice", group: "분석 · 리포트" },
    { id: "settings", label: "시스템 설정", file: "admin-settings.html", icon: "fa-gear", group: "설정" }
];

const ADMIN_TOP_NAV = ["dashboard", "reservations", "guests", "rooms", "sales", "reviews", "settings"];

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
    $("#adminApp").html(`
        <header class="admin-topbar">
            <a class="admin-brand" href="admin-dashboard.html">
                <span class="brand-icon"><i class="fa-solid fa-hotel"></i></span>
                <span class="brand-copy"><strong>StayNow</strong><span>관리자 시스템</span></span>
            </a>
            <nav class="admin-top-nav">${ADMIN_TOP_NAV.map(id => navTop(id, pageId)).join("")}</nav>
            <div class="admin-tools">
                <input class="quick-search" type="search" placeholder="빠른 검색..." disabled>
                <span class="bell"><i class="fa-regular fa-bell"></i></span>
                <div class="admin-profile"><span class="avatar">관</span><span><strong>${escapeHtml(auth.name || "김관리자")}</strong><span>Super Admin</span></span></div>
            </div>
        </header>
        <aside class="admin-sidebar">
            <div class="hotel-switcher"><span class="mini-icon"><i class="fa-solid fa-hotel"></i></span><span>그랜드 서울</span><i class="fa-solid fa-chevron-down" style="margin-left:auto;color:#94a3b8"></i></div>
            ${renderSideGroups(pageId)}
        </aside>
        <main class="admin-main">
            <section class="page-head">
                <div><h1>${page.label}${page.id === "dashboard" ? '<span class="trend">● 실시간 업데이트 중</span>' : ""}</h1><p>${pageSubtitle(page.id)}</p></div>
                <div class="head-actions">${headActions(page.id)}</div>
            </section>
            <section id="adminContent">${renderPage(page.id)}</section>
            <div class="admin-note">현재 화면은 기능 연결 전 임시 관리자 UI입니다. 버튼, 필터, 차트는 다음 단계에서 백엔드 API와 연결할 수 있도록 고정 데이터로 구성했습니다.</div>
        </main>
    `);
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

function pageSubtitle(id) {
    const copy = {
        dashboard: `${ADMIN_TODAY_LABEL}`,
        reservations: `${ADMIN_TODAY_LABEL} · 전체 예약 현황 및 승인 관리`,
        checkin: `${ADMIN_TODAY_LABEL} · 체크인 / 체크아웃 운영`,
        guests: "회원, 투숙 이력, 등급, 문의 상태 관리",
        rooms: "실시간 객실 상태, 청소, 점검, 재고 관리",
        rates: `${ADMIN_TODAY_LABEL} · 객실 유형별 요금 및 시즌 정책 관리`,
        promotions: `${ADMIN_TODAY_LABEL} · 특가 및 할인 프로모션 관리`,
        sales: "그랜드 서울 · 2025년 7월 14일 기준",
        reviews: "리뷰 답변, 신고, 평점 추이 관리",
        settlement: `${ADMIN_TODAY_LABEL} · 객실 유형별 매출 및 수수료 정산 현황`,
        settings: "권한, 호텔 정보, 외부 연동, 운영 정책 설정"
    };
    return copy[id] || ADMIN_TODAY_LABEL;
}

function headActions(id) {
    const byPage = {
        reservations: [["엑셀 다운로드", "fa-download"], ["인쇄", "fa-print"], ["예약 등록", "fa-plus", "primary"]],
        rates: [["변경 이력", "fa-clock"], ["내보내기", "fa-download"], ["요금 추가", "fa-plus", "primary"]],
        promotions: [["필터", "fa-sliders"], ["내보내기", "fa-download"], ["프로모션 생성", "fa-plus", "primary"]],
        sales: [["이번 달 (07.01 ~ 07.14)", "fa-calendar"], ["리포트 다운로드", "fa-download"], ["공유", "fa-share-nodes", "primary"]],
        settlement: [["2025년 7월", "fa-calendar"], ["엑셀 다운로드", "fa-download"], ["정산 확정", "fa-check-double", "primary"]],
        checkin: [["캘린더 보기", "fa-calendar"], ["내보내기", "fa-download"], ["신규 예약", "fa-plus", "primary"]]
    };
    const buttons = byPage[id] || [["이번 달 (7월)", "fa-calendar"], ["리포트 다운로드", "fa-download"], ["예약 등록", "fa-plus", "primary"]];
    return buttons.map(([text, icon, type]) => `<button class="admin-btn ${type || ""}" type="button"><i class="fa-solid ${icon}"></i> ${text}</button>`).join("");
}

function renderPage(id) {
    const pages = {
        dashboard: renderDashboard,
        reservations: renderReservations,
        checkin: renderCheckin,
        guests: renderGuests,
        rooms: renderRooms,
        rates: renderRates,
        promotions: renderPromotions,
        sales: renderSales,
        reviews: renderReviews,
        settlement: renderSettlement,
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
        ${metrics([["오늘 신규 예약", "47건", "fa-calendar-check", "+12.4%"], ["체크인 예정", "18팀", "fa-right-to-bracket", "오늘", "warn"], ["취소 요청", "1건", "fa-circle-xmark", "처리필요", "danger"], ["이번 달 예약", "284건", "fa-calendar-days", "+8.2%"]])}
        ${filterPanel("예약번호, 고객명 검색...", ["전체 상태", "07.01 ~ 07.31", "전체 객실", "전체 채널"])}
        <div class="panel">${reservationTable(true)}</div>
    `;
}

function renderCheckin() {
    return `
        ${metrics([["오늘 전체 예약", "47건", "fa-calendar-check", "+3건"], ["오늘 체크인", "12건", "fa-right-to-bracket", "+2건"], ["오늘 체크아웃", "9건", "fa-right-from-bracket", "-1건", "warn"], ["승인 대기", "3건", "fa-clock", "확인필요", "danger"]])}
        <div class="grid split" style="margin-top:22px">
            <div class="panel">${tabs(["전체 47", "예약확정 32", "체크인 12", "체크아웃 9", "취소 4"])}${checkinTable()}</div>
            <div class="grid">${checkinPanel()}${weekPanel()}${urgentPanel()}</div>
        </div>
    `;
}

function renderGuests() {
    return `
        ${metrics([["총 고객", "12,482명", "fa-users", "+8.4%"], ["재방문율", "42.6%", "fa-repeat", "+3.1%"], ["신규 가입", "218명", "fa-user-plus", "이번 달"], ["문의 대기", "6건", "fa-comments", "처리필요", "danger"]])}
        ${filterPanel("고객명, 이메일, 전화번호 검색...", ["전체 등급", "최근 투숙", "포인트 보유", "문의 상태"])}
        <div class="grid split">
            <div class="panel">${panelHead("고객 목록", "임시 데이터")} ${simpleTable(["고객명", "이메일", "예약", "포인트", "등급", "상태"], [["김민준", "minjun@example.com", "12건", "14,364P", "VIP", "활성"], ["박서연", "seoyeon@example.com", "7건", "8,200P", "VVIP", "활성"], ["이지현", "jihyun@example.com", "2건", "1,120P", "일반", "활성"], ["최준혁", "jun@example.com", "4건", "3,400P", "일반", "휴면"]])}</div>
            <div class="panel">${panelHead("고객 관리 메모", "다음 단계")}<div class="task-list">${task("등급/포인트 정책은 Member 담당 API 확인 필요", "warn")}${task("개인정보 수정은 권한 로그가 필요", "")}${task("마케팅 수신 동의 UI는 현재 필수 아님", "")}</div></div>
        </div>
    `;
}

function renderRooms() {
    return `
        ${metrics([["전체 객실", "120실", "fa-bed", "기준"], ["사용중", "73실", "fa-door-closed", "61%"], ["청소중", "8실", "fa-broom", "4실 증가", "warn"], ["점검 필요", "3실", "fa-screwdriver-wrench", "처리필요", "danger"]])}
        <div class="panel">${panelHead("객실 현황 맵", "층별 실시간 상태")}${roomMap()}</div>
        <div class="grid split" style="margin-top:22px">
            <div class="panel">${panelHead("하우스키핑 작업", "청소 및 점검")} ${simpleTable(["객실", "상태", "담당", "예정 시간", "메모"], [["506", "청소중", "홍길순", "11:20", "어메니티 보충"], ["204", "청소중", "김청소", "12:00", "침구 교체"], ["305", "점검", "이정비", "14:30", "에어컨 확인"]])}</div>
            <div class="panel">${panelHead("구현 의견", "객실 관리")}<div class="task-list">${task("Room 상태 필드가 없다면 백엔드 확장이 필요합니다.", "warn")}${task("청소 담당자 배정은 별도 Staff/Task 도메인이 있어야 자연스럽습니다.", "")}</div></div>
        </div>
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
        ${metrics([["진행중 프로모션", "5개", "fa-percent", "+2개"], ["프로모션 예약 수", "284건", "fa-ticket", "+18.4%"], ["총 할인 제공액", "₩38.2M", "fa-coins", "+22.1%", "danger"], ["전환율", "34.7%", "fa-wand-magic-sparkles", "+3.2%"]])}
        ${filterPanel("프로모션명 검색...", ["전체", "진행중", "예정", "종료", "일시중지"])}
        <div class="panel">${simpleTable(["프로모션명", "유형", "할인율/금액", "적용 객실", "기간", "예약수", "전환율", "상태"], [["여름 성수기 특가", "할인율", "최대 30%", "디럭스 전 객실", "2025.07.01 ~ 08.31", "142건", "38.2%", "진행중"], ["주중 특별 할인", "정액할인", "₩30,000", "스탠다드 전 객실", "2025.07.07 ~ 09.30", "68건", "29.5%", "진행중"], ["장기 투숙 패키지", "패키지", "20% + 조식", "전 객실", "2025.06.01 ~ 12.31", "211건", "42.1%", "진행중"], ["멤버십 전용 특가", "할인율", "10~25%", "프리미엄 · 스위트", "2025.07.01 ~ 10.31", "34건", "18.3%", "일시중지"]])}</div>
    `;
}

function renderSales() {
    return `
        ${metrics([["이번 달 총 매출", "₩186,450,000", "fa-money-bill", "+18.3%"], ["객실 점유율", "87.4%", "fa-bed", "+22.1%"], ["평균 객단가", "₩655,000", "fa-users", "+5.7%", "warn"], ["재방문율", "42.6%", "fa-repeat", "-2.1%", "danger"]])}
        <div style="margin-top:22px">${salesPanel("월별 매출 추이", "최근 7개월 · 단위: 만원")}</div>
        <div class="grid split" style="margin-top:22px">
            <div class="panel">${panelHead("객실 유형별 매출", "총 ₩163,820,000 · 7월 기준")}${barLines([["프리미엄 스위트", 38, "₩62,400,000"], ["디럭스 더블/킹", 29, "₩48,250,000"], ["스탠다드 트윈", 19, "₩31,100,000"], ["비즈니스 더블", 8, "₩13,580,000"], ["슈페리어 싱글", 5, "₩8,490,000"]])}</div>
            <div class="panel">${panelHead("매출 상위 예약", "이번 달 최고 매출 예약")}<div class="task-list">${task("박서연 · 프리미엄 스위트 502호 ₩1,800,000", "")}${task("강지훈 · 프리미엄 스위트 501호 ₩1,200,000", "")}</div></div>
        </div>
    `;
}

function renderReviews() {
    return `
        ${metrics([["평균 평점", "9.4점", "fa-star", "+0.2"], ["신규 리뷰", "127건", "fa-message", "이번 달"], ["답변 대기", "8건", "fa-reply", "처리필요", "warn"], ["신고 리뷰", "2건", "fa-triangle-exclamation", "확인필요", "danger"]])}
        <div class="panel" style="margin-top:22px">${panelHead("리뷰 통계", "항목별 만족도")}<div class="review-score"><div class="big-score">9.4</div>${barLines([["청결도", 96, "9.6"], ["서비스", 94, "9.4"], ["위치", 97, "9.7"], ["시설", 91, "9.1"]])}<div class="task-list">${task("5점 65%", "")}${task("4점 24%", "")}${task("3점 이하 11%", "warn")}</div></div></div>
        <div class="panel" style="margin-top:22px">${tabs(["전체", "사진포함", "답변 대기", "신고됨"])}<div class="review-row"><span class="avatar">김</span><div><strong>객실이 깨끗하고 위치가 좋았어요.</strong><p>그랜드 서울 호텔 · 2025.07.08</p></div><span class="status orange">답변 대기</span></div><div class="review-row"><span class="avatar">박</span><div><strong>체크인 응대가 빨랐습니다.</strong><p>파크하얏트 서울 · 2025.07.06</p></div><span class="status">답변 완료</span></div></div>
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
        <div class="grid settings-grid">
            ${settingCard("호텔 기본 정보", "이름, 주소, 연락처, 대표 이미지")}
            ${settingCard("관리자 권한", "ADMIN / STAFF / VIEWER 역할")}
            ${settingCard("예약 정책", "취소, 체크인, 노쇼 기준")}
            ${settingCard("알림 설정", "메일, SMS, 관리자 알림")}
            ${settingCard("외부 연동", "TourAPI, Kakao Map, 결제 PG")}
            ${settingCard("보안 로그", "접속 이력, 권한 변경 기록")}
        </div>
        <div class="panel" style="margin-top:22px">${panelHead("구현 판단", "필수와 보류")}<div class="task-list">${task("권한 관리는 반드시 필요합니다. 관리자 화면 전체가 ADMIN 전용이어야 합니다.", "")}${task("결제 PG 정산 자동화는 현재 가짜 결제 구조라 상세 구현은 보류가 맞습니다.", "warn")}${task("외부 OTA 채널 관리는 실제 OTA 연동 전까지 더미로 충분합니다.", "warn")}</div></div>
    `;
}

function metrics(items, count) {
    const cols = count || items.length;
    return `<div class="grid stats-grid cols-${Math.min(cols, 5)}">${items.map(([label, value, icon, trend, tone]) => `
        <article class="metric-card">
            <div class="metric-top"><span class="metric-icon"><i class="fa-solid ${icon}"></i></span><span class="trend ${tone || ""}">${trend}</span></div>
            <div class="metric-label">${label}</div><div class="metric-value">${value}</div><div class="progress"><i style="width:${label.length * 7 + 32}%"></i></div>
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

function reservationTable(withChecks) {
    const headers = withChecks ? ["", "예약번호", "고객명", "객실", "체크인", "체크아웃", "금액", "상태", "액션"] : ["예약번호", "고객명", "객실", "체크인", "체크아웃", "금액", "상태", "액션"];
    const rows = reservations.map(row => {
        const cells = [`<strong>${row[0]}</strong>`, row[1], row[2], row[3], row[4], `<strong>${row[5]}</strong>`, `<span class="status ${row[7]}">${row[6]}</span>`, actionBtns()];
        if (withChecks) cells.unshift('<input type="checkbox" disabled>');
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
    return `<div class="panel">${panelHead("처리 필요 항목", "5건")}<div class="task-list">${task("취소 요청 · 정하은 고객", "danger")}${task("미답변 리뷰 · 최근 7일 이내", "warn")}${task("고객 문의 · 미확인 문의 메시지", "")}</div></div>`;
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
    const floors = [["5F", ["501 use", "502 done", "503", "504", "505 wait", "506 clean"]], ["4F", ["401 use", "402 use", "403 done", "404 wait", "405", "406 clean", "407 done", "408 use"]], ["3F", ["301", "302 wait", "303 use", "304 use", "305 clean", "306 done", "307", "308 done"]], ["2F", ["201 use", "202 wait", "203 done", "204 clean", "205 use", "206", "207 done"]]];
    return `<div class="room-map">${floors.map(([floor, rooms]) => `<div class="floor-row"><div class="floor-label">${floor}</div>${rooms.map(room => {
        const [num, cls = ""] = room.split(" ");
        const label = cls === "use" ? "사용중" : cls === "done" ? "예약완료" : cls === "wait" ? "체크아웃대기" : cls === "clean" ? "청소중" : "공실";
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
