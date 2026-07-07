package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberAuthAccountRequestDto;
import com.mjc.hotel.member.dto.MemberAuthAccountResponseDto;
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
@Tag(name = "로그인 인증 정보", description = "로그인 인증 정보를 관리합니다.")
public class MemberAuthAccountRestController {

    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

    @Operation(
            summary = "로그인 인증 정보 생성",
            description = "로그인 인증 정보를 만듭니다."
    )

    @PostMapping("/api/member-auth-accounts")
    public ResponseEntity<ApiResponse<MemberAuthAccountResponseDto>> insertAuthAccount(
            @RequestBody MemberAuthAccountRequestDto request
    ) {
        return ResponseEntity.status(201).body(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member auth account insert success",
                        memberDtoMapper.toAuthAccountResponseDto(memberService.createAuthAccount(request))
                )
        );
    }

    @Operation(
            summary = "로그인 인증 정보 단일 조회",
            description = "로그인 인증 정보 하나를 조회합니다."
    )

    @GetMapping("/api/member-auth-accounts/{sid}")
    public ResponseEntity<ApiResponse<MemberAuthAccountResponseDto>> getAuthAccount(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member auth account select success",
                        memberDtoMapper.toAuthAccountResponseDto(memberService.getAuthAccount(sid))
                )
        );
    }

    @Operation(
            summary = "로그인 인증 정보 전체 조회",
            description = "로그인 인증 정보 전체를 조회합니다.."
    )

    @GetMapping("/api/member/{memberSid}/auth-accounts")
    public ResponseEntity<ApiResponse<List<MemberAuthAccountResponseDto>>> getAuthAccountsByMember(
            @PathVariable Long memberSid
    ) {
        List<MemberAuthAccountResponseDto> authAccounts = memberService.getAuthAccountsByMember(memberSid).stream()
                .map(memberDtoMapper::toAuthAccountResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member auth account select success", authAccounts)
        );
    }

    @Operation(
            summary = "로그인 인증 정보 수정",
            description = "로그인 인증 정보를 수정합니다."
    )

    @PatchMapping("/api/member-auth-accounts")
    public ResponseEntity<ApiResponse<MemberAuthAccountResponseDto>> updateAuthAccount(
            @RequestBody MemberAuthAccountRequestDto request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member auth account update success",
                        memberDtoMapper.toAuthAccountResponseDto(memberService.updateAuthAccount(request.getSid(), request))
                )
        );
    }

    @Operation(
            summary = "로그인 인증 정보 삭제",
            description = "로그인 인증 정보를 삭제합니다."
    )

    @DeleteMapping("/api/member-auth-accounts/{sid}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthAccount(@PathVariable Long sid) {
        memberService.deleteAuthAccount(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member auth account delete success", null)
        );
    }
}
