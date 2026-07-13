package com.mjc.hotel.hotels.service;


import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HotelTypeService {
    @Autowired
    private HotelTypeRepository hotelTypeRepository;
    @Autowired
    private HotelRepository hotelRepository;

    @Transactional
    public HotelTypeDto insert(HotelTypeDto type) {
        if (type.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelType clone = HotelType
                .builder()
                .title(type.getTitle())
                .build();

        return toDto(hotelTypeRepository.save(clone), true);
    }

    @Transactional
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

    @Transactional
    public HotelTypeDto delete(Long id) {
        if (hotelRepository.existsByTypeSid(id)) {
            throw new IllegalStateException("이 타입을 사용 중인 호텔이 있습니다.");
        }
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        hotelTypeRepository.delete(type);
        return toDto(type, true);
    }

    public HotelTypeDto findById(Long id) {
        HotelType type = hotelTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(type, true);
    }

    public List<HotelTypeDto> findAll() {
        return hotelTypeRepository.findAll().stream()
                .filter(type -> !Boolean.TRUE.equals(type.getDeleted()))
                .map(type -> toDto(type, true))
                .toList();
    }

    public Slice<HotelTypeDto> findByTitleContain(String title, Pageable pageable) {
        Slice<HotelType> slc = hotelTypeRepository.findByTitleContains(title, pageable);
        List<HotelTypeDto> dtos = slc
                .stream()
                .map(t -> HotelTypeDto.builder()
                        .sid(t.getSid()).title(t.getTitle()).build())
                .toList();
        return new SliceImpl<>(dtos, slc.getPageable(), slc.hasNext());
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
