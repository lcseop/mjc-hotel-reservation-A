package com.mjc.hotel.hotels.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class HotelSearchRequestDto {
    private String location;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer adult;
    private Integer child;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer star;
    private List<Long> roomTypeIds;

    public Integer getTotalPeople() {
        int adultCount = adult != null ? adult : 0;
        int childCount = child != null ? child : 0;
        int total = adultCount + childCount;

        return total > 0 ? total : null;
    }
}
