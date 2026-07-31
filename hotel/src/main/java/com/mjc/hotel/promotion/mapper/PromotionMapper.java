package com.mjc.hotel.promotion.mapper;

import com.mjc.hotel.promotion.dto.PromotionDto;
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
                .discountContent(dto.getDiscountContent())
                .conditionType(resolveConditionType(dto.getStatus()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public static PromotionDto toDto(Promotion promotion) {
        String status = "진행중";

        // Condition 엔티티 대신, Promotion의 통합된 conditionType 필드를 사용합니다.
        if (promotion.getConditionType() == ConditionType.STOP) {
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
                .discountContent(promotion.getDiscountContent())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(status)
                .build();
    }

    private static ConditionType resolveConditionType(String status) {
        if (status == null || status.isBlank()) {
            return ConditionType.ACTIVE;
        }

        try {
            return ConditionType.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (status.trim()) {
                case "예정" -> ConditionType.EXPECTED;
                case "진행중" -> ConditionType.ACTIVE;
                case "일시정지", "중지" -> ConditionType.STOP;
                case "종료" -> ConditionType.END;
                default -> ConditionType.ACTIVE;
            };
        }
    }
}
