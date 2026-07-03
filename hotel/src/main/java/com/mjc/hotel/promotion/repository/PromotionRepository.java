package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, PromotionRepositoryCustom {
    boolean existsByRoomTypeSid(Long sid);
}
