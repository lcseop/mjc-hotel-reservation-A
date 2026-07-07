package com.mjc.hotel.coupon.service;

import com.mjc.hotel.coupon.dto.CouponDto;
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
                .sid(saved.getSid())
                .couponName(saved.getCouponName())
                .discountType(saved.getDiscountType())
                .discountValue(saved.getDiscountValue())
                .minOrderAmount(saved.getMinOrderAmount())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .totalQuantity(saved.getTotalQuantity())
                .build();
    }

    @Transactional
    public void delete(Long id) {
        couponIssueRepository.deleteByCouponSid(id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        couponRepository.delete(coupon);

    }

    @Transactional
    public void update(Long id, CouponDto couponDto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다." + id));

        coupon.update(
                couponDto.getCouponName(),
                couponDto.getDiscountType(),
                couponDto.getDiscountValue(),
                couponDto.getMinOrderAmount(),
                couponDto.getStartDate(),
                couponDto.getEndDate(),
                couponDto.getTotalQuantity()
        );
    }
}
