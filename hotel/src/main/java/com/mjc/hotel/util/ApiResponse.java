package com.mjc.hotel.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private ResponseCode code;
    private String message;
    private T data;

}

