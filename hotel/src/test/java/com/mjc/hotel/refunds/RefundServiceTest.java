package com.mjc.hotel.refunds;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.refunds.entity.RefundStatus;
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.repository.RefundsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefundServiceTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private RefundsRepository refundsRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("환불 정보를 저장할 수 있다")
    @Test
    @Transactional
    void addRefundTest() {
        Member member = memberRepository.findById(1L).orElseThrow();

        Payments payments = paymentsRepository.findById(1L).orElseThrow();

        Refunds refunds = Refunds
                .builder()
                .payment(payments)
                .member(member)
                .pgTransactionKey("PG-REFUND-TEST-20260625-001")
                .idempotencyKey("IDEMPOTENCY-TEST-20260625-001")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 부분 환불")
                .status(RefundStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .build();

        Refunds savedRefund = refundsRepository.saveAndFlush(refunds);

        assertThat(savedRefund.getSid()).isNotNull();
        assertThat(savedRefund.getRefundAmount()).isEqualByComparingTo("50000.00");
        assertThat(savedRefund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    }
}
