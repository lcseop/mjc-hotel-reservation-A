package com.mjc.hotel.promotion.mapper;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.entity.Condition;
import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;
import com.mjc.hotel.room.entity.RoomType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    public static PromotionDto toDto(Promotion promotion, Condition condition) {
        String status = "진행중";

        if (condition != null && condition.getConditiontype() == ConditionType.STOP) {
            status = "일시정지";
        } else {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(promotion.getStartDate())) {
                status = "예정";
            } else if (now.isAfter(promotion.getEndDate())) {
                status = "종료";
            }
        }

        return PromotionDto.builder()
                .sid(promotion.getSid())
                .roomTypeId(promotion.getRoomType().getSid())
                .promotionName(promotion.getPromotionName())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(status)
                .build();
    }
}
