package com.mjc.hotel.util;

import java.util.Random;

public class CommonUtils {
    public static String getRandomString(int length) {
        String arrs = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int index = rnd.nextInt(arrs.length());
            sb.append(arrs.charAt(index));
        }
        return sb.toString();
    }
}
