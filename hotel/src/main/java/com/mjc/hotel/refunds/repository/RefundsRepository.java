package com.mjc.hotel.refunds.repository;

import com.mjc.hotel.refunds.entity.Refunds;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundsRepository extends JpaRepository<Refunds, Long> {

    List<Refunds> findByPaymentSidOrderByCreatedAtDesc(Long paymentSid);
}
