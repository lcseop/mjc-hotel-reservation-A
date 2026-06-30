package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
