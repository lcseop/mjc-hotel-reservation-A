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
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelPhotoRepository hotelPhotoRepository;
    @Autowired
    private HotelTypeRepository hotelTypeRepository;

    @Transactional
    public HotelResponseDto insert(HotelRequestDto hotel) {
        HotelPhoto photo = hotelPhotoRepository.findById(hotel.getPhotoId()).orElseThrow();
        HotelType type = hotelTypeRepository.findById(hotel.getTypeId()).orElseThrow();

        Hotel insert = HotelMapper.clone(null, hotel, false, type, photo);

        Hotel saved = hotelRepository.save(insert);

        HotelResponseDto dto = HotelResponseDto
                .builder()
                .sid(saved.getSid())
                .typeTitle(type.getTitle())
                .photoPath(photo.getImagePath())
                .hotelName(saved.getHotelName())
                .hotelPrice(saved.getHotelPrice())
                .location(saved.getLocation())
                .starRating(saved.getStarRating())
                .description(saved.getDescription())
                .build();
        return dto;
    }

    @Transactional
    public HotelResponseDto update(HotelRequestDto hotel) {
        Hotel origin = hotelRepository.findById(hotel.getSid()).orElseThrow();

        if (origin.getDeleted() != null && origin.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        }

        HotelPhoto photo = hotelPhotoRepository.findById(hotel.getPhotoId()).orElseThrow();
        HotelType type = hotelTypeRepository.findById(hotel.getTypeId()).orElseThrow();

        Hotel update = HotelMapper.clone(origin, hotel, true, type, photo);

        Hotel saved = hotelRepository.save(update);

        HotelResponseDto dto = HotelResponseDto
                .builder()
                .sid(saved.getSid())
                .typeTitle(type.getTitle())
                .photoPath(photo.getImagePath())
                .hotelName(saved.getHotelName())
                .hotelPrice(saved.getHotelPrice())
                .location(saved.getLocation())
                .starRating(saved.getStarRating())
                .description(saved.getDescription())
                .build();

        return dto;
    }

    @Transactional
    public HotelResponseDto delete(Long id) {
        Hotel target = hotelRepository.findById(id).orElseThrow();

        if (target.getDeleted() != null && target.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, target.getHotelName() + " is not found");
        }

        HotelPhoto photo = hotelPhotoRepository.findById(target.getPhoto().getSid()).orElseThrow();
        HotelType type = hotelTypeRepository.findById(target.getType().getSid()).orElseThrow();

        target.setDeleted(true);
        target.setDeletedAt(LocalDateTime.now());

        Hotel saved = hotelRepository.save(target);

        HotelResponseDto dto = HotelResponseDto
                .builder()
                .sid(saved.getSid())
                .typeTitle(type.getTitle())
                .photoPath(photo.getImagePath())
                .hotelName(saved.getHotelName())
                .hotelPrice(saved.getHotelPrice())
                .location(saved.getLocation())
                .starRating(saved.getStarRating())
                .description(saved.getDescription())
                .build();

        return dto;
    }
}
