package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.DiscountRate;
import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRateRepository extends JpaRepository<DiscountRate, Long> {
}
