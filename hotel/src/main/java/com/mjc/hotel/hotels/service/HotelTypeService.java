package com.mjc.hotel.hotels.service;


import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class HotelTypeService {
    @Autowired
    private HotelTypeRepository hotelTypeRepository;

    public HotelTypeDto insert(HotelTypeDto type) {
        if (type.getTitle() == null) return null;
        HotelTypeDto clone = HotelTypeDto
                .builder()
                .title(type.getTitle())
                .build();

        return hotelTypeRepository.save(clone);
    }

    public HotelType update(HotelType type) {
        if (type.getTitle() == null || type.getSid() == null) return null;
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return hotelTypeRepository.save(type);
    }

    public HotelType delete(Long id) {
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        type.setDeleted(true);
        type.setDeletedAt(LocalDateTime.now());
        return hotelTypeRepository.save(type);
    }

    public HotelType findById(Long id) {
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return type;
    }


}
