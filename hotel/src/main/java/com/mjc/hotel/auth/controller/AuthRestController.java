package com.mjc.hotel.auth.controller;

import com.mjc.hotel.auth.dto.LogoutRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenResponseDto;
import com.mjc.hotel.auth.oauth.OAuth2FrontendRedirectService;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입과 로그인을 관리합니다.")
public class AuthRestController {

    private final AuthService authService;
    private final MemberDtoMapper memberDtoMapper;
    private final OAuth2FrontendRedirectService oauth2FrontendRedirectService;

    @Operation(
            summary = "회원가입",
            description = "회원가입을 합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto>> signup(@RequestBody MemberSignupRequestDto request) {
        MemberResponseDto signup = memberDtoMapper.toResponseDto(authService.signup(request));
        return ResponseEntity.status(201).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "member signup success", signup)
        );
    }

    @Operation(
            summary = "회원 로그인",
            description = "회원 로그인을 합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberLoginResponseDto>> login(@RequestBody MemberLoginRequestDto request) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "login success", authService.login(request))
        );
    }

    @Operation(
            summary = "refresh 토큰",
            description = "refresh 토큰을 통해 access 토큰을 재발급합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> refresh(
            @RequestBody RefreshTokenRequestDto request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        ResponseCode.SUCCESS,
                        "access token refresh success",
                        authService.refreshAccessToken(request)
                )
        );
    }

    @Operation(
            summary = "회원 로그아웃",
            description = "저장된 refresh 토큰을 삭제해 재발급을 차단합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) LogoutRequestDto request) {
        authService.logout(request);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "logout success", null)
        );
    }

    @Operation(
            summary = "Google OAuth2 로그인 시작",
            description = "프론트 콜백 URL을 세션에 저장하고 Google 인증 화면으로 이동합니다."
    )
    @GetMapping("/oauth2/google/start")
    public ResponseEntity<Void> oauth2GoogleStart(
            @RequestParam String redirectUri,
            HttpServletRequest request,
            HttpSession session
    ) {
        if (!oauth2FrontendRedirectService.rememberRequestedCallback(redirectUri, request, session)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/google"))
                .build();
    }

}
