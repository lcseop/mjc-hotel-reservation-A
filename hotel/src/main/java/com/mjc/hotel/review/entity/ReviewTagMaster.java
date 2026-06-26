package com.mjc.hotel.review.entity;

import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_tag_master")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReviewTagMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewTagMasterId;

    @Column(name = "review_tag_name", nullable = false, length = 10,unique = true)
    private String reviewTagName;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_tag_category", nullable = false)
    private ReviewTagCategory reviewTagCategory;
}
