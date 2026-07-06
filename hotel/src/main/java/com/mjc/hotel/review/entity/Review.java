package com.mjc.hotel.review.entity;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.review.entity.enums.TravelType;
import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "review")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_type", nullable = true)
    private TravelType travelType;

    @Column(name = "content",  nullable = false, length = 1000)
    private String content;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount; //null 못들어가게 기본형

    @Column(name = "dislike_count", nullable = false)
    private Integer dislikeCount;

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void increaseDislike() {
        this.dislikeCount++;
    }

    public void decreaseDislike() {
        if (this.dislikeCount > 0) this.dislikeCount--;
    }
}
