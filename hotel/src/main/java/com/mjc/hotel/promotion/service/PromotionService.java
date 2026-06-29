package com.mjc.hotel.promotion.service;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.Condition;
import com.mjc.hotel.promotion.entity.Promotion;
import com.mjc.hotel.promotion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private PromotionPackageRepository promotionPackageRepository;
    @Autowired
    private DiscountRateRepository discountRateRepository;
    @Autowired
    private FlatRepository flatRepository;
    @Autowired
    private ConditionRepository conditionRepository;

    public PromotionDto insert(PromotionDto promotionDto) {
        Condition condition = conditionRepository.findById(promotionDto.getConditionId())
                .orElseThrow(() -> new IllegalArgumentException("조건 정보를 찾을 수 없습니다."));

        Promotion promotion = Promotion.builder()
                .id(promotionDto.getId())
                .roomType()
                // DTO로부터 필요한 데이터들을 엔티티에 세팅
                .build();

        Promotion saved =  promotionRepository.save(promotion);

        return PromotionDto.builder()
                .id(saved.getId())
                .build();
    }
}
