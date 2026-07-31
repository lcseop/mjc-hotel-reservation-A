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
                .orderId(dto.getOrderId())
                .paymentKey(dto.getPaymentKey())
                .provider(dto.getProvider())
                .receiptUrl(dto.getReceiptUrl())
                .failCode(dto.getFailCode())
                .failMessage(dto.getFailMessage())
                .requestedAt(dto.getRequestedAt())
                .approvedAt(dto.getApprovedAt())
                .paidAt(dto.getPaidAt() != null ? dto.getPaidAt() : LocalDateTime.now())
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
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .provider(payment.getProvider())
                .receiptUrl(payment.getReceiptUrl())
                .failCode(payment.getFailCode())
                .failMessage(payment.getFailMessage())
                .paidAt(payment.getPaidAt())
                .requestedAt(payment.getRequestedAt())
                .approvedAt(payment.getApprovedAt())
                .point(payment.getPoint())
                .deleted(Boolean.TRUE.equals(payment.getDeleted()))
                .deletedAt(payment.getDeletedAt())
                .build();
    }
}
