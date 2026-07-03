package com.mjc.hotel.room.repository;

import com.mjc.hotel.room.entity.RoomInTag;
import com.mjc.hotel.room.entity.RoomTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomInTagRepository extends JpaRepository<RoomInTag, Long> {
    List<RoomInTag> findByRoomSid(Long sid);
    void deleteByRoomSid(Long sid);
    void deleteByTagSid(Long sid);
}
