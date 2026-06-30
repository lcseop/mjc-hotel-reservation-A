package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Condition;
import com.mjc.hotel.promotion.entity.ConditionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
    // ConditionType을 조건으로 검색하는 메서드
    List<Condition> findByConditiontype(ConditionType conditiontype);
}
