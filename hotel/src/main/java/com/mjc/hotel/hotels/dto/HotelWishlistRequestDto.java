package com.mjc.hotel.hotels.dto;

import lombok.Data;

@Data
public class HotelWishlistRequestDto {
    private Long memberId;
    private Long hotelId;
}
