package com.mjc.hotel.review.entity;

import com.mjc.hotel.review.entity.composite_key.ReviewTagId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "review_tag")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@IdClass(ReviewTagId.class)
public class ReviewTag {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_tag_master_id")
    private ReviewTagMaster reviewTagMaster;
}
