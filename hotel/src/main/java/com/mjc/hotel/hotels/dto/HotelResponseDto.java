package com.mjc.hotel.hotels.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class HotelResponseDto {
    private Long sid;
    private String typeTitle;
    private String photoPath;
    private String amenitiesTitle;
    private String amenitiesDescription;
    private String hotelName;
    private Integer hotelPrice;
    private String location;
    private Integer starRating;
    private String description;
}
