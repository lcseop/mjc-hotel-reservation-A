package com.mjc.hotel.room.service;

import com.mjc.hotel.room.dto.RoomTagDto;
import com.mjc.hotel.room.dto.RoomTagDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.repository.RoomInTagRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTagService {
    private final RoomTagRepository roomTagRepository;
    private final RoomInTagRepository roomInTagRepository;

    @Transactional
    public RoomTagDto insert(RoomTagDto roomTagDto) {
        if (roomTagDto.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomTag clone = RoomTag
                .builder()
                .title(roomTagDto.getTitle())
                .build();
        return toDto(roomTagRepository.save(clone), true);
    }

    @Transactional
    public RoomTagDto update(RoomTagDto roomTagDto) {
        if (roomTagDto.getSid() == null || roomTagDto.getTitle() == null) throw new IllegalArgumentException("not null 속성이 null인 값이 있습니다.");
        RoomTag origin = roomTagRepository.findById(roomTagDto.getSid()).orElseThrow();
        if (origin.getDeleted() != null && origin.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        RoomTag clone = RoomTag
                .builder()
                .sid(roomTagDto.getSid())
                .title(roomTagDto.getTitle())
                .build();

        clone.setCreatedAt(origin.getCreatedAt());
        clone.setDeleted(origin.getDeleted());
        clone.setDeletedAt(origin.getDeletedAt());
        return toDto(roomTagRepository.save(clone), true);
    }

    @Transactional
    public RoomTagDto delete(Long id) {
        RoomTag tag = roomTagRepository.findById(id).orElseThrow();
        roomInTagRepository.deleteByTagSid(id);
        if (tag.getDeleted() != null && tag.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        roomTagRepository.delete(tag);
        return toDto(tag, true);
    }

    @Transactional
    public RoomTagDto findById(Long id) {
        RoomTag tag = roomTagRepository.findById(id).orElseThrow();
        if (tag.getDeleted() != null && tag.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(tag, true);
    }

    private RoomTagDto toDto(RoomTag tag, boolean sid) {
        if (sid) {
            return RoomTagDto
                    .builder()
                    .sid(tag.getSid())
                    .title(tag.getTitle())
                    .build();
        } else {
            return RoomTagDto
                    .builder()
                    .title(tag.getTitle())
                    .build();
        }
    }
}
