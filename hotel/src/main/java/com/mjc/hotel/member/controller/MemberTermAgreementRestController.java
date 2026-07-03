package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberTermAgreementRequestDto;
import com.mjc.hotel.member.dto.MemberTermAgreementResponseDto;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
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
public class MemberTermAgreementRestController {

    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

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

    @DeleteMapping("/api/member-term-agreements/{sid}")
    public ResponseEntity<ApiResponse<Void>> deleteTermAgreement(@PathVariable Long sid) {
        memberService.deleteTermAgreement(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member term agreement delete success", null)
        );
    }
}
