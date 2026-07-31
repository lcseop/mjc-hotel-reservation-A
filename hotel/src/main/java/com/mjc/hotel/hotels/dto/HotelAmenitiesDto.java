package com.mjc.hotel.hotels.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelAmenitiesDto {
    private Long sid;
    private String title;
    private String description;
}
