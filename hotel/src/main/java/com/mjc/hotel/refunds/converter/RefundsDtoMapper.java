package com.mjc.hotel.refunds.converter;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.dto.RefundsResponseDto;
import com.mjc.hotel.refunds.entity.RefundStatus;
import com.mjc.hotel.refunds.entity.Refunds;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RefundsDtoMapper {

    public Refunds toEntity(RefundsRequestDto dto, Payments payment, Member member) {
        LocalDateTime requestedAt = dto.getRequestedAt() != null ? dto.getRequestedAt() : LocalDateTime.now();

        return Refunds.builder()
                .payment(payment)
                .member(member)
                .pgTransactionKey(dto.getPgTransactionKey())
                .idempotencyKey(dto.getIdempotencyKey())
                .refundAmount(dto.getRefundAmount())
                .reason(dto.getReason())
                .status(dto.getStatus())
                .requestedAt(requestedAt)
                .completedAt(resolveCompletedAt(dto, null))
                .failedAt(resolveFailedAt(dto, null))
                .failureReason(dto.getFailureReason())
                .build();
    }

    public LocalDateTime resolveRequestedAt(RefundsRequestDto dto, LocalDateTime currentRequestedAt) {
        if (dto.getRequestedAt() != null) {
            return dto.getRequestedAt();
        }
        if (currentRequestedAt != null) {
            return currentRequestedAt;
        }
        return LocalDateTime.now();
    }

    public LocalDateTime resolveCompletedAt(RefundsRequestDto dto, LocalDateTime currentCompletedAt) {
        if (dto.getCompletedAt() != null) {
            return dto.getCompletedAt();
        }
        if (currentCompletedAt != null) {
            return currentCompletedAt;
        }
        if (dto.getStatus() == RefundStatus.COMPLETED) {
            return LocalDateTime.now();
        }
        return null;
    }

    public LocalDateTime resolveFailedAt(RefundsRequestDto dto, LocalDateTime currentFailedAt) {
        if (dto.getFailedAt() != null) {
            return dto.getFailedAt();
        }
        if (currentFailedAt != null) {
            return currentFailedAt;
        }
        if (dto.getStatus() == RefundStatus.FAILED) {
            return LocalDateTime.now();
        }
        return null;
    }

    public RefundsResponseDto toResponseDto(Refunds refund) {
        return RefundsResponseDto.builder()
                .sid(refund.getSid())
                .paymentSid(refund.getPayment().getSid())
                .memberSid(refund.getMember().getSid())
                .pgTransactionKey(refund.getPgTransactionKey())
                .idempotencyKey(refund.getIdempotencyKey())
                .refundAmount(refund.getRefundAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .failedAt(refund.getFailedAt())
                .failureReason(refund.getFailureReason())
                .deleted(Boolean.TRUE.equals(refund.getDeleted()))
                .deletedAt(refund.getDeletedAt())
                .build();
    }
}
