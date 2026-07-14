package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, PromotionRepositoryCustom {
    boolean existsByRoomTypeSid(Long sid);
    Long countByStartDateBeforeAndEndDateAfterAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<Promotion> findByConditionType(ConditionType conditionType);

    @Query("""
        SELECT p FROM promotion p
        WHERE p.roomType.sid = :roomTypeId
        AND p.conditionType = :conditionType
        AND (p.deleted IS NULL OR p.deleted = false)
        AND (p.startDate IS NULL OR p.startDate <= :now)
        AND (p.endDate IS NULL OR p.endDate >= :now)
    """)
    List<Promotion> findActivePromotionsByRoomType(
            @Param("roomTypeId") Long roomTypeId,
            @Param("conditionType") ConditionType conditionType,
            @Param("now") LocalDateTime now
    );
}
