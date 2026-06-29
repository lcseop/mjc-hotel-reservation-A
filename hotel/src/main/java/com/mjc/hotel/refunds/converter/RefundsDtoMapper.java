package com.mjc.hotel.refunds.converter;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.dto.RefundsResponseDto;
import com.mjc.hotel.refunds.entity.Refunds;
import org.springframework.stereotype.Component;

@Component
public class RefundsDtoMapper {

    public Refunds toEntity(RefundsRequestDto dto, Payments payment, Member member) {
        return Refunds.builder()
                .payment(payment)
                .member(member)
                .pgTransactionKey(dto.getPgTransactionKey())
                .idempotencyKey(dto.getIdempotencyKey())
                .refundAmount(dto.getRefundAmount())
                .reason(dto.getReason())
                .status(dto.getStatus())
                .requestedAt(dto.getRequestedAt())
                .completedAt(dto.getCompletedAt())
                .failedAt(dto.getFailedAt())
                .failureReason(dto.getFailureReason())
                .build();
    }

    public RefundsResponseDto toResponseDto(Refunds refund) {
        return RefundsResponseDto.builder()
                .refundId(refund.getRefundId())
                .paymentId(refund.getPayment().getPaymentId())
                .memberId(refund.getMember().getMemberId())
                .pgTransactionKey(refund.getPgTransactionKey())
                .idempotencyKey(refund.getIdempotencyKey())
                .refundAmount(refund.getRefundAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .failedAt(refund.getFailedAt())
                .failureReason(refund.getFailureReason())
                .build();
    }
}
