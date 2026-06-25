package com.mjc.hotel.reservations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_id")
    private Long emailLogId;

    // reservation_id FK → Reservation 객체 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "receipient_email", length = 100, nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    public enum EmailStatus {
        PENDING,
        SEND,
        FAILED
    }
}