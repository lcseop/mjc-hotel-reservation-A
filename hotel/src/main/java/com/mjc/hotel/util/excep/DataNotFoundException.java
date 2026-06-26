package com.mjc.hotel.util.excep;

import com.mjc.hotel.util.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataNotFoundException extends RuntimeException {
    private ResponseCode code;
    private String message;
}
