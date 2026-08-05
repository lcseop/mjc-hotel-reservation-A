package com.mjc.hotel.payments;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.entity.PaymentMethod;
import com.mjc.hotel.payments.entity.PaymentStatus;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("결제 정보를 저장할 수 있다")
    @Test
    @Transactional
    void addPaymentTest() {
        Member member = memberRepository.findById(1L).orElseThrow();
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Payments payments = Payments
                .builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionNo("TXN-TEST-20260625-001")
                .paidAt(LocalDateTime.now())
                .point(1800)
                .build();

        Payments savedPayment = paymentsRepository.saveAndFlush(payments);

        assertThat(savedPayment.getSid()).isNotNull();
        assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo("180000.00");
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }
}
