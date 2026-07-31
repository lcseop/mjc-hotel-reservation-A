package com.mjc.hotel.room.service;

import com.mjc.hotel.promotion.repository.PromotionRepository;
import com.mjc.hotel.room.dto.RoomTypeDto;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final PromotionRepository promotionRepository;

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
        RoomType type = roomTypeRepository.findById(id).orElseThrow();
        if (roomRepository.existsByRoomTypeIdSid(id)) {
            throw new IllegalStateException("이 타입을 사용 중인 객실이 있습니다.");
        }
        if (promotionRepository.existsByRoomTypeSid(id)) {
            throw new IllegalStateException("이 타입을 사용 중인 프로모션이 있습니다.");
        }
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        roomTypeRepository.delete(type);
        return toDto(type, true);
    }

    @Transactional
    public RoomTypeDto findById(Long id) {
        RoomType type = roomTypeRepository.findById(id).orElseThrow();
        if (type.getDeleted() != null && type.getDeleted()) throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        return toDto(type, true);
    }

    public List<RoomTypeDto> findAll() {
        return roomTypeRepository.findAll().stream()
                .filter(type -> type.getDeleted() == null || !type.getDeleted())
                .map(type -> toDto(type, true))
                .toList();
    }

    private RoomTypeDto toDto(RoomType type, boolean sid) {
        if (sid) {
            return RoomTypeDto
                    .builder()
                    .sid(type.getSid())
                    .title(type.getTitle())
                    .build();
        } else {
            return RoomTypeDto
                    .builder()
                    .title(type.getTitle())
                    .build();
        }
    }
}
