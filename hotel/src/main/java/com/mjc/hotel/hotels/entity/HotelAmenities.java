package com.mjc.hotel.hotels.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "hotel_amenities")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelAmenities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(length = 10, nullable = false)
    private String title;

    @Column(length = 30)
    private String description;
}
