package com.mjc.hotel.hotels.mapper;

import com.mjc.hotel.hotels.dto.HotelInPhotoResponseDto;
import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelType;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    private HotelMapper() {}

    public static Hotel clone(Hotel origin, HotelRequestDto hotel, boolean sid, HotelType type) {
        if (hotel == null
                || type == null || hotel.getHotelName() == null
                || hotel.getHotelPrice() == null || hotel.getLocation() == null) {
            throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        }
        if (sid && hotel.getSid() == null) {
            throw new IllegalArgumentException("sid가 입력되지 않았습니다.");
        }
        Hotel clone = Hotel
                .builder()
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
                .hotelName(hotel.getHotelName())
                .hotelPrice(hotel.getHotelPrice())
                .location(hotel.getLocation())
                .starRating(hotel.getStarRating())
                .description(hotel.getDescription())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .build();
    }

    public static HotelInPhotoResponseDto photoResponse(Hotel hotel) {
        return HotelInPhotoResponseDto
                .builder()
                .sid(hotel.getSid())
                .typeTitle(hotel.getType().getTitle())
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
