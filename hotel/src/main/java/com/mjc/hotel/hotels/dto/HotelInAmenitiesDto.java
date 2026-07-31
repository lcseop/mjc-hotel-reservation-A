package com.mjc.hotel.hotels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelInAmenitiesDto {
    private Long sid;
    private Long hotelId;
    private String hotelName;
    private Long amenitiesId;
    private String amenitiesTitle;
}
