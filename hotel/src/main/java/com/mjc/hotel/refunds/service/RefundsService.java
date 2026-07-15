package com.mjc.hotel.refunds.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.refunds.converter.RefundsDtoMapper;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.entity.RefundStatus;
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.repository.RefundsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundsService {

    private final RefundsRepository refundsRepository;
    private final PaymentsRepository paymentsRepository;
    private final MemberRepository memberRepository;
    private final RefundsDtoMapper refundsDtoMapper;

    public List<Refunds> getRefunds() {
        return refundsRepository.findAll();
    }

    public Refunds getRefund(Long sid) {
        return refundsRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 환불입니다. sid=" + sid));
    }

    @Transactional
    public Refunds saveRefund(RefundsRequestDto dto) {
        return refundsRepository.save(
                refundsDtoMapper.toEntity(dto, getPayment(resolvePaymentSid(dto)), getMember(resolveMemberSid(dto)))
        );
    }

    @Transactional
    public Refunds updateRefund(Long sid, RefundsRequestDto dto) {
        Refunds refund = getRefund(sid);
        refund.setPayment(getPayment(resolvePaymentSid(dto)));
        refund.setMember(getMember(resolveMemberSid(dto)));
        refund.setPgTransactionKey(dto.getPgTransactionKey());
        refund.setIdempotencyKey(dto.getIdempotencyKey());
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setReason(dto.getReason());
        refund.setStatus(dto.getStatus());
        refund.setRequestedAt(refundsDtoMapper.resolveRequestedAt(dto, refund.getRequestedAt()));
        refund.setCompletedAt(refundsDtoMapper.resolveCompletedAt(dto, refund.getCompletedAt()));
        refund.setFailedAt(refundsDtoMapper.resolveFailedAt(dto, refund.getFailedAt()));
        refund.setFailureReason(dto.getFailureReason());

        return refund;
    }

    @Transactional
    public void deleteRefund(Long sid) {
        Refunds refund = getRefund(sid);
        refund.markDeleted();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createReservationCancelRefund(Long paymentSid, Long memberSid, BigDecimal refundAmount, String reason, String idempotencyKey) {
        Refunds refund = Refunds.builder()
                .payment(getPayment(paymentSid))
                .member(getMember(memberSid))
                .idempotencyKey(idempotencyKey)
                .refundAmount(refundAmount)
                .reason(reason)
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();
        return refundsRepository.save(refund).getSid();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeRefund(Long refundSid, String pgTransactionKey) {
        Refunds refund = getRefund(refundSid);
        refund.setPgTransactionKey(pgTransactionKey);
        refund.setStatus(RefundStatus.COMPLETED);
        refund.setCompletedAt(LocalDateTime.now());
        refund.setFailedAt(null);
        refund.setFailureReason(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failRefund(Long refundSid, String failureReason) {
        Refunds refund = getRefund(refundSid);
        refund.setStatus(RefundStatus.FAILED);
        refund.setFailedAt(LocalDateTime.now());
        refund.setFailureReason(failureReason);
    }

    private Payments getPayment(Long sid) {
        return paymentsRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. sid=" + sid));
    }

    private Member getMember(Long sid) {
        return memberRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. sid=" + sid));
    }

    private Long resolvePaymentSid(RefundsRequestDto dto) {
        return dto.getPaymentSid() != null ? dto.getPaymentSid() : dto.getSid();
    }

    private Long resolveMemberSid(RefundsRequestDto dto) {
        return dto.getMemberSid() != null ? dto.getMemberSid() : dto.getSid();
    }
}
