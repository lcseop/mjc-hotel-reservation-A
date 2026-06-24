package com.mjc.hotel.room.repository;

import com.mjc.hotel.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
