package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
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

    @Autowired
    private MemberDtoMapper memberDtoMapper;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(@RequestBody MemberRequestDto dto) {
        MemberResponseDto insert = memberDtoMapper.toResponseDto(memberService.saveMember(memberDtoMapper.toEntity(dto)));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "member insert success", insert)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {
        List<MemberResponseDto> members = memberService.getMembers().stream()
                .map(memberDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", members)
        );
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", memberDtoMapper.toResponseDto(memberService.getMember(memberId)))
        );
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @PathVariable Long memberId,
            @RequestBody MemberRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member update success", memberDtoMapper.toResponseDto(memberService.updateMember(memberId, memberDtoMapper.toEntity(dto))))
        );
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member delete success", null)
        );
    }
}
