package com.mjc.hotel.room.entity;

import com.mjc.hotel.hotels.entity.Hotel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "room")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name = "hotel_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotelId;

    @JoinColumn(name = "room_tag_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RoomTag roomTagId;

    @JoinColumn(name = "room_photo_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RoomPhoto roomPhotoId;

    @JoinColumn(name = "room_type_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RoomType roomTypeId;

    @Column(name = "room_name", length = 30, nullable = false)
    private String roomName;

    @Column(name = "room_price", nullable = false)
    private Integer roomPrice;

    @Column(name = "room_number", nullable = false)
    private Integer roomNumber;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Integer area;

    @Column(name = "maximum_people", nullable = false)
    private Integer maximumPeople;
}
