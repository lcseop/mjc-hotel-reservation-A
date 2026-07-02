package com.mjc.hotel.room;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.entity.*;
import com.mjc.hotel.room.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class RoomServiceTest {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomPhotoRepository roomPhotoRepository;
    @Autowired
    private RoomTagRepository roomTagRepository;
    @Autowired
    private RoomInTagRepository roomInTagRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private HotelRepository hotelRepository;

    @DisplayName("roomTestData")
    @Test
    @Commit
    public void addRoomTests() {
        RoomTag roomTag = RoomTag
                .builder()
                .title("강남뷰")
                .build();

        roomTagRepository.save(roomTag);

        RoomType roomType = RoomType
                .builder()
                .title("스탠다드")
                .build();

        roomTypeRepository.save(roomType);

        Hotel hotel = hotelRepository.findById(1L).orElseThrow();

        Room room = Room
                .builder()
                .hotelId(hotel)
                .roomTypeId(roomType)
                .roomName("스탠다드 룸")
                .roomPrice(80000)
                .roomNumber(305)
                .floor(3)
                .area(30)
                .maximumPeople(3)
                .checkInTime(15)
                .checkOutTime(11)
                .parking("발렛파킹 주차")
                .pet(RoomPetAndSmokeEnum.BAN)
                .smoke(RoomPetAndSmokeEnum.BAN)
                .idCard(RoomIdCardEnum.OPTIONAL)
                .build();

        roomRepository.save(room);

        RoomPhoto roomPhoto = RoomPhoto
                .builder()
                .room(room)
                .imagePath("https://img.lottehotel.com/cms/asset/2025/01/31/3287/180708-1-2000-acc-guro-city.webp")
                .build();

        roomPhotoRepository.save(roomPhoto);

        RoomInTag roomInTag = RoomInTag
                .builder()
                .room(room)
                .tag(roomTag)
                .build();

        roomInTagRepository.save(roomInTag);
    }
}
