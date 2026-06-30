package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberAuthAccountRequestDto;
import com.mjc.hotel.member.dto.MemberAuthAccountResponseDto;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberAuthAccountRestController {

    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;

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

    @PutMapping("/api/member-auth-accounts/{sid}")
    public ResponseEntity<ApiResponse<MemberAuthAccountResponseDto>> updateAuthAccount(
            @PathVariable Long sid,
            @RequestBody MemberAuthAccountRequestDto request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "member auth account update success",
                        memberDtoMapper.toAuthAccountResponseDto(memberService.updateAuthAccount(sid, request))
                )
        );
    }

    @DeleteMapping("/api/member-auth-accounts/{sid}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthAccount(@PathVariable Long sid) {
        memberService.deleteAuthAccount(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member auth account delete success", null)
        );
    }
}
