package com.mjc.hotel.hotels.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class HotelRequestDto {
    private Long sid;
    private Long typeId;
    private Long photoId;
    private String hotelName;
    private Integer hotelPrice;
    private String location;
    private Integer starRating;
    private String description;
    private Double latitude;
    private Double longitude;
}
