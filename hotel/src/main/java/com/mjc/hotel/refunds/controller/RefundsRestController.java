package com.mjc.hotel.refunds.controller;

import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.dto.RefundsResponseDto;
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.service.RefundsService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundsRestController {

    @Autowired
    private RefundsService refundsService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> insert(@RequestBody RefundsRequestDto dto) {
        RefundsResponseDto insert = toResponseDto(refundsService.saveRefund(dto));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds insert success", insert)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundsResponseDto>>> getRefunds() {
        List<RefundsResponseDto> refunds = refundsService.getRefunds().stream()
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", refunds)
        );
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> getRefund(@PathVariable Long refundId) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", toResponseDto(refundsService.getRefund(refundId)))
        );
    }

    @PutMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> update(
            @PathVariable Long refundId,
            @RequestBody RefundsRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds update success", toResponseDto(refundsService.updateRefund(refundId, dto)))
        );
    }

    @DeleteMapping("/{refundId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long refundId) {
        refundsService.deleteRefund(refundId);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds delete success", null)
        );
    }

    private RefundsResponseDto toResponseDto(Refunds refund) {
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
