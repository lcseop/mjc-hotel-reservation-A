package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelPhotoDto;
import com.mjc.hotel.hotels.dto.HotelTypeDto;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
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

    public HotelPhotoDto insert(HotelPhotoDto photo) {
        if (photo.getImagePath() == null) return null;
        HotelPhoto clone = HotelPhoto
                .builder()
                .imagePath(photo.getImagePath())
                .build();

        return toDto(hotelPhotoRepository.save(clone), false);
    }

    public HotelPhotoDto update(HotelPhotoDto photo) {
        if (photo.getSid() == null || photo.getImagePath() == null) return null;
        HotelPhoto origin =  hotelPhotoRepository.findById(photo.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        HotelPhoto clone = HotelPhoto
                .builder()
                .sid(photo.getSid())
                .imagePath(photo.getImagePath())
                .build();
        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(hotelPhotoRepository.save(clone), true);
    }

    public HotelPhotoDto delete(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        photo.setDeleted(true);
        photo.setDeletedAt(LocalDateTime.now());
        return toDto(hotelPhotoRepository.save(photo), true);
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
                    .imagePath(photo.getImagePath())
                    .build();
        } else {
            return HotelPhotoDto
                    .builder()
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
