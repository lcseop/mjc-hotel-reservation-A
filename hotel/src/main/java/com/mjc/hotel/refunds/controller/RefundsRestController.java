package com.mjc.hotel.refunds.controller;

import com.mjc.hotel.refunds.converter.RefundsDtoMapper;
import com.mjc.hotel.refunds.dto.RefundsRequestDto;
import com.mjc.hotel.refunds.dto.RefundsResponseDto;
import com.mjc.hotel.refunds.service.RefundsService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@Tag( name = "환불", description = "환불 데이터 전반을 관리합니다.")
public class RefundsRestController {

    @Autowired
    private RefundsService refundsService;

    @Autowired
    private RefundsDtoMapper refundsDtoMapper;

    @Operation(
            summary = "환불 데이터 생성",
            description = "환불 데이터를 만듭니다."
    )

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> insert(@RequestBody RefundsRequestDto dto) {
        RefundsResponseDto insert = refundsDtoMapper.toResponseDto(refundsService.saveRefund(dto));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds insert success", insert)
        );
    }

    @Operation(
            summary = "환불 전체 데이터 조회",
            description = "환불 전체 데이터를 조회합니다."
    )

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundsResponseDto>>> getRefunds() {
        List<RefundsResponseDto> refunds = refundsService.getRefunds().stream()
                .map(refundsDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", refunds)
        );
    }

    @Operation(
            summary = "환불 단일 데이터 조회",
            description = "환불 데이터 한개를 조회합니다."
    )

    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<RefundsResponseDto>> getRefund(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds select success", refundsDtoMapper.toResponseDto(refundsService.getRefund(sid)))
        );
    }

    @Operation(
            summary = "환불 데이터 수정",
            description = "환불 데이터를 수정합니다."
    )

    @PatchMapping
    public ResponseEntity<ApiResponse<RefundsResponseDto>> update(
            @RequestBody RefundsRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds update success", refundsDtoMapper.toResponseDto(refundsService.updateRefund(dto.getSid(), dto)))
        );
    }

    @Operation(
            summary = "환불 데이터 삭제",
            description = "환불 데이터를 삭제합니다."
    )

    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        refundsService.deleteRefund(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "refunds delete success", null)
        );
    }
}
