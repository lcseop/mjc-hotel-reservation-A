package com.mjc.hotel.promotion.dto;

import com.mjc.hotel.room.entity.RoomType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class PromotionDto {
    private Long sid;
    private Long roomTypeId ;
    private String promotionName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
