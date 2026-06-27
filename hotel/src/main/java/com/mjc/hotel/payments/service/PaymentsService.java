package com.mjc.hotel.payments.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.dto.PaymentsRequestDto;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public List<Payments> getPayments() {
        return paymentsRepository.findAll();
    }

    public Payments getPayment(Long paymentId) {
        return paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. paymentId=" + paymentId));
    }

    @Transactional
    public Payments savePayment(PaymentsRequestDto dto) {
        return paymentsRepository.save(toEntity(dto));
    }

    @Transactional
    public Payments updatePayment(Long paymentId, PaymentsRequestDto dto) {
        Payments payment = getPayment(paymentId);
        if (dto.getReservationId() != null) {
            payment.setReservation(getReservation(dto.getReservationId()));
        }
        if (dto.getMemberId() != null) {
            payment.setMember(getMember(dto.getMemberId()));
        }
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setTransactionNo(dto.getTransactionNo());
        payment.setPaidAt(dto.getPaidAt());
        payment.setPoint(dto.getPoint());

        return payment;
    }

    @Transactional
    public void deletePayment(Long paymentId) {
        paymentsRepository.deleteById(paymentId);
    }

    private Payments toEntity(PaymentsRequestDto dto) {
        return Payments.builder()
                .reservation(getReservation(dto.getReservationId()))
                .member(getMember(dto.getMemberId()))
                .paymentAmount(dto.getPaymentAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(dto.getPaymentStatus())
                .transactionNo(dto.getTransactionNo())
                .paidAt(dto.getPaidAt())
                .point(dto.getPoint())
                .build();
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));
    }
}
