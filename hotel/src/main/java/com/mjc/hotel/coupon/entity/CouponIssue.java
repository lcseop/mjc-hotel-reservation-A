package com.mjc.hotel.coupon.entity;


import com.mjc.hotel.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "coupon_issue")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sid;

    @JoinColumn(name="coupon_id",  nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    @JoinColumn(name="member_id",  nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name="is_used")
    private Boolean isUsed;

    @Column(name="used_at")
    private LocalDateTime usedAt;
}
