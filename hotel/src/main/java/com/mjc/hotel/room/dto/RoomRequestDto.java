package com.mjc.hotel.room.dto;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.room.entity.*;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class RoomRequestDto {
    private Long sid;
    private Long hotelId;
    private Long roomTagId;
    private Long roomPhotoId;
    private Long roomTypeId;
    private String roomName;
    private Integer roomPrice;
    private Integer roomNumber;
    private Integer floor;
    private Integer area;
    private Integer maximumPeople;
    private Integer checkInTime;
    private Integer checkOutTime;
    private String parking;
    private RoomPetAndSmokeEnum pet;
    private RoomPetAndSmokeEnum smoke;
    private RoomIdCardEnum idCard;
}
