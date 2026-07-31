package com.mjc.hotel.coupon.repository;

import com.mjc.hotel.coupon.entity.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
    void deleteByCouponSid(Long couponSid);

    List<CouponIssue> findByMemberSid(Long memberId);
}
