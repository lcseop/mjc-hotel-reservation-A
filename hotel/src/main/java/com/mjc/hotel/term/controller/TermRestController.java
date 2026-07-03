package com.mjc.hotel.term.controller;

import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.service.TermService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/term")
@RequiredArgsConstructor
public class TermRestController {

    private final TermService termService;

    @Operation(
            summary = "약관 데이터 생성",
            description = "약관 데이터를 만듭니다."
    )
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<TermResponseDto>> insert(@RequestBody TermRequestDto dto) {
        TermResponseDto insert = termService.insert(dto);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "term insert success", insert)
        );
    }

    @Operation(
            summary = "약관 목록 조회",
            description = "약관 데이터를 모두 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<TermResponseDto>>> getTerms() {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "term select success", termService.getTerms())
        );
    }

    @Operation(
            summary = "약관 단건 조회",
            description = "약관 데이터를 하나 조회합니다."
    )
    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<TermResponseDto>> getTerm(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "term select success", termService.getTerm(sid))
        );
    }

    @Operation(
            summary = "약관 데이터 수정",
            description = "약관 데이터를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<TermResponseDto>> update(
            @RequestBody TermRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "term update success", termService.updateTerm(dto.getSid(), dto))
        );
    }

    @Operation(
            summary = "약관 데이터 삭제",
            description = "약관 데이터를 삭제합니다."
    )
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        termService.deleteTerm(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "term delete success", null)
        );
    }
}
