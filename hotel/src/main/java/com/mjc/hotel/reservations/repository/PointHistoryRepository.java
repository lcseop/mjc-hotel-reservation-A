package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.PointHistory;
import com.mjc.hotel.reservations.entity.PointStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Page<PointHistory> findByMemberSidOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<PointHistory> findByReservationSidAndPointStatus(Long reservationId, PointStatus pointStatus);
}
