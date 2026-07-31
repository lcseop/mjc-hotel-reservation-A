package com.mjc.hotel.util;

import java.util.Random;

public class CommonUtils {

    private CommonUtils() {}

    private static final Random rnd = new Random();

    public static String getRandomString(int length) {
        String arrs = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int index = rnd.nextInt(arrs.length());
            sb.append(arrs.charAt(index));
        }
        return sb.toString();
    }
}
