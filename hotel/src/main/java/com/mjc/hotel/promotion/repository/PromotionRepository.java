package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, PromotionRepositoryCustom {
    boolean existsByRoomTypeSid(Long sid);
    Long countByStartDateBeforeAndEndDateAfterAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<Promotion> findByConditionType(ConditionType conditionType);
}
