package com.mjc.hotel.coupon.repository;

import com.mjc.hotel.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
