package com.mjc.hotel.promotion.service;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.*;
import com.mjc.hotel.promotion.mapper.PromotionMapper;
import com.mjc.hotel.promotion.repository.*;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private ConditionRepository conditionRepository;

    @Transactional
    public PromotionDto insert(PromotionDto promotionDto) {
        RoomType roomType = roomTypeRepository.findById(promotionDto.getRoomTypeId()).orElseThrow();

        Promotion promotion = Promotion.builder()
                .sid(promotionDto.getSid())
                .roomType(roomType)
                .promotionName(promotionDto.getPromotionName())
                .startDate(promotionDto.getStartDate())
                .endDate(promotionDto.getEndDate())
                .build();

        Promotion saved =  promotionRepository.save(promotion);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomTypeId(saved.getRoomType().getSid())
                .promotionName(saved.getPromotionName())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();
    }

    @Transactional
    public PromotionDto update(PromotionDto promotionDto) {
        Promotion promotion = promotionRepository.findById(promotionDto.getSid())
                .orElseThrow(() -> new IllegalArgumentException("해당 프로모션이 없습니다."));
        RoomType roomType = roomTypeRepository.findById(promotionDto.getRoomTypeId()).orElseThrow();

        if (promotion.getDeleted() != null && promotion.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, promotion.getPromotionName() + " is not found");
        }

        Promotion updated = Promotion.builder()
                .sid(promotionDto.getSid())
                .roomType(roomType)
                .promotionName(promotionDto.getPromotionName())
                .startDate(promotionDto.getStartDate())
                .endDate(promotionDto.getEndDate())
                .build();

        updated.setCreatedAt(promotion.getCreatedAt());

        Promotion saved = promotionRepository.save(updated);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomTypeId(saved.getRoomType().getSid())
                .promotionName(saved.getPromotionName())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();
    }

    @Transactional
    public PromotionDto delete(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 프로모션이 없습니다."));

        if (promotion.getDeleted() != null && promotion.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, promotion.getPromotionName() + " is not found");
        }

        promotion.setDeleted(true);
        promotion.setDeletedAt(LocalDateTime.now());

        Promotion saved = promotionRepository.save(promotion);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomTypeId(saved.getRoomType().getSid())
                .promotionName(saved.getPromotionName())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> getPromotionByStatus(ConditionType type) {
        List<Condition> conditions = conditionRepository.findByConditiontype(type);

        return conditions.stream()
                .map(c -> PromotionMapper.toDto(c.getPromotion(), c))
                .collect(Collectors.toList());
    }
}
