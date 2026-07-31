package com.mjc.hotel.payments.repository;

import com.mjc.hotel.payments.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByOrderId(String orderId);

    Optional<Payments> findByPaymentKey(String paymentKey);

    List<Payments> findByReservationSidOrderByCreatedAtDesc(Long reservationSid);
}
