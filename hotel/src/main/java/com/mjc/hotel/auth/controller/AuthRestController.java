package com.mjc.hotel.auth.controller;

import com.mjc.hotel.auth.dto.LogoutRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginRequestDto;
import com.mjc.hotel.auth.dto.MemberLoginResponseDto;
import com.mjc.hotel.auth.dto.MemberSignupRequestDto;
import com.mjc.hotel.auth.dto.OAuth2MemberResponseDto;
import com.mjc.hotel.auth.dto.RefreshTokenRequestDto;
import com.mjc.hotel.auth.dto.RefreshTokenResponseDto;
import com.mjc.hotel.auth.oauth.GoogleOidcUser;
import com.mjc.hotel.auth.service.AuthService;
import com.mjc.hotel.member.converter.MemberDtoMapper;
import com.mjc.hotel.member.dto.MemberResponseDto;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입과 로그인을 관리합니다.")
public class AuthRestController {

    private final AuthService authService;
    private final MemberDtoMapper memberDtoMapper;

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
            summary = "OAuth2 로그인 성공",
            description = "OAuth2 로그인으로 확인한 회원 정보를 반환합니다."
    )
    @GetMapping("/oauth2/success")
    public ResponseEntity<ApiResponse<OAuth2MemberResponseDto>> oauth2Success(
            @AuthenticationPrincipal GoogleOidcUser principal
    ) {
        if (principal == null) {
            throw new AuthenticationFailedException("OAuth2 로그인 정보가 없습니다.");
        }

        OAuth2MemberResponseDto response = OAuth2MemberResponseDto.builder()
                .memberSid(principal.getMemberSid())
                .provider(MemberAuthProvider.GOOGLE)
                .email(principal.getEmail())
                .name(principal.getDisplayName())
                .role(principal.getRole())
                .build();

        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "google oauth2 login success", response)
        );
    }

    @Operation(
            summary = "OAuth2 로그인 실패",
            description = "OAuth2 로그인 실패 사유를 반환합니다."
    )
    @GetMapping("/oauth2/failure")
    public ResponseEntity<ApiResponse<String>> oauth2Failure(HttpSession session) {
        Object authenticationException = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        String message = authenticationException instanceof Exception exception
                ? exception.getMessage()
                : "OAuth2 로그인에 실패했습니다.";
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResponse<>(ResponseCode.AUTHENTICATION_ERROR, "oauth2 login failed", message)
        );
    }
}
