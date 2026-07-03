package com.mjc.hotel.room.service;

import com.mjc.hotel.room.dto.RoomInTagDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomInTag;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.repository.RoomInTagRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class RoomInTagService {
    @Autowired
    private RoomInTagRepository roomInTagRepository;
    @Autowired
    private RoomTagRepository roomTagRepository;
    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    public RoomInTagDto insert(RoomInTagDto roomInTagDto) {
        if (roomInTagDto.getRoomId() == null || roomInTagDto.getRoomTagId() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomTag roomTag = roomTagRepository.findById(roomInTagDto.getRoomTagId()).orElseThrow();
        Room room = roomRepository.findById(roomInTagDto.getRoomId()).orElseThrow();
        RoomInTag clone = RoomInTag
                .builder()
                .room(room)
                .tag(roomTag)
                .build();
        return toDto(roomInTagRepository.save(clone), true);
    }

    @Transactional
    public RoomInTagDto update(RoomInTagDto roomInTagDto) {
        if (roomInTagDto.getSid() == null || roomInTagDto.getRoomId() == null
        || roomInTagDto.getRoomTagId() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomInTag origin = roomInTagRepository.findById(roomInTagDto.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        RoomTag roomTag = roomTagRepository.findById(roomInTagDto.getRoomTagId()).orElseThrow();
        Room room = roomRepository.findById(roomInTagDto.getRoomId()).orElseThrow();
        RoomInTag clone = RoomInTag
                .builder()
                .room(room)
                .tag(roomTag)
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(roomInTagRepository.save(clone), true);
    }

    @Transactional
    public RoomInTagDto delete(Long id) {
        RoomInTag tag = roomInTagRepository.findById(id).orElseThrow();
        roomInTagRepository.delete(tag);
        return toDto(tag, true);
    }

    @Transactional
    public RoomInTagDto findById(Long id) {
        RoomInTag tag = roomInTagRepository.findById(id).orElseThrow();
        if (tag.getDeleted() != null && tag.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(tag, true);
    }

    private RoomInTagDto toDto(RoomInTag tag, boolean sid) {
        if (sid) {
            return RoomInTagDto
                    .builder()
                    .sid(tag.getSid())
                    .title(tag.getTitle())
                    .build();
        } else {
            return RoomInTagDto
                    .builder()
                    .title(tag.getTitle())
                    .build();
        }
    }
}
