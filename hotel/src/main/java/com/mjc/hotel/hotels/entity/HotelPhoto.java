package com.mjc.hotel.hotels.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "hotel_photo")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(name = "image_path", length = 255, nullable = false)
    private String imagePath;
}
