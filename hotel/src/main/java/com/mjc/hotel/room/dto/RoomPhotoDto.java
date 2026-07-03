package com.mjc.hotel.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoomPhotoDto {
    private Long sid;
    private Long roomId;
    private String imagePath;
}
