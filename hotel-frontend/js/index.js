$(function () {

    init();

});

function init() {
    cardHover();
    travelTypeSelect();
    categoryTab();
    searchValidation();
    dealButton();
    recommendButton();
    scrollAnimation();
}

/* ===========================
   여행 유형 선택
=========================== */

function travelTypeSelect() {

    $(".travel-card").click(function () {

        $(".travel-card").removeClass("selected");

        $(this).addClass("selected");

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

    $(".search-btn").click(function () {

        let location = $(".location input").val();

        let checkIn = $(".search-item input[type='date']").eq(0).val();

        let checkOut = $(".search-item input[type='date']").eq(1).val();

        if (location == "") {

            alert("여행지를 입력하세요.");

            return;

        }

        if (checkIn == "") {

            alert("체크인 날짜를 선택하세요.");

            return;

        }

        if (checkOut == "") {

            alert("체크아웃 날짜를 선택하세요.");

            return;

        }

        alert("검색 기능은 Spring Boot와 연결될 예정입니다.");

    });

}

/* ===========================
   특가 예약
=========================== */

function dealButton() {

    $(".deal-body button").click(function () {

        let hotel = $(this).siblings("h3").text();

        alert(hotel + "\n예약 페이지로 이동합니다.");

    });

}

/* ===========================
   추천 버튼
=========================== */

function recommendButton() {

    let title = [

        "제주에서 힐링하기",
        "부산 오션뷰 여행",
        "서울 호캉스",
        "강릉 바다 여행",
        "여름 휴양지 BEST"

    ];

    let desc = [

        "AI가 분석한 최고의 제주 호텔입니다.",

        "부산 인기 호텔을 추천합니다.",

        "도심 속 럭셔리 호텔을 만나보세요.",

        "동해가 보이는 숙소를 추천합니다.",

        "이번 시즌 인기 여행지입니다."

    ];

    $(".recommend-text button").click(function () {

        let random = Math.floor(Math.random() * title.length);

        $(".recommend-text h2").text(title[random]);

        $(".recommend-text p").text(desc[random]);

    });

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

        url: "http://localhost:33000/api/hotel/pop4",
        type: "GET",

        success: function (result) {

            drawPopularHotels(result.data);

        },

        error: function () {

            console.log("호텔 정보를 불러오지 못했습니다.");

        }

    });

}

function drawPopularHotels(hotels) {

    $(".hotel-grid").empty();

    $.each(hotels, function(index, hotel){

        let card = `
            <div class="hotel-card">

                <div class="hotel-image">
                    <img src="${hotel.firstImage}">
                </div>

                <div class="hotel-body">

                    <div class="title-row">
                        <h3>${hotel.hotelName}</h3>
                        <span class="price">₩${hotel.price.toLocaleString()}</span>
                    </div>

                    <p>${hotel.location}</p>

                    <div class="card-footer">
                        <span>⭐ ${hotel.rating}</span>
                    </div>

                </div>

            </div>
        `;

        $(".hotel-grid").append(card);

    });

}

