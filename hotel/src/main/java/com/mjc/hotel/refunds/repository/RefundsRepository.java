package com.mjc.hotel.refunds.repository;

import com.mjc.hotel.refunds.entity.Refunds;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundsRepository extends JpaRepository<Refunds, Long> {
}
