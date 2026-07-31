package com.mjc.hotel.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_category_master")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewCategoryMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @Column(name = "review_category_name", nullable = false, length = 10, unique = true)
    private String reviewCategoryName;
}
