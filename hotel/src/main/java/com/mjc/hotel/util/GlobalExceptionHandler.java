package com.mjc.hotel.util;

import com.mjc.hotel.util.excep.AuthenticationFailedException;
import com.mjc.hotel.auth.exception.DuplicateEmailException;
import com.mjc.hotel.util.excep.DataNotFoundException;
import com.mjc.hotel.member.withdrawal.exception.SocialUnlinkException;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<String>> duplicateEmailHandler(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResponse<>(ResponseCode.INSERT_ERROR, "duplicate email", ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> illegalArgumentHandler(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(ResponseCode.INSERT_ERROR, "invalid request", ex.getMessage())
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> accessDeniedHandler(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiResponse<>(ResponseCode.AUTHENTICATION_ERROR, "access denied", "접근 권한이 없습니다.")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiResponse<>(ResponseCode.SERVER_ERROR, "server error", ex.getMessage())
        );
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> dataNotFoundHandler(DataNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found", ex.getMessage())
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResponse<String>> authenticationFailedHandler(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResponse<>(ResponseCode.AUTHENTICATION_ERROR, "authentication failed", ex.getMessage())
        );
    }

    @ExceptionHandler(WithdrawalConflictException.class)
    public ResponseEntity<ApiResponse<String>> withdrawalConflictHandler(WithdrawalConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResponse<>(ResponseCode.DELETE_ERROR, "withdrawal conflict", ex.getMessage())
        );
    }

    @ExceptionHandler(SocialUnlinkException.class)
    public ResponseEntity<ApiResponse<String>> socialUnlinkHandler(SocialUnlinkException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ApiResponse<>(ResponseCode.DELETE_ERROR, "social unlink failed", ex.getMessage())
        );
    }
}
