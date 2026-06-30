package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelInAmenitiesDto;
import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelInAmenities;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelInAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class HotelInAmenitiesService {
    @Autowired
    private HotelInAmenitiesRepository hotelInAmenitiesRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelAmenitiesRepository hotelAmenitiesRepository;

    public HotelInAmenitiesDto insert(HotelInAmenities ame) {
        if (ame.getHotel() == null || ame.getAmenities() == null) return null;
        HotelInAmenities insert = HotelInAmenities
                .builder()
                .hotel(ame.getHotel())
                .amenities(ame.getAmenities())
                .build();
        return toDto(hotelInAmenitiesRepository.save(insert), false);
    }

    public HotelInAmenitiesDto update(HotelInAmenitiesDto ame) {
        if (ame.getHotelId() == null || ame.getAmenitiesId() == null
        || ame.getSid() == null) return null;
        HotelInAmenities origin = hotelInAmenitiesRepository.findById(ame.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        Hotel hotel = hotelRepository.findById(ame.getHotelId()).orElseThrow();
        HotelAmenities hotelAmenities = hotelAmenitiesRepository.findById(ame.getAmenitiesId()).orElseThrow();
        HotelInAmenities clone = HotelInAmenities
                .builder()
                .sid(ame.getSid())
                .hotel(hotel)
                .amenities(hotelAmenities)
                .build();
        return toDto(hotelInAmenitiesRepository.save(clone), true);
    }

    public HotelInAmenitiesDto delete(Long id) {
        HotelInAmenities ame = hotelInAmenitiesRepository.findById(id).orElseThrow();
        if (ame.getDeleted() != null && ame.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        ame.setDeleted(true);
        ame.setDeletedAt(LocalDateTime.now());
        return toDto(hotelInAmenitiesRepository.save(ame), true);
    }

    public HotelInAmenitiesDto findById(Long id) {
        HotelInAmenities ame = hotelInAmenitiesRepository.findById(id).orElseThrow();
        if (ame.getDeleted() != null && ame.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(ame, true);
    }

    private HotelInAmenitiesDto toDto(HotelInAmenities ame, boolean sid) {
        if (sid) {
            return HotelInAmenitiesDto
                    .builder()
                    .sid(ame.getSid())
                    .hotelId(ame.getHotel().getSid())
                    .hotelName(ame.getHotel().getHotelName())
                    .amenitiesId(ame.getAmenities().getSid())
                    .amenitiesTitle(ame.getAmenities().getTitle())
                    .build();
        } else {
            return HotelInAmenitiesDto
                    .builder()
                    .hotelId(ame.getHotel().getSid())
                    .hotelName(ame.getHotel().getHotelName())
                    .amenitiesId(ame.getAmenities().getSid())
                    .amenitiesTitle(ame.getAmenities().getTitle())
                    .build();
        }
    }
}
