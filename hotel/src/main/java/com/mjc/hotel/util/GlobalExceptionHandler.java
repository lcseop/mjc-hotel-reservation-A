package com.mjc.hotel.util;

import com.mjc.hotel.util.excep.AuthenticationFailedException;
import com.mjc.hotel.util.excep.DataNotFoundException;
import com.mjc.hotel.member.withdrawal.exception.SocialUnlinkException;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
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
