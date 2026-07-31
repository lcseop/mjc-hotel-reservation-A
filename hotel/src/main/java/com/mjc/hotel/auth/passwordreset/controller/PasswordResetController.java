package com.mjc.hotel.auth.passwordreset.controller;

import com.mjc.hotel.auth.passwordreset.dto.PasswordResetConfirmRequest;
import com.mjc.hotel.auth.passwordreset.dto.PasswordResetRequest;
import com.mjc.hotel.auth.passwordreset.dto.PasswordResetVerifyRequest;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetAccountNotFoundException;
import com.mjc.hotel.auth.passwordreset.exception.PasswordResetException;
import com.mjc.hotel.auth.passwordreset.service.PasswordResetService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member/password-reset")
@RequiredArgsConstructor
@Tag(name = "비밀번호 찾기", description = "비밀번호를 재설정 합니다.")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "인증번호 발송",
            description = "인증번호를 발송합니다."
    )
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(
            @RequestBody PasswordResetRequest request
    ) {
        passwordResetService.requestVerificationCode(request == null ? null : request.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "password reset verification code sent",
                null
        ));
    }

    @Operation(
            summary = "인증번호 일치 여부",
            description = "인증번호 일치를 확인합니다."
    )
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Void>> verifyCode(
            @RequestBody PasswordResetVerifyRequest request
    ) {
        passwordResetService.verifyCode(
                request == null ? null : request.getEmail(),
                request == null ? null : request.getCode()
        );
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "password reset verification success",
                null
        ));
    }

    @Operation(
            summary = "비밀번호 변경",
            description = "새 비밀번호로 변경합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(
                ResponseCode.SUCCESS,
                "password reset success",
                null
        ));
    }

    @ExceptionHandler(PasswordResetAccountNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleAccountNotFound(
            PasswordResetAccountNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                ResponseCode.DATA_NOT_FOUND_ERROR,
                "password reset account not found",
                exception.getMessage()
        ));
    }

    @ExceptionHandler(PasswordResetException.class)
    public ResponseEntity<ApiResponse<String>> handlePasswordResetException(
            PasswordResetException exception
    ) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(
                ResponseCode.UPDATE_ERROR,
                "password reset failed",
                exception.getMessage()
        ));
    }
}
