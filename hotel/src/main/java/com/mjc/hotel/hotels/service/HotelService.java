package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelRequestDto;
import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelAmenitiesRepository hotelAmenitiesRepository;
    @Autowired
    private HotelPhotoRepository hotelPhotoRepository;
    @Autowired
    private HotelTypeRepository hotelTypeRepository;

    @Transactional
    public HotelResponseDto insert(HotelRequestDto hotel) {
        HotelAmenities amenities = hotelAmenitiesRepository.findById(hotel.getAmenitiesId()).orElseThrow();
        HotelPhoto photo = hotelPhotoRepository.findById(hotel.getPhotoId()).orElseThrow();
        HotelType type = hotelTypeRepository.findById(hotel.getTypeId()).orElseThrow();

        Hotel insert = HotelMapper.clone(hotel, false, type, photo, amenities);

        Hotel saved = hotelRepository.save(insert);

        HotelResponseDto dto = HotelResponseDto
                .builder()
                .sid(saved.getSid())
                .typeTitle(type.getTitle())
                .photoPath(photo.getImagePath())
                .amenitiesTitle(amenities.getTitle())
                .amenitiesDescription(amenities.getDescription())
                .hotelName(saved.getHotelName())
                .hotelPrice(saved.getHotelPrice())
                .location(saved.getLocation())
                .starRating(saved.getStarRating())
                .description(saved.getDescription())
                .build();
        return dto;
    }
}
