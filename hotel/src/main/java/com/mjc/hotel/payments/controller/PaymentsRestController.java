package com.mjc.hotel.payments.controller;

import com.mjc.hotel.payments.converter.PaymentsDtoMapper;
import com.mjc.hotel.payments.dto.PaymentsRequestDto;
import com.mjc.hotel.payments.dto.PaymentsResponseDto;
import com.mjc.hotel.payments.service.PaymentsService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentsRestController {

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsDtoMapper paymentsDtoMapper;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> insert(@RequestBody PaymentsRequestDto dto) {
        PaymentsResponseDto insert = paymentsDtoMapper.toResponseDto(paymentsService.savePayment(dto));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments insert success", insert)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentsResponseDto>>> getPayments() {
        List<PaymentsResponseDto> payments = paymentsService.getPayments().stream()
                .map(paymentsDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments select success", payments)
        );
    }

    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> getPayment(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments select success", paymentsDtoMapper.toResponseDto(paymentsService.getPayment(sid)))
        );
    }

    @PutMapping("/{sid}")
    public ResponseEntity<ApiResponse<PaymentsResponseDto>> update(
            @PathVariable Long sid,
            @RequestBody PaymentsRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments update success", paymentsDtoMapper.toResponseDto(paymentsService.updatePayment(sid, dto)))
        );
    }

    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        paymentsService.deletePayment(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "payments delete success", null)
        );
    }
}
