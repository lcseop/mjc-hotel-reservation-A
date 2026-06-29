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
    private RoomType roomType ;
    private String promotionName;
    private Integer starRating;
    private LocalDateTime howLong;
    private Integer totalAmount;
}
