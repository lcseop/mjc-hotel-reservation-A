package com.mjc.hotel.coupon.service;

import com.mjc.hotel.coupon.dto.CouponDto;
import com.mjc.hotel.coupon.dto.CouponIssueRequestDto;
import com.mjc.hotel.coupon.dto.CouponIssueResponseDto;
import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.coupon.repository.CouponRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CouponIssueRepository couponIssueRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Transactional
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

    public List<CouponDto> findAll() {
        return couponRepository.findAll()
                .stream()
                .map(this::toCouponDto)
                .toList();
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

    public List<CouponIssueResponseDto> findUsableByMember(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        return couponIssueRepository.findByMemberSid(memberId)
                .stream()
                .filter(issue -> !Boolean.TRUE.equals(issue.getIsUsed()))
                .filter(issue -> {
                    Coupon coupon = issue.getCoupon();
                    return coupon != null
                            && (coupon.getStartDate() == null || !coupon.getStartDate().isAfter(now))
                            && (coupon.getEndDate() == null || !coupon.getEndDate().isBefore(now));
                })
                .sorted(Comparator
                        .comparing((CouponIssue issue) -> issue.getCoupon().getEndDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CouponIssue::getSid, Comparator.reverseOrder()))
                .map(this::toIssueResponse)
                .toList();
    }

    @Transactional
    public CouponIssueResponseDto issue(CouponIssueRequestDto requestDto) {
        Coupon coupon = couponRepository.findById(requestDto.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        CouponIssue issue = CouponIssue.builder()
                .coupon(coupon)
                .member(member)
                .isUsed(false)
                .build();

        return toIssueResponse(couponIssueRepository.save(issue));
    }

    private CouponIssueResponseDto toIssueResponse(CouponIssue issue) {
        Coupon coupon = issue.getCoupon();
        return CouponIssueResponseDto.builder()
                .sid(issue.getSid())
                .couponId(coupon.getSid())
                .couponName(coupon.getCouponName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .isUsed(issue.getIsUsed())
                .usedAt(issue.getUsedAt())
                .build();
    }

    private CouponDto toCouponDto(Coupon coupon) {
        return CouponDto.builder()
                .sid(coupon.getSid())
                .couponName(coupon.getCouponName())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .totalQuantity(coupon.getTotalQuantity())
                .build();
    }
}
