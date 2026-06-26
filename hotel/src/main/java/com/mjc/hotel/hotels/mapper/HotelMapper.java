package com.mjc.hotel.hotels.mapper;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotelMapper {

    public static Hotel clone(HotelRequestDto hotel, boolean sid, HotelType type, HotelPhoto photo) {
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
                .build();

        if (sid) {
            clone.setSid(hotel.getSid());
        }

        return clone;
    }
}
