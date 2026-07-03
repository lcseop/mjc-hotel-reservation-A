package com.mjc.hotel.hotels.entity;

import com.mjc.hotel.util.BaseEntity;
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
public class HotelPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name = "hotel_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    @Column(name = "image_path", length = 255, nullable = false)
    private String imagePath;
}
