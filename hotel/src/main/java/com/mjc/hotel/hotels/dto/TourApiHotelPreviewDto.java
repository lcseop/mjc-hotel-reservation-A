package com.mjc.hotel.hotels.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TourApiHotelPreviewDto {
    private String contentId;
    private String title;
    private String location;
    private String imagePath;
    private Double latitude;
    private Double longitude;
    private Boolean alreadyImported;
}
