package com.mjc.hotel.room.service;

import com.mjc.hotel.room.dto.RoomTypeDto;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomInTagRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoomTypeService {
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private Room

    @Transactional
    public RoomTypeDto insert(RoomTypeDto roomTypeDto) {
        if (roomTypeDto.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomType clone = RoomType
                .builder()
                .title(roomTypeDto.getTitle())
                .build();
        return toDto(roomTypeRepository.save(clone), true);
    }

    @Transactional
    public RoomTypeDto update(RoomTypeDto roomTypeDto) {
        if (roomTypeDto.getSid() == null || roomTypeDto.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomType origin = roomTypeRepository.findById(roomTypeDto.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        RoomType clone = RoomType
                .builder()
                .sid(roomTypeDto.getSid())
                .title(roomTypeDto.getTitle())
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(roomTypeRepository.save(clone), true);
    }

    @Transactional
    public RoomTypeDto delete(Long id) {
        RoomType tag = roomTypeRepository.findById(id).orElseThrow();
        if ()
        if (tag.getDeleted() != null && tag.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        roomTypeRepository.delete(tag);
        return toDto(roomTypeRepository.save(tag), true);
    }

    @Transactional
    public RoomTypeDto findById(Long id) {
        RoomType tag = roomTypeRepository.findById(id).orElseThrow();
        if (tag.getDeleted() != null && tag.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(tag, true);
    }

    private RoomTypeDto toDto(RoomType tag, boolean sid) {
        if (sid) {
            return RoomTypeDto
                    .builder()
                    .sid(tag.getSid())
                    .title(tag.getTitle())
                    .build();
        } else {
            return RoomTypeDto
                    .builder()
                    .title(tag.getTitle())
                    .build();
        }
    }
}
