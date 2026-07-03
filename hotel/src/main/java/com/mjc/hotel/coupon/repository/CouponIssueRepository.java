package com.mjc.hotel.coupon.repository;

import com.mjc.hotel.coupon.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
    void deleteByCouponId(Long couponId);
}
