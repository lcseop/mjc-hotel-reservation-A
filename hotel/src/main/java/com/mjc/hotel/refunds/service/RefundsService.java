package com.mjc.hotel.refunds.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.refunds.converter.RefundsDtoMapper;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.repository.RefundsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Refunds getRefund(Long refundId) {
        return refundsRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 환불입니다. refundId=" + refundId));
    }

    @Transactional
    public Refunds saveRefund(RefundsRequestDto dto) {
        return refundsRepository.save(
                refundsDtoMapper.toEntity(dto, getPayment(dto.getPaymentId()), getMember(dto.getMemberId()))
        );
    }

    @Transactional
    public Refunds updateRefund(Long refundId, RefundsRequestDto dto) {
        Refunds refund = getRefund(refundId);
        refund.setPayment(getPayment(dto.getPaymentId()));
        refund.setMember(getMember(dto.getMemberId()));
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
    public void deleteRefund(Long refundId) {
        refundsRepository.deleteById(refundId);
    }

    private Payments getPayment(Long paymentId) {
        return paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. paymentId=" + paymentId));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId=" + memberId));
    }
}
