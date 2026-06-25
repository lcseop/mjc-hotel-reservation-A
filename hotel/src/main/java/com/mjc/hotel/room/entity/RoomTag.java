package com.mjc.hotel.room.entity;

import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "room_tag")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(length = 15, nullable = false)
    private String title;
}
