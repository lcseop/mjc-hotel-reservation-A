package com.mjc.hotel.member.withdrawal.exception;

public class SocialUnlinkException extends RuntimeException {

    public SocialUnlinkException(String message) {
        super(message);
    }

    public SocialUnlinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
