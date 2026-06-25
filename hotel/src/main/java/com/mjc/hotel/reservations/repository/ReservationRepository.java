package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}
