package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
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

    public HotelAmenities insert(HotelAmenities amenities) {
        if (amenities.getTitle() == null) return null;
        HotelAmenities clone = HotelAmenities
                .builder()
                .title(amenities.getTitle())
                .description(amenities.getDescription())
                .build();
        return hotelAmenitiesRepository.save(clone);
    }

    public HotelAmenities update(HotelAmenities amenities) {
        if (amenities.getTitle() == null || amenities.getSid() == null) return null;
        if (amenities.getDeleted() != null && amenities.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return hotelAmenitiesRepository.save(amenities);
    }

    public HotelAmenities delete(Long id) {
        HotelAmenities amenities = hotelAmenitiesRepository.findById(id).orElseThrow();
        if (amenities.getDeleted() != null && amenities.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        amenities.setDeleted(true);
        amenities.setDeletedAt(LocalDateTime.now());
        return hotelAmenitiesRepository.save(amenities);
    }

    public HotelAmenities findById(Long id) {
        HotelAmenities amenities = hotelAmenitiesRepository.findById(id).orElseThrow();
        if (amenities.getDeleted() != null && amenities.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return amenities;
    }
}
