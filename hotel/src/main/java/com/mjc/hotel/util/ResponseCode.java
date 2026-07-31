package com.mjc.hotel.util;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(10000),
    INSERT_ERROR(41000),
    UPDATE_ERROR(42000),
    DELETE_ERROR(43000),
    SELECT_ERROR(44000),
    SERVER_ERROR(49000),
    DATA_NOT_FOUND_ERROR(51000),
    AUTHENTICATION_ERROR(52000),
    AUTHORIZATION_ERROR(53000);

    private Integer code;

    ResponseCode(Integer code) {
        this.code = code;
    }
}
