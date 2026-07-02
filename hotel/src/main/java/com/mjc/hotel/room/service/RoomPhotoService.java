package com.mjc.hotel.room.service;

import com.mjc.hotel.room.dto.RoomPhotoDto;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class RoomPhotoService {
    @Autowired
    private RoomPhotoRepository roomPhotoRepository;

    public RoomPhotoDto insert(RoomPhotoDto roomPhotoDto) {
        if (roomPhotoDto.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomPhoto clone = RoomPhoto
                .builder()
                .imagePath(roomPhotoDto.getImagePath())
                .build();
        return toDto(roomPhotoRepository.save(clone), true);
    }

    public RoomPhotoDto update(RoomPhotoDto roomPhotoDto) {
        if (roomPhotoDto.getSid() == null || roomPhotoDto.getImagePath() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomPhoto origin = roomPhotoRepository.findById(roomPhotoDto.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        RoomPhoto clone = RoomPhoto
                .builder()
                .sid(roomPhotoDto.getSid())
                .imagePath(roomPhotoDto.getImagePath())
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(roomPhotoRepository.save(clone), true);
    }

    public RoomPhotoDto delete(Long id) {
        RoomPhoto photo = roomPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        photo.setDeleted(true);
        photo.setDeletedAt(LocalDateTime.now());
        return toDto(roomPhotoRepository.save(photo), true);
    }

    public RoomPhotoDto findById(Long id) {
        RoomPhoto photo = roomPhotoRepository.findById(id).orElseThrow();
        if (photo.getDeleted() != null && photo.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(photo, true);
    }

    private RoomPhotoDto toDto(RoomPhoto photo, boolean sid) {
        if (sid) {
            return RoomPhotoDto
                    .builder()
                    .sid(photo.getSid())
                    .imagePath(photo.getImagePath())
                    .build();
        } else {
            return RoomPhotoDto
                    .builder()
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
