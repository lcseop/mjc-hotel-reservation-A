package com.mjc.hotel.hotels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelWishlistResponseDto {
    private Long sid;
    private Long memberId;
    private Long hotelId;
    private String hotelName;
    private String typeTitle;
    private Integer hotelPrice;
    private String location;
    private Integer starRating;
    private String description;
    private Integer maxDiscountRate;
    private String imagePath;
    private LocalDateTime createdAt;
    private Boolean wished;
}
