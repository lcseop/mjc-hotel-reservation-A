package com.mjc.hotel.room.entity;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.reservations.entity.PointStatus;
import com.mjc.hotel.util.BaseEntity;
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
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name = "hotel_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotelId;

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

    @Column(name = "check_in_time")
    private Integer checkInTime;

    @Column(name = "check_out_time")
    private Integer checkOutTime;

    @Column(length = 20)
    private String parking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomPetAndSmokeEnum pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomPetAndSmokeEnum smoke;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_card", nullable = false)
    private RoomIdCardEnum idCard;
}
