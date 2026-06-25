package com.mjc.hotel.promotion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "discount_rate")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DiscountRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name="promotion_id",  nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Promotion promotion;

    @Column(name="type")
    private String type;

    @Column(name="sale")
    private Integer sale;
}
