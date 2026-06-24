package com.mjc.hotel.room.repository;

import com.mjc.hotel.room.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
}
