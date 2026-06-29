package com.mjc.hotel.promotion.mapper;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.Promotion;

public class PromotionMapper {
    public static Promotion toEntity(PromotionDto dto) {
        return Promotion.builder()
                .sid(dto.getSid())
                .roomType(dto.getRoomType())
                .promotionName(dto.getPromotionName())
                .starRating(dto.getStarRating())
                .howLong(dto.getHowLong())
                .totalAmount(dto.getTotalAmount())
                .build();
    }
}
