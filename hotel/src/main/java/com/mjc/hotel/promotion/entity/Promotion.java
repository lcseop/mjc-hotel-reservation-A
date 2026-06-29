package com.mjc.hotel.promotion.entity;

import com.mjc.hotel.room.entity.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "promotion")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private RoomType roomType ;

    @Column(length = 50, name="promotion_name")
    private String promotionName;

    @Column(name="star_rating")
    private Integer starRating;

    @Column(name="how_long")
    private LocalDateTime howLong;

    @Column(name="total_amount")
    private Integer totalAmount;

    public void update(String promotionName, RoomType roomType) {
    }
}
