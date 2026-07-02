package com.mjc.hotel.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoomInTagDto {
    private Long sid;
    private Long roomId;
    private Long roomTagId;
}
