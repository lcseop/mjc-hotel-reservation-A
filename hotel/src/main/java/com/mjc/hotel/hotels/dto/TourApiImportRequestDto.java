package com.mjc.hotel.hotels.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TourApiImportRequestDto {
    private String keyword;
    private Integer page;
    private Integer size;
    private List<String> contentIds;
}
