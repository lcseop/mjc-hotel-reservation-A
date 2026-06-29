package com.mjc.hotel.promotion.mapper;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.Promotion;
import com.mjc.hotel.room.entity.RoomType;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {
    public static Promotion toEntity(PromotionDto dto, RoomType roomType) {
        return Promotion.builder()
                .sid(dto.getSid())
                .roomType(roomType)
                .promotionName(dto.getPromotionName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }
}
