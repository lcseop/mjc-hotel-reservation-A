package com.mjc.hotel.coupon;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.entity.CouponDiscountType;
import com.mjc.hotel.coupon.service.CouponService;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private MemberRepository memberRepository;

    private Long testMemberId;

    @BeforeEach
    void setUp() {
        // 1. Member 생성 (필수값 전부 포함)
        Member member = Member.builder()
                .name("테스트 회원")
                .email("test@example.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(0)
                .build();
        testMemberId = memberRepository.save(member).getSid();
    }

    @Test
    @DisplayName("쿠폰 생성 및 삭제 테스트")
    void couponTest() {
        // 2. 쿠폰 생성 (CouponDto 구성)
        CouponDto dto = CouponDto.builder()
                .couponName("여름 할인 쿠폰")
                .discountType(CouponDiscountType.PERCENT)
                .discountValue(10.0)
                .minOrderAmount(1000)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .totalQuantity(100)
                .build();

        // 3. 서비스 호출
        CouponDto saved = couponService.insert(dto);

        // 4. 검증
        assertThat(saved.getSid()).isNotNull();
        assertThat(saved.getCouponName()).isEqualTo("여름 할인 쿠폰");
    }
}