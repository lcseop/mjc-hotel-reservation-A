package com.mjc.hotel.promotion.dto;

import com.mjc.hotel.promotion.entity.ConditionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionSearchRequestDto {
    private String promotionName; // 프로모션 이름 검색용
    private ConditionType conditionType; // 프로모션 유형 (Enum)
    private LocalDateTime startDate;    // 기간 시작
    private LocalDateTime endDate;      // 기간 종료
}
