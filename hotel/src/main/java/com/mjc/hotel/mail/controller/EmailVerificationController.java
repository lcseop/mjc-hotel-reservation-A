package com.mjc.hotel.mail.controller;

import com.mjc.hotel.mail.dto.EmailVerificationConfirmRequest;
import com.mjc.hotel.mail.dto.EmailVerificationResponse;
import com.mjc.hotel.mail.dto.EmailVerificationSendRequest;
import com.mjc.hotel.mail.service.EmailVerificationService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail/verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> sendCode(
            @RequestBody EmailVerificationSendRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "email verification code sent", emailVerificationService.sendCode(request.getEmail()))
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> confirmCode(
            @RequestBody EmailVerificationConfirmRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "email verification confirmed", emailVerificationService.confirmCode(request.getEmail(), request.getCode()))
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(ResponseCode.UPDATE_ERROR, "email verification failed", ex.getMessage())
        );
    }
}
