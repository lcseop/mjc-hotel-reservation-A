package com.mjc.hotel.coupon;

import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.coupon.entity.CouponDiscountType;
import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.coupon.repository.CouponRepository;
import com.mjc.hotel.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class CouponServiceTest {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Test
    @Commit
    public void couponTest() {
        Coupon coupon = Coupon
                .builder()
                .couponName("couponName")
                .discountType(CouponDiscountType.PERCENT)
                .discountValue(30.00)
                .minOrderAmount(3000)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .totalQuantity(1)
                .build();
        couponRepository.save(coupon);

        Member member = Member
                .builder()
                .sid(1L)
                .build();

        CouponIssue couponIssue = CouponIssue
                .builder()
                .coupon(coupon)
                .member(member)
                .isUsed(false)
                .build();
        couponIssueRepository.save(couponIssue);
    }
}
