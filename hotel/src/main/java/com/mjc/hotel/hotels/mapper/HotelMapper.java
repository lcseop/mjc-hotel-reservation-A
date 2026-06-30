package com.mjc.hotel.hotels.mapper;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotelMapper {

    public static Hotel clone(Hotel origin, HotelRequestDto hotel, boolean sid, HotelType type, HotelPhoto photo) {
        if (hotel == null || photo == null
                || type == null || hotel.getHotelName() == null
                || hotel.getHotelPrice() == null || hotel.getLocation() == null) {
            return null;
        }
        if (sid && hotel.getSid() == null) {
            return null;
        }
        Hotel clone = Hotel
                .builder()
                .photo(photo)
                .type(type)
                .hotelName(hotel.getHotelName())
                .hotelPrice(hotel.getHotelPrice())
                .location(hotel.getLocation())
                .starRating(hotel.getStarRating())
                .description(hotel.getDescription())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .build();

        if (sid) {
            clone.setSid(hotel.getSid());
            clone.setCreatedAt(origin.getCreatedAt());
            clone.setUpdatedAt(origin.getUpdatedAt());
            clone.setDeletedAt(origin.getDeletedAt());
            clone.setDeleted(origin.getDeleted());
        }

        return clone;
    }

    public static HotelResponseDto response(Hotel hotel) {
        return HotelResponseDto
                .builder()
                .sid(hotel.getSid())
                .typeTitle(hotel.getType().getTitle())
                .photoPath(hotel.getPhoto().getImagePath())
                .hotelName(hotel.getHotelName())
                .hotelPrice(hotel.getHotelPrice())
                .location(hotel.getLocation())
                .starRating(hotel.getStarRating())
                .description(hotel.getDescription())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .build();
    }
}
