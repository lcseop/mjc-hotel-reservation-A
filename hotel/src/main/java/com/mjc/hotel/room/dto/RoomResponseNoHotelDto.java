package com.mjc.hotel.room.dto;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.room.entity.RoomIdCardEnum;
import com.mjc.hotel.room.entity.RoomPetAndSmokeEnum;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class RoomResponseNoHotelDto {
    private Long sid;
    private String roomTagTitle;
    private String roomPhotoPath;
    private String roomTypeTitle;
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
