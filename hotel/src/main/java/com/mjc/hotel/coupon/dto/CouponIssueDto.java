package com.mjc.hotel.coupon.dto;

import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CouponIssueDto {
    private Long sid;
    private Coupon coupon;
    private Member member;
    private Boolean isUsed;
    private LocalDateTime usedAt;
}
