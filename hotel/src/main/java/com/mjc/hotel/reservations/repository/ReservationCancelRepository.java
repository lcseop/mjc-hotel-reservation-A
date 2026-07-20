package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.ReservationCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCancelRepository extends JpaRepository<ReservationCancel, Long> {

    boolean existsByReservationSid(Long reservationId);
}
