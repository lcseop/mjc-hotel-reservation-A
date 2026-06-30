package com.mjc.hotel.review.entity;

import com.mjc.hotel.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_answer")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "review_answer", columnDefinition = "TEXT", nullable = false)
    private String reviewAnswer;
}
