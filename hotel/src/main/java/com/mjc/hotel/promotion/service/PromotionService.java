package com.mjc.hotel.promotion.service;

import com.mjc.hotel.promotion.dto.PromotionDashboardDto;
import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.dto.PromotionSearchRequestDto;
import com.mjc.hotel.promotion.dto.PromotionStatsDto;
import com.mjc.hotel.promotion.entity.*;
import com.mjc.hotel.promotion.mapper.PromotionMapper;
import com.mjc.hotel.promotion.repository.*;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Transactional
    public PromotionDto insert(PromotionDto promotionDto) {
        RoomType roomType = roomTypeRepository.findById(promotionDto.getRoomTypeId()).orElseThrow();

        Promotion promotion = Promotion.builder()
                .sid(promotionDto.getSid())
                .roomType(roomType)
                .promotionName(promotionDto.getPromotionName())
                .discountContent(promotionDto.getDiscountContent())
                .conditionType(resolveConditionType(promotionDto.getStatus()))
                .startDate(promotionDto.getStartDate())
                .endDate(promotionDto.getEndDate())
                .build();

        Promotion saved =  promotionRepository.save(promotion);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomTypeId(saved.getRoomType().getSid())
                .promotionName(saved.getPromotionName())
                .discountContent(saved.getDiscountContent())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .status(saved.getConditionType() != null ? saved.getConditionType().name() : null)
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
                .discountContent(promotionDto.getDiscountContent() != null ? promotionDto.getDiscountContent() : promotion.getDiscountContent())
                .conditionType(resolveConditionType(promotionDto.getStatus(), promotion.getConditionType()))
                .startDate(promotionDto.getStartDate() != null ? promotionDto.getStartDate() : promotion.getStartDate())
                .endDate(promotionDto.getEndDate() != null ? promotionDto.getEndDate() : promotion.getEndDate())
                .build();

        updated.setCreatedAt(promotion.getCreatedAt());

        Promotion saved = promotionRepository.save(updated);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomTypeId(saved.getRoomType().getSid())
                .promotionName(saved.getPromotionName())
                .discountContent(saved.getDiscountContent())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .status(saved.getConditionType() != null ? saved.getConditionType().name() : null)
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
                .discountContent(saved.getDiscountContent())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .status(saved.getConditionType() != null ? saved.getConditionType().name() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> getPromotionByStatus(ConditionType type) {
        List<Promotion> promotions = promotionRepository.findByConditionType(type);

        return promotions.stream()
                .map(PromotionMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<PromotionDto> search(PromotionSearchRequestDto req, Pageable pageable) {
        return promotionRepository.search(req, pageable);
    }

    @Transactional(readOnly = true)
    public PromotionDashboardDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        Long activeCount = promotionRepository.countByStartDateBeforeAndEndDateAfterAndDeletedFalse(now, now);

        List<PromotionStatsDto> stats = promotionRepository.getPromotionStatistics();

        Long totalReservations = stats.stream()
                .mapToLong(PromotionStatsDto::getReservationCount)
                .sum();
        Long totalDiscountAmount = stats.stream()
                .mapToLong(PromotionStatsDto::getTotalDiscountAmount)
                .sum();

        Double conversionRate = (totalReservations > 0) ? 34.7 : 0.0;

        return new PromotionDashboardDto(activeCount, totalReservations, totalDiscountAmount, conversionRate);
    }

    private ConditionType resolveConditionType(String status) {
        return resolveConditionType(status, ConditionType.ACTIVE);
    }

    private ConditionType resolveConditionType(String status, ConditionType fallback) {
        if (status == null || status.isBlank()) {
            return fallback != null ? fallback : ConditionType.ACTIVE;
        }

        try {
            return ConditionType.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (status.trim()) {
                case "예정" -> ConditionType.EXPECTED;
                case "진행중" -> ConditionType.ACTIVE;
                case "일시정지", "중지" -> ConditionType.STOP;
                case "종료" -> ConditionType.END;
                default -> fallback != null ? fallback : ConditionType.ACTIVE;
            };
        }
    }
}
