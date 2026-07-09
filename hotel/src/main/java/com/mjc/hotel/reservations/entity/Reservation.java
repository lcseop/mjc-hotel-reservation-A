package com.mjc.hotel.reservations.entity;

import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    // member_id FK → Member 객체 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // room_id FK → Room 객체 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_issue_id")
    private CouponIssue couponIssue;

    @Column(name = "reservation_number", length = 255, nullable = false)
    private String reservationNumber;

    @Column(name = "check_in_date", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDateTime checkOutDate;

    @Column(nullable = false)
    private Integer adults;

    @Column
    private Integer children;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @Column(name = "original_Amount", nullable = false)
    private Integer originalAmount;

    @Column(name = "discount_Amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "coupon_discount")
    @Builder.Default
    private Integer couponDiscount = 0;

    @Column(name = "point_discount")
    @Builder.Default
    private Integer pointDiscount = 0;

    @Column(name = "earned_Point")
    @Builder.Default
    private Integer earnedPoint = 0;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "check_in_qr", columnDefinition = "TEXT")
    private String checkInQr;

    @Column(name = "total_nights")
    private Integer totalNights;

    @Column(name = "guest_name", length = 50)
    private String guestName;
}
