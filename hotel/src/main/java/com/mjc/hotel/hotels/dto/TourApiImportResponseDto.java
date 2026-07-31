package com.mjc.hotel.hotels.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TourApiImportResponseDto {
    private Integer requested;
    private Integer imported;
    private Integer skipped;
}
