package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    void deleteByPromotion_Sid(Long promotionId);
}
