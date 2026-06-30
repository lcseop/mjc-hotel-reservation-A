package com.mjc.hotel.payments.converter;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.payments.dto.PaymentsRequestDto;
import com.mjc.hotel.payments.dto.PaymentsResponseDto;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.reservations.entity.Reservation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentsDtoMapper {

    public Payments toEntity(PaymentsRequestDto dto, Reservation reservation, Member member) {
        return Payments.builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(dto.getPaymentAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(dto.getPaymentStatus())
                .transactionNo(dto.getTransactionNo())
                .paidAt(LocalDateTime.now())
                .point(dto.getPoint())
                .build();
    }

    public PaymentsResponseDto toResponseDto(Payments payment) {
        return PaymentsResponseDto.builder()
                .sid(payment.getSid())
                .reservationId(payment.getReservation().getSid())
                .memberSid(payment.getMember().getSid())
                .paymentAmount(payment.getPaymentAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionNo(payment.getTransactionNo())
                .paidAt(payment.getPaidAt())
                .point(payment.getPoint())
                .deleted(Boolean.TRUE.equals(payment.getDeleted()))
                .deletedAt(payment.getDeletedAt())
                .build();
    }
}
