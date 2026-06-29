package com.mjc.hotel.payments.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.converter.PaymentsDtoMapper;
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
    private final PaymentsDtoMapper paymentsDtoMapper;

    public List<Payments> getPayments() {
        return paymentsRepository.findAll();
    }

    public Payments getPayment(Long sid) {
        return paymentsRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. sid=" + sid));
    }

    @Transactional
    public Payments savePayment(PaymentsRequestDto dto) {
        return paymentsRepository.save(
                paymentsDtoMapper.toEntity(dto, getReservation(dto.getReservationId()), getMember(dto.getSid()))
        );
    }

    @Transactional
    public Payments updatePayment(Long sid, PaymentsRequestDto dto) {
        Payments payment = getPayment(sid);
        if (dto.getReservationId() != null) {
            payment.setReservation(getReservation(dto.getReservationId()));
        }
        if (dto.getSid() != null) {
            payment.setMember(getMember(dto.getSid()));
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
    public void deletePayment(Long sid) {
        paymentsRepository.deleteById(sid);
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));
    }

    private Member getMember(Long sid) {
        return memberRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. sid=" + sid));
    }
}
