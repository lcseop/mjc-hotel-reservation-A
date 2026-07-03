package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.dto.MemberLoginRequestDto;
import com.mjc.hotel.member.dto.MemberLoginResponseDto;
import com.mjc.hotel.member.service.AuthService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberLoginResponseDto>> login(@RequestBody MemberLoginRequestDto request) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "login success", authService.login(request))
        );
    }
}
