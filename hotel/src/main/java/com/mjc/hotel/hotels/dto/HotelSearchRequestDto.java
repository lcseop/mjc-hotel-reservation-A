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
    private Integer people;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer star;
    private List<Long> roomTypeIds;
}
