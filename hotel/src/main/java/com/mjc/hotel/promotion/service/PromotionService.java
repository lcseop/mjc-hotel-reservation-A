package com.mjc.hotel.promotion.service;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.*;
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

    @Transactional
    public PromotionDto insert(PromotionDto promotionDto) {
        PromotionPackage promotionPackage = promotionPackageRepository.findById(promotionDto.getSid()).orElseThrow();
        Flat flat = flatRepository.findById(promotionDto.getSid()).orElseThrow();
        DiscountRate discountRate = discountRateRepository.findById(promotionDto.getSid()).orElseThrow();
        Condition condition = conditionRepository.findById(promotionDto.getSid())
                .orElseThrow(() -> new IllegalArgumentException("조건 정보를 찾을 수 없습니다."));

        Promotion promotion = Promotion.builder()
                .sid(promotionDto.getSid())
                .roomType(promotionDto.getRoomType())
                .promotionName(promotionDto.getPromotionName())
                .starRating(promotionDto.getStarRating())
                .howLong(promotionDto.getHowLong())
                .totalAmount(promotionDto.getTotalAmount())
                .build();

        Promotion saved =  promotionRepository.save(promotion);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomType(saved.getRoomType())
                .promotionName(saved.getPromotionName())
                .starRating(saved.getStarRating())
                .howLong(saved.getHowLong())
                .totalAmount(saved.getTotalAmount())
                .build();
    }

    @Transactional
    public PromotionDto update(PromotionDto promotionDto) {
        Promotion promotion = promotionRepository.findById(promotionDto.getSid())
                .orElseThrow(() -> new IllegalArgumentException("해당 프로모션이 없습니다."));

        Flat flat = flatRepository.findById(promotionDto.getSid()).orElseThrow();
        DiscountRate discountRate = discountRateRepository.findById(promotionDto.getSid()).orElseThrow();
        Condition condition = conditionRepository.findById(promotionDto.getSid()).orElseThrow();

        promotion.update(promotionDto.getPromotionName(), promotionDto.getRoomType());

        Promotion saved = promotionRepository.save(promotion);

        return PromotionDto.builder()
                .sid(saved.getSid())
                .roomType(saved.getRoomType())
                .promotionName(saved.getPromotionName())
                .starRating(saved.getStarRating())
                .howLong(saved.getHowLong())
                .totalAmount(saved.getTotalAmount())
                .build();
    }

    @Transactional
    public PromotionDto delete(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 프로모션이 없습니다."));
        promotionRepository.delete(promotion);

        return PromotionDto.builder()
                .sid(promotion.getSid())
                .roomType(promotion.getRoomType())
                .promotionName(promotion.getPromotionName())
                .starRating(promotion.getStarRating())
                .howLong(promotion.getHowLong())
                .totalAmount(promotion.getTotalAmount())
                .build();
    }
}
