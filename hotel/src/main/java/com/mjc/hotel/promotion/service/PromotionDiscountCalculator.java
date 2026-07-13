package com.mjc.hotel.promotion.service;

import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PromotionDiscountCalculator {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d[\\d,]*");

    private PromotionDiscountCalculator() {
    }

    public static Promotion findBestPromotion(List<Promotion> promotions, Integer roomPrice) {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }

        return promotions.stream()
                .filter(PromotionDiscountCalculator::isActiveNow)
                .max(Comparator.comparingInt(promotion -> calculateDiscountAmount(roomPrice, promotion.getDiscountContent())))
                .orElse(null);
    }

    public static boolean isActiveNow(Promotion promotion) {
        if (promotion == null || Boolean.TRUE.equals(promotion.getDeleted())) {
            return false;
        }
        if (promotion.getConditionType() != ConditionType.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return (promotion.getStartDate() == null || !now.isBefore(promotion.getStartDate()))
                && (promotion.getEndDate() == null || !now.isAfter(promotion.getEndDate()));
    }

    public static Integer calculateDiscountAmount(Integer roomPrice, String discountContent) {
        int price = roomPrice != null ? roomPrice : 0;
        int value = extractDiscountValue(discountContent);

        if (price <= 0 || value <= 0) {
            return 0;
        }

        if (isFlatDiscount(discountContent)) {
            return Math.min(price, value);
        }

        return Math.min(price, Math.round(price * (Math.min(value, 100) / 100.0f)));
    }

    public static Integer calculateDiscountedPrice(Integer roomPrice, String discountContent) {
        int price = roomPrice != null ? roomPrice : 0;
        return Math.max(0, price - calculateDiscountAmount(price, discountContent));
    }

    public static Integer extractDiscountRate(String discountContent) {
        if (isFlatDiscount(discountContent)) {
            return 0;
        }
        return Math.min(extractDiscountValue(discountContent), 100);
    }

    public static Integer extractDiscountValue(String discountContent) {
        if (discountContent == null || discountContent.isBlank()) {
            return 0;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(discountContent);
        if (!matcher.find()) {
            return 0;
        }

        return Integer.parseInt(matcher.group().replace(",", ""));
    }

    public static boolean isFlatDiscount(String discountContent) {
        return discountContent != null && discountContent.contains("원");
    }
}
