package com.mjc.hotel.coupon.service;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.dto.CouponIssueDto;
import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponIssueRepository couponIssueRepository;

    public CouponDto insert(CouponDto couponDto) {
        CouponIssue issue = couponIssueRepository.findById(couponDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발행 정보입니다."));

        Coupon coupon = Coupon.builder()
                .couponName(couponDto.getCouponName())
                .discountType(couponDto.getDiscountType())
                .discountValue(couponDto.getDiscountValue())
                .minOrderAmount(couponDto.getMinOrderAmount())
                .startDate(couponDto.getStartDate())
                .endDate(couponDto.getEndDate())
                .totalQuantity(couponDto.getTotalQuantity())
                .build();

        Coupon saved = couponRepository.save(coupon);

        return CouponDto
                .builder()
                .id(saved.getId())
                .couponName(saved.getCouponName())
                .discountType(saved.getDiscountType())
                .discountValue(saved.getDiscountValue())
                .minOrderAmount(saved.getMinOrderAmount())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .totalQuantity(saved.getTotalQuantity())
                .build();
    }
}
