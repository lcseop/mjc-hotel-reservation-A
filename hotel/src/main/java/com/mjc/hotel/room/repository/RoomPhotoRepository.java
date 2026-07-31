package com.mjc.hotel.room.repository;

import com.mjc.hotel.room.entity.RoomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Long> {
    List<RoomPhoto> findByRoomSid(Long id);
}
