package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    // Condition 엔티티의 conditionType을 기준으로 프로모션 목록 조회
    List<Promotion> findByCondition_Conditiontype(ConditionType conditionType);
}
