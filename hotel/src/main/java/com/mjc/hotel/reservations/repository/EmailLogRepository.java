package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
}
