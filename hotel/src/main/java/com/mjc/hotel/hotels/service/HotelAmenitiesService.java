package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelAmenitiesDto;
import com.mjc.hotel.hotels.dto.HotelInAmenitiesDto;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelInAmenities;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelInAmenitiesRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class HotelAmenitiesService {
    @Autowired
    private HotelAmenitiesRepository hotelAmenitiesRepository;
    @Autowired
    private HotelInAmenitiesRepository hotelInAmenitiesRepository;

    @Transactional
    public HotelAmenitiesDto insert(HotelAmenitiesDto amenities) {
        if (amenities.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelAmenities clone = HotelAmenities
                .builder()
                .title(amenities.getTitle())
                .description(amenities.getDescription())
                .build();
        return toDto(hotelAmenitiesRepository.save(clone), true);
    }

    @Transactional
    public HotelAmenitiesDto update(HotelAmenitiesDto amenities) {
        if (amenities.getTitle() == null || amenities.getSid() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelAmenities origin = hotelAmenitiesRepository.findById(amenities.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        HotelAmenities clone =  HotelAmenities
                .builder()
                .sid(amenities.getSid())
                .title(amenities.getTitle())
                .description((amenities.getDescription() == null) ? origin.getDescription() : amenities.getDescription())
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(hotelAmenitiesRepository.save(clone), true);
    }

    @Transactional
    public HotelAmenitiesDto delete(Long id) {
        HotelAmenities amenities = hotelAmenitiesRepository.findById(id).orElseThrow();
        hotelInAmenitiesRepository.deleteByAmenitiesSid(id);
        hotelAmenitiesRepository.delete(amenities);
        return toDto(amenities, true);
    }

    public HotelAmenitiesDto findById(Long id) {
        HotelAmenities amenities = hotelAmenitiesRepository.findById(id).orElseThrow();
        if (amenities.getDeleted() != null && amenities.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(amenities, true);
    }

    private HotelAmenitiesDto toDto(HotelAmenities ame, boolean sid) {
        if (sid) {
            return HotelAmenitiesDto
                    .builder()
                    .sid(ame.getSid())
                    .title(ame.getTitle())
                    .description(ame.getDescription())
                    .build();
        } else {
            return HotelAmenitiesDto
                    .builder()
                    .title(ame.getTitle())
                    .description(ame.getDescription())
                    .build();
        }
    }
}
