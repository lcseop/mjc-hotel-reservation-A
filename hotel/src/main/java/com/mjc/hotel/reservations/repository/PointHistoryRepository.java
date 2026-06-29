package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

}
