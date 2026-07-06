package com.mjc.hotel.hotels.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class HotelPopularResponseDto {
    private Long sid;
    private String hotelName;
    private String location;
    private String firstImage;
    private Integer price;
    private Double rating;
}
