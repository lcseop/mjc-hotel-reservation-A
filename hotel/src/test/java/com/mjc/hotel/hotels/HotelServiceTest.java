package com.mjc.hotel.hotels;

import com.mjc.hotel.hotels.entity.*;
import com.mjc.hotel.hotels.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class HotelServiceTest {
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelAmenitiesRepository hotelAmenitiesRepository;
    @Autowired
    private HotelInAmenitiesRepository hotelInAmenitiesRepository;
    @Autowired
    private HotelPhotoRepository hotelPhotoRepository;
    @Autowired
    private HotelTypeRepository hotelTypeRepository;

    @DisplayName("hotelTestData")
    @Test
    @Commit
    public void addHotelTest() {
        HotelAmenities hotelAmenities = HotelAmenities
                .builder()
                .title("무료 와이파이")
                .description("전 구역 이용 가능")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        HotelPhoto hotelPhoto = HotelPhoto
                .builder()
                .imagePath("https://cf.bstatic.com/xdata/images/hotel/max1024x768/27025252.jpg?k=cec48daabc79a9a6a85840aa1cf63b268fc689835c8cebe0abda01975ea156dc&o=")
                .build();

        hotelPhotoRepository.save(hotelPhoto);

        HotelType hotelType = HotelType
                .builder()
                .title("호텔")
                .build();

        hotelTypeRepository.save(hotelType);


        Hotel hotel = Hotel
                .builder()
                .type(hotelType)
                .photo(hotelPhoto)
                .hotelName("골든 서울 호텔")
                .hotelPrice(300000)
                .location("서울 강서구 염창동 공항대로 663")
                .starRating(4)
                .description("골든 서울 호텔은 서울 강서구 염창동 공항대로에 위치한 호텔입니다. 골든")
                .build();

        hotelRepository.save(hotel);
    }

    @Test
    @Commit
    public void amenitiesAdd() {
        HotelAmenities hotelAmenities = HotelAmenities
                .builder()
                .title("루프탑 수영장")
                .description("연중무휴 운영")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("피트니스 센터")
                .description("24시간 이용")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("럭셔리 스파")
                .description("예약 후 이용")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("미슐랭 레스토랑")
                .description("조식·중식·석식 운영")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("발렛 파킹")
                .description("유료 · 24시간")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("24시간 컨시어지")
                .description("한·영·일 지원")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);

        hotelAmenities = HotelAmenities
                .builder()
                .title("루프탑 바")
                .description("PM 6 ~ AM 2")
                .build();

        hotelAmenitiesRepository.save(hotelAmenities);
    }

    @Test
    @Commit
    public void hotelInAmenitiesAdd() {
        Hotel hotel = hotelRepository.findById(1L).orElseThrow();
        HotelAmenities hotelAmenities = hotelAmenitiesRepository.findById(1L).orElseThrow();

        HotelInAmenities hotelInAmenities = HotelInAmenities
                .builder()
                .hotel(hotel)
                .amenities(hotelAmenities)
                .build();

        hotelInAmenitiesRepository.save(hotelInAmenities);
    }
}
