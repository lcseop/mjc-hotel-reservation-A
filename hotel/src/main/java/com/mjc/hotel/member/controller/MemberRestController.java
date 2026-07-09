package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberRequestDto;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 데이터 전반을 관리합니다.")
public class MemberRestController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberDtoMapper memberDtoMapper;

    @Operation(
            summary = "회원 데이터 생성",
            description = "회원 데이터를 만듭니다."
    )

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<MemberResponseDto>> insert(@RequestBody MemberRequestDto dto) {
        MemberResponseDto insert = memberDtoMapper.toResponseDto(memberService.saveMember(memberDtoMapper.toEntity(dto)));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "member insert success", insert)
        );
    }

    @Operation(
            summary = "회원 데이터 검색",
            description = "회원 데이터를 검색합니다."
    )

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {
        List<MemberResponseDto> members = memberService.getMembers().stream()
                .map(memberDtoMapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", members)
        );
    }

    @Operation(
            summary = "회원 데이터 상세 검색",
            description = "회원 데이터 하나를 검색합니다."
    )

    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long sid) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member select success", memberDtoMapper.toResponseDto(memberService.getMember(sid)))
        );
    }

    @Operation(
            summary = "회원 데이터 수정",
            description = "회원 데이터를 수정합니다."
    )

    @PatchMapping
    public ResponseEntity<ApiResponse<MemberResponseDto>> update(
            @RequestBody MemberRequestDto dto
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member update success", memberDtoMapper.toResponseDto(memberService.updateMember(dto.getSid(), memberDtoMapper.toEntity(dto))))
        );
    }

    @Operation(
            summary = "회원 데이터 삭제",
            description = "회원 데이터를 삭제합니다."
    )

    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        memberService.deleteMember(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "member delete success", null)
        );
    }
}
