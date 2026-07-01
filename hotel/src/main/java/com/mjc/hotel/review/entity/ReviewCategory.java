package com.mjc.hotel.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_category")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_category_master_id")
    private ReviewCategoryMaster reviewCategoryMaster;

    @Column(name = "rating", nullable = false)
    private Integer rating;
}
