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

    @JoinColumn(name = "type_id", nullable = false, comment = "호텔 타입")
    @ManyToOne(fetch = FetchType.LAZY)
    private HotelType type;

    @Column(name = "hotel_name", length = 50, nullable = false, comment = "호텔 이름")
    private String hotelName;

    @Column(name = "hotel_price", nullable = false, comment = "호텔 대표 가격")
    private Integer hotelPrice;

    @Column(length = 50, nullable = false, comment = "위치")
    private String location;

    @Column(name = "star_rating", comment = "성급")
    private Integer starRating;

    @Column(columnDefinition = "TEXT", comment = "호텔 설명")
    private String description;

    @Column(comment = "경도")
    private Double latitude;

    @Column(comment = "위도")
    private Double longitude;



}
