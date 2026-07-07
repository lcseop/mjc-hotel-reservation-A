package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberTermAgreementRequestDto;
import com.mjc.hotel.member.dto.MemberTermAgreementResponseDto;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag( name = "회원 약관 동의", description = "회원 약관 동의를 관리합니다.")
public class MemberTermAgreementRestController {

    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

    @Operation(
            summary = "회원 약관 동의 생성",
            description = "회원 약관 동의를 만듭니다."
    )

    @PostMapping("/api/member-term-agreements")
    public ResponseEntity<ApiResponse<MemberTermAgreementResponseDto>> insertTermAgreement(
            @RequestBody MemberTermAgreementRequestDto request
    ) {
        return ResponseEntity.status(201).body(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member term agreement insert success",
                        memberDtoMapper.toTermAgreementResponseDto(memberService.createTermAgreement(request))
                )
        );
    }

    @Operation(
            summary = "단일 회원 약관 동의 조회",
            description = "단일 회원의 약관 동의를 조회합니다."
    )

    @GetMapping("/api/member-term-agreements/{sid}")
    public ResponseEntity<ApiResponse<MemberTermAgreementResponseDto>> getTermAgreement(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member term agreement select success",
                        memberDtoMapper.toTermAgreementResponseDto(memberService.getTermAgreement(sid))
                )
        );
    }

    @Operation(
            summary = "전체 회원 약관 동의 조회",
            description = "전체 회원의 약관 동의를 조회합니다."
    )

    @GetMapping("/api/member/{memberSid}/term-agreements")
    public ResponseEntity<ApiResponse<List<MemberTermAgreementResponseDto>>> getTermAgreementsByMember(
            @PathVariable Long memberSid
    ) {
        List<MemberTermAgreementResponseDto> termAgreements = memberService.getTermAgreementsByMember(memberSid).stream()
                .map(memberDtoMapper::toTermAgreementResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member term agreement select success", termAgreements)
        );
    }

    @Operation(
            summary = "회원 약관 동의 수정",
            description = "회원 약관 동의 데이터를 수정합니다."
    )

    @PatchMapping("/api/member-term-agreements")
    public ResponseEntity<ApiResponse<MemberTermAgreementResponseDto>> updateTermAgreement(
            @RequestBody MemberTermAgreementRequestDto request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member term agreement update success",
                        memberDtoMapper.toTermAgreementResponseDto(memberService.updateTermAgreement(request.getSid(), request))
                )
        );
    }

    @Operation(
            summary = "회원 약관 동의 데이터 삭제",
            description = "회원 약관 동의 데이터를 삭제합니다."
    )

    @DeleteMapping("/api/member-term-agreements/{sid}")
    public ResponseEntity<ApiResponse<Void>> deleteTermAgreement(@PathVariable Long sid) {
        memberService.deleteTermAgreement(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member term agreement delete success", null)
        );
    }
}
