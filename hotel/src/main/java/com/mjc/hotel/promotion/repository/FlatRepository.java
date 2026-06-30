package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.entity.Flat;
import com.mjc.hotel.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatRepository extends JpaRepository<Flat, Long> {
}
