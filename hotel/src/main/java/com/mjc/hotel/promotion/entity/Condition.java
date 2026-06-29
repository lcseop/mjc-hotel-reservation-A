package com.mjc.hotel.promotion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "condition")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name="promotion_id",  nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name="condition")
    private ConditionType conditiontype;
}
