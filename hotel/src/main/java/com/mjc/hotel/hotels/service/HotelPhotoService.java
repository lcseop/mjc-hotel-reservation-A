package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.entity.HotelPhoto;
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

    public HotelPhoto insert(HotelPhoto photo) {
        if (photo.getImagePath() == null) return null;
        HotelPhoto clone = HotelPhoto
                .builder()
                .imagePath(photo.getImagePath())
                .build();
        return hotelPhotoRepository.save(clone);
    }

    public HotelPhoto update(HotelPhoto photo) {
        if (photo.getSid() == null || photo.getImagePath() == null) return null;
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return hotelPhotoRepository.save(photo);
    }

    public HotelPhoto delete(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        photo.setDeleted(true);
        photo.setDeletedAt(LocalDateTime.now());
        return hotelPhotoRepository.save(photo);
    }

    public HotelPhoto findById(Long id) {
        HotelPhoto photo = hotelPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return photo;
    }
}
