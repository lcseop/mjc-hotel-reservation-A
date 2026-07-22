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
    private List<HotelAmenitiesDto> amenities;

    public HotelResponseDto(Long sid,
                            String typeTitle,
                            String hotelName,
                            Integer hotelPrice,
                            String location,
                            Integer starRating,
                            String description,
                            Double latitude,
                            Double longitude,
                            Integer maxDiscountRate) {
        this.sid = sid;
        this.typeTitle = typeTitle;
        this.hotelName = hotelName;
        this.hotelPrice = hotelPrice;
        this.location = location;
        this.starRating = starRating;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxDiscountRate = maxDiscountRate;
    }
}
