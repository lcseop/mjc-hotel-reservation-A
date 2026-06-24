package com.mjc.hotel.hotels;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
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
                .amenities(hotelAmenities)
                .hotelName("골든 서울 호텔")
                .hotelPrice(300000)
                .location("서울 강서구 염창동 공항대로 663")
                .starRating(4)
                .description("골든 서울 호텔은 서울 강서구 염창동 공항대로에 위치한 호텔입니다. 골든")
                .build();

        hotelRepository.save(hotel);

    }
}
