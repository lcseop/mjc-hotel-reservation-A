package com.mjc.hotel.refunds.controller;

import com.mjc.hotel.refunds.converter.RefundsDtoMapper;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.dto.RefundsResponseDto;
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

    @Autowired
    private RefundsDtoMapper refundsDtoMapper;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> insert(@RequestBody RefundsRequestDto dto) {
        RefundsResponseDto insert = refundsDtoMapper.toResponseDto(refundsService.saveRefund(dto));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds insert success", insert)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundsResponseDto>>> getRefunds() {
        List<RefundsResponseDto> refunds = refundsService.getRefunds().stream()
                .map(refundsDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", refunds)
        );
    }

    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> getRefund(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", refundsDtoMapper.toResponseDto(refundsService.getRefund(sid)))
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<RefundsResponseDto>> update(
            @RequestBody RefundsRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds update success", refundsDtoMapper.toResponseDto(refundsService.updateRefund(dto.getSid(), dto)))
        );
    }

    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        refundsService.deleteRefund(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds delete success", null)
        );
    }
}
