package com.mjc.hotel.room.controller;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.mapper.RoomMapper;
import com.mjc.hotel.room.repository.RoomRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomMapper roomMapper;
    private final RoomRepository roomRepository;

    @Operation(
            summary = "객실 맵퍼 조회",
            description = "맵퍼를 이용해 객실을 조회합니다."
    )
    @GetMapping("/mapperRooms")
    public List<Room> getRooms() {
        return roomMapper.getRooms();
    }

    @Operation(
            summary = "객실 레포지토리 조회",
            description = "레포지토리를 이용해 객실을 조회합니다."
    )
    @GetMapping("/repositoryRooms")
    public List<Room> getRoomRepository() {
        return roomRepository.findAll();
    }
}
