package com.mjc.hotel.room.repository;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Hotel hotel);
}
