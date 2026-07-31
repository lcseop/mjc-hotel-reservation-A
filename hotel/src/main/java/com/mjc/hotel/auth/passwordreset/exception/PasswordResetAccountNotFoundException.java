package com.mjc.hotel.auth.passwordreset.exception;

public class PasswordResetAccountNotFoundException extends PasswordResetException {

    public PasswordResetAccountNotFoundException() {
        super("가입된 LOCAL 계정을 찾을 수 없습니다.");
    }
}
