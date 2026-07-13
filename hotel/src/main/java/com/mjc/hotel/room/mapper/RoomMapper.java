package com.mjc.hotel.room.mapper;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.room.dto.RoomRequestDto;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomType;

import java.util.List;

public class RoomMapper {
    public static Room clone(Room origin, RoomRequestDto room, boolean sid, Hotel hotel, RoomType type) {
        if (room == null || type == null
                || hotel == null || room.getRoomName() == null
                || room.getRoomPrice() == null || room.getRoomNumber() == null
                || room.getFloor() == null || room.getArea() == null
                || room.getMaximumPeople() == null) {
            throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        }
        if (sid && room.getSid() == null) {
            throw new IllegalArgumentException("sid가 입력되지 않았습니다.");
        }
        Room clone = Room
                .builder()
                .hotelId(hotel)
                .roomTypeId(type)
                .roomName(room.getRoomName())
                .roomPrice(room.getRoomPrice())
                .roomAvailable(room.getRoomAvailable() != null ? room.getRoomAvailable() : true)
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .area(room.getArea())
                .maximumPeople(room.getMaximumPeople())
                .checkInTime(room.getCheckInTime())
                .checkOutTime(room.getCheckOutTime())
                .parking(room.getParking())
                .pet(room.getPet())
                .smoke(room.getSmoke())
                .idCard(room.getIdCard())
                .build();

        if (sid) {
            clone.setSid(room.getSid());
            clone.setCreatedAt(origin.getCreatedAt());
            clone.setUpdatedAt(origin.getUpdatedAt());
            clone.setDeletedAt(origin.getDeletedAt());
            clone.setDeleted(origin.getDeleted());
        }

        return clone;
    }

    public static RoomResponseDto response(Room room) {
        return RoomResponseDto
                .builder()
                .sid(room.getSid())
                .hotel(HotelMapper.photoResponse(room.getHotelId()))
                .roomTypeId(room.getRoomTypeId().getSid())
                .roomTypeTitle(room.getRoomTypeId().getTitle())
                .roomName(room.getRoomName())
                .roomPrice(room.getRoomPrice())
                .roomAvailable(room.getRoomAvailable() != null ? room.getRoomAvailable() : true)
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .area(room.getArea())
                .maximumPeople(room.getMaximumPeople())
                .checkInTime(room.getCheckInTime())
                .checkOutTime(room.getCheckOutTime())
                .parking(room.getParking())
                .pet(room.getPet())
                .smoke(room.getSmoke())
                .idCard(room.getIdCard())
                .build();
    }
}
