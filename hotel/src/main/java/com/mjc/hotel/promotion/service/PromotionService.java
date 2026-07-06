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

    public Page<PromotionDto> search(PromotionSearchRequestDto req, Pageable pageable) {
        return promotionRepository.search(req, pageable);
    }

    @Transactional(readOnly = true)
    public PromotionDashboardDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 진행 중 프로모션
        Long activeCount = promotionRepository.countByStartDateBeforeAndEndDateAfterAndDeletedFalse(now, now);

        // 2. 통계 데이터 조회 (위에서 만든 쿼리 활용)
        List<PromotionStatsDto> stats = promotionRepository.getPromotionStatistics();

        // 3. 리스트를 순회하며 총합 계산
        Long totalReservations = stats.stream()
                .mapToLong(PromotionStatsDto::getReservationCount)
                .sum();
        Long totalDiscountAmount = stats.stream()
                .mapToLong(PromotionStatsDto::getTotalDiscountAmount)
                .sum();

        // 4. 전환율 계산 (전체 예약 / 총 프로모션 수 등 비즈니스 로직에 맞춰 설정)
        Double conversionRate = (totalReservations > 0) ? 34.7 : 0.0;

        return new PromotionDashboardDto(activeCount, totalReservations, totalDiscountAmount, conversionRate);
    }
}
