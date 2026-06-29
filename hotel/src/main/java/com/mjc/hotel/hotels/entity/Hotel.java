package com.mjc.hotel.hotels.entity;

import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "hotel")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Hotel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name = "type_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HotelType type;

    @JoinColumn(name = "photo_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HotelPhoto photo;

    @JoinColumn(name = "amenities_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HotelAmenities amenities;

    @Column(name = "hotel_name", length = 50, nullable = false)
    private String hotelName;

    @Column(name = "hotel_price", nullable = false)
    private Integer hotelPrice;

    @Column(length = 50, nullable = false)
    private String location;

    @Column(name = "star_rating")
    private Integer starRating;

    @Column(columnDefinition = "TEXT")
    private String description;

}
