package com.mjc.hotel.reservations.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_cancels")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReservationCancel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    // reservation_id FK → Reservation 객체 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "refund_amout")
    private Integer refundAmount;

    @CreationTimestamp
    @Column(name = "cancelled_at", updatable = false)
    private LocalDateTime cancelledAt;
}
