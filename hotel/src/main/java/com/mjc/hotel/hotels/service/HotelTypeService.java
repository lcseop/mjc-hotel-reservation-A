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
        if (type.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelType clone = HotelType
                .builder()
                .title(type.getTitle())
                .build();

        return toDto(hotelTypeRepository.save(clone), true);
    }

    public HotelTypeDto update(HotelTypeDto type) {
        if (type.getTitle() == null || type.getSid() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelType origin = hotelTypeRepository.findById(type.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        HotelType clone = HotelType
                .builder()
                .sid(type.getSid())
                .title(type.getTitle())
                .build();
        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(hotelTypeRepository.save(clone), true);
    }

    public HotelTypeDto delete(Long id) {
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        type.setDeleted(true);
        type.setDeletedAt(LocalDateTime.now());
        return toDto(hotelTypeRepository.save(type), true);
    }

    public HotelTypeDto findById(Long id) {
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(type, true);
    }

    private HotelTypeDto toDto(HotelType type, boolean sid) {
        if (sid) {
            return HotelTypeDto
                    .builder()
                    .sid(type.getSid())
                    .title(type.getTitle())
                    .build();
        } else {
            return HotelTypeDto
                    .builder()
                    .title(type.getTitle())
                    .build();
        }
    }


}
