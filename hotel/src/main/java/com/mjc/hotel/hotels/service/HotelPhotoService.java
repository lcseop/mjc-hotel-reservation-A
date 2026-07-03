package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelPhotoDto;
import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class HotelPhotoService {
    @Autowired
    private HotelPhotoRepository hotelPhotoRepository;
    @Autowired
    private HotelRepository hotelRepository;

    @Transactional
    public HotelPhotoDto insert(HotelPhotoDto photo) {
        if (photo.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        Hotel hotel = hotelRepository.findById(photo.getHotelId()).orElseThrow();
        HotelPhoto clone = HotelPhoto
                .builder()
                .hotel(hotel)
                .imagePath(photo.getImagePath())
                .build();

        return toDto(hotelPhotoRepository.save(clone), true);
    }

    @Transactional
    public HotelPhotoDto update(HotelPhotoDto photo) {
        if (photo.getSid() == null || photo.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        HotelPhoto origin =  hotelPhotoRepository.findById(photo.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        Hotel hotel = hotelRepository.findById(photo.getHotelId()).orElseThrow();
        HotelPhoto clone = HotelPhoto
                .builder()
                .sid(photo.getSid())
                .hotel(hotel)
                .imagePath(photo.getImagePath())
                .build();
        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(hotelPhotoRepository.save(clone), true);
    }

    @Transactional
    public HotelPhotoDto delete(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        hotelPhotoRepository.delete(photo);
        return toDto(photo, true);
    }

    public HotelPhotoDto findById(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(photo, true);
    }

    private HotelPhotoDto toDto(HotelPhoto photo, boolean sid) {
        if (sid) {
            return HotelPhotoDto
                    .builder()
                    .sid(photo.getSid())
                    .hotelId(photo.getHotel().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        } else {
            return HotelPhotoDto
                    .builder()
                    .hotelId(photo.getHotel().getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
