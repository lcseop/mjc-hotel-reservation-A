package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByReservation_Sid(Long reservationSid);
}
