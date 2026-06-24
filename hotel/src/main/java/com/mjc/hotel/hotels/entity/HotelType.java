package com.mjc.hotel.hotels.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "hotel_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(length = 10, nullable = false)
    private String title;
}
