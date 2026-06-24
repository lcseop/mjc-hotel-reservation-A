package com.mjc.hotel.room.controller;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.mapper.RoomMapper;
import com.mjc.hotel.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomMapper roomMapper;
    private final RoomRepository roomRepository;

    @GetMapping("/mapperRooms")
    public List<Room> getRooms() {
        return roomMapper.getRooms();
    }

    @GetMapping("/repositoryRooms")
    public List<Room> getRoomRepository() {
        return roomRepository.findAll();
    }
}
