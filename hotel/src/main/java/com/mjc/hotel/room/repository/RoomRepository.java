package com.mjc.hotel.room.repository;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
    SELECT r FROM room r
    WHERE r.hotelId = :hotel
    AND (r.deleted IS NULL OR r.deleted = false)
""")
    List<Room> findActiveRooms(@Param("hotel") Hotel hotel);

    List<Room> findByHotelIdSid(Long hotelId);

    List<Room> findByRoomTypeIdSid(Long typeId);
}
