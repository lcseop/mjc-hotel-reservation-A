package com.mjc.hotel.hotels.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class HotelResponseDto {
    private Long sid;
    private String typeTitle;
    private String hotelName;
    private Integer hotelPrice;
    private String location;
    private Integer starRating;
    private String description;
    private Double latitude;
    private Double longitude;
    private Integer maxDiscountRate;
}
