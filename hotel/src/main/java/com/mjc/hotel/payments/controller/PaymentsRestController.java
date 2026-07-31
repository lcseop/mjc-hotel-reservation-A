package com.mjc.hotel.payments.controller;

import com.mjc.hotel.payments.converter.PaymentsDtoMapper;
import com.mjc.hotel.payments.dto.PaymentsRequestDto;
import com.mjc.hotel.payments.dto.PaymentsResponseDto;
import com.mjc.hotel.payments.dto.TossPaymentConfirmRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentFailRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentReadyRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentReadyResponseDto;
import com.mjc.hotel.payments.service.PaymentsService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag( name = "결제", description = "결제 데이터 전반을 관리합니다.")
public class PaymentsRestController {

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsDtoMapper paymentsDtoMapper;

    @Operation(
            summary = "결제 데이터 생성",
            description = "결제 데이터를 만듭니다."
    )

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> insert(@RequestBody PaymentsRequestDto dto) {
        PaymentsResponseDto insert = paymentsDtoMapper.toResponseDto(paymentsService.savePayment(dto));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments insert success", insert)
        );
    }

    @Operation(
            summary = "토스 결제 요청 생성",
            description = "토스 결제창 호출 전 결제 요청 정보를 저장합니다."
    )
    @PostMapping("/toss/ready")
    public ResponseEntity<ApiResponse<TossPaymentReadyResponseDto>> readyTossPayment(@RequestBody TossPaymentReadyRequestDto dto) {
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "toss payment ready success", paymentsService.readyTossPayment(dto))
        );
    }

    @Operation(
            summary = "토스 결제 승인",
            description = "토스 결제 성공 후 paymentKey, orderId, amount를 검증하고 승인합니다."
    )
    @PostMapping("/toss/confirm")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> confirmTossPayment(@RequestBody TossPaymentConfirmRequestDto dto) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "toss payment confirm success", paymentsDtoMapper.toResponseDto(paymentsService.confirmTossPayment(dto)))
        );
    }

    @Operation(
            summary = "토스 결제 실패 기록",
            description = "토스 결제 실패 또는 취소 정보를 저장합니다."
    )
    @PostMapping("/toss/fail")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> failTossPayment(@RequestBody TossPaymentFailRequestDto dto) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "toss payment fail saved", paymentsDtoMapper.toResponseDto(paymentsService.failTossPayment(dto)))
        );
    }

    @Operation(
            summary = "결제 전체 데이터 조회",
            description = "결제 전체 데이터를 조회합니다."
    )

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentsResponseDto>>> getPayments() {
        List<PaymentsResponseDto> payments = paymentsService.getPayments().stream()
                .map(paymentsDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments select success", payments)
        );
    }

    @Operation(
            summary = "결제 데이터 단일 조회",
            description = "결제 데이터 한개를 조회합니다."
    )

    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> getPayment(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments select success", paymentsDtoMapper.toResponseDto(paymentsService.getPayment(sid)))
        );
    }

    @Operation(
            summary = "결제 데이터 수정",
            description = "결제 데이터를 수정합니다."
    )

    @PatchMapping
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> update(
            @RequestBody PaymentsRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments update success", paymentsDtoMapper.toResponseDto(paymentsService.updatePayment(dto.getSid(), dto)))
        );
    }

    @Operation(
            summary = "결제 데이터 삭제",
            description = "결제 데이터를 삭제합니다."
    )

    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        paymentsService.deletePayment(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments delete success", null)
        );
    }
}
