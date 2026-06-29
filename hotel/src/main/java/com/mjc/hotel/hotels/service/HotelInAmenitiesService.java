package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.entity.HotelInAmenities;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelInAmenitiesRepository;
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

    public HotelInAmenities insert(HotelInAmenities ame) {
        if (ame.getHotel() == null || ame.getAmenities() == null) return null;
        HotelInAmenities insert = HotelInAmenities
                .builder()
                .hotel(ame.getHotel())
                .amenities(ame.getAmenities())
                .build();
        return hotelInAmenitiesRepository.save(insert);
    }

    public HotelInAmenities update(HotelInAmenities ame) {
        if (ame.getHotel() == null || ame.getAmenities() == null
        || ame.getSid() == null) return null;
        if (ame.getDeleted() != null && ame.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return hotelInAmenitiesRepository.save(ame);
    }

    public HotelInAmenities delete(Long id) {
        HotelInAmenities ame = hotelInAmenitiesRepository.findById(id).orElseThrow();
        if (ame.getDeleted() != null && ame.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        ame.setDeleted(true);
        ame.setDeletedAt(LocalDateTime.now());
        return hotelInAmenitiesRepository.save(ame);
    }

    public HotelInAmenities findByid(Long id) {

    }
}
