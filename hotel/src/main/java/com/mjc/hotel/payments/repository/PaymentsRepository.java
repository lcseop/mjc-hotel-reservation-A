package com.mjc.hotel.payments.repository;

import com.mjc.hotel.payments.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

}
