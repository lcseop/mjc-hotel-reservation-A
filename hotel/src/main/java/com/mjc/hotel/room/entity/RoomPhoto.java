package com.mjc.hotel.room.entity;

import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "room_photo")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoomPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(name = "image_path", length = 255, nullable = false)
    private String imagePath;
}
