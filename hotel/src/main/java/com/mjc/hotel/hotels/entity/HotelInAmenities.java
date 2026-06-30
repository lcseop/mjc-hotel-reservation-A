package com.mjc.hotel.hotels.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "hotel_in_amenities")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
// 호텔 편의 시설 - 호텔을 연결
public class HotelInAmenities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name = "hotel_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    @JoinColumn(name = "amenities_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private HotelAmenities amenities;
}
