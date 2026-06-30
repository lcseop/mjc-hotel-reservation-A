package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Promotion;
import com.mjc.hotel.promotion.entity.PromotionPackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionPackageRepository extends JpaRepository<PromotionPackage, Long> {
}
