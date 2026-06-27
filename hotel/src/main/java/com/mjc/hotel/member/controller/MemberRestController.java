package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberRestController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(@RequestBody MemberRequestDto dto) {
        MemberResponseDto insert = toResponseDto(memberService.saveMember(toEntity(dto)));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "member insert success", insert)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {
        List<MemberResponseDto> members = memberService.getMembers().stream()
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", members)
        );
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", toResponseDto(memberService.getMember(memberId)))
        );
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @PathVariable Long memberId,
            @RequestBody MemberRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member update success", toResponseDto(memberService.updateMember(memberId, toEntity(dto))))
        );
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member delete success", null)
        );
    }

    private Member toEntity(MemberRequestDto dto) {
        return Member.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .status(dto.getStatus())
                .role(dto.getRole())
                .emailVerified(dto.getEmailVerified())
                .phoneVerified(dto.getPhoneVerified())
                .build();
    }

    private MemberResponseDto toResponseDto(Member member) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .status(member.getStatus())
                .role(member.getRole())
                .emailVerified(member.getEmailVerified())
                .phoneVerified(member.getPhoneVerified())
                .build();
    }
}
