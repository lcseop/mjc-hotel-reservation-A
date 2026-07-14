package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.coupon.entity.QCoupon;
import com.mjc.hotel.coupon.entity.QCouponIssue;
import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.dto.PromotionSearchRequestDto;
import com.mjc.hotel.promotion.dto.PromotionStatsDto;
import com.mjc.hotel.promotion.entity.*;
import com.mjc.hotel.reservations.entity.QReservation;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.mjc.hotel.promotion.entity.QPromotion;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PromotionDto> search(PromotionSearchRequestDto req, Pageable pageable) {
        QPromotion p = QPromotion.promotion;

        // 1. content 쿼리 수정 (조인 제거)
        List<PromotionDto> content = queryFactory
                .select(Projections.constructor(PromotionDto.class,
                        p.sid,
                        p.roomType.sid,
                        p.promotionName,
                        p.discountContent,
                        p.startDate,
                        p.endDate,
                        p.conditionType.stringValue()
                ))
                .from(p)
                .where(
                        notDeletedPromotion(p),
                        validPromotion(p),
                        nameCond(p, req.getPromotionName()),
                        typeCond(p, req.getConditionType()),
                        dateCond(p, req.getStartDate(), req.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(p.createdAt.desc())
                .fetch();

        // 2. total 쿼리 수정 (조인 제거)
        Long total = queryFactory
                .select(p.count())
                .from(p)
                .where(
                        notDeletedPromotion(p),
                        validPromotion(p),
                        nameCond(p, req.getPromotionName()),
                        typeCond(p, req.getConditionType()),
                        dateCond(p, req.getStartDate(), req.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression nameCond(QPromotion p, String name) {
        return (name != null && !name.isEmpty()) ? p.promotionName.contains(name) : null;
    }

    private BooleanExpression notDeletedPromotion(QPromotion p) {
        return p.deleted.isFalse().or(p.deleted.isNull());
    }

    private BooleanExpression validPromotion(QPromotion p) {
        return p.roomType.isNotNull()
                .and(p.discountContent.isNotNull())
                .and(p.discountContent.isNotEmpty())
                .and(p.conditionType.isNotNull());
    }

    private BooleanExpression typeCond(QPromotion p, ConditionType type) {
        if (type == null) {
            return null;
        }
        return p.conditionType.eq(type);
    }

    private BooleanExpression dateCond(QPromotion p, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return p.startDate.loe(end).and(p.endDate.goe(start));
    }

    @Override
    public List<PromotionStatsDto> getPromotionStatistics() {
        QReservation reservation = QReservation.reservation;
        QPromotion promo = QPromotion.promotion;
        QCouponIssue couponIssue = QCouponIssue.couponIssue;
        QCoupon coupon = QCoupon.coupon;

        return queryFactory
                .select(Projections.fields(PromotionStatsDto.class,
                        promo.sid.as("promotionSid"),
                        reservation.count().as("reservationCount"),
                        reservation.totalAmount.sum().coalesce(0).as("totalDiscountAmount")
                ))
                .from(promo)
                // 1. Coupon 테이블의 promotion_sid 컬럼과 promo.sid를 직접 비교
                .leftJoin(coupon).on(coupon.sid.eq(Expressions.numberPath(Long.class, "promotion_sid")))

                // 2. CouponIssue와 Coupon 조인
                .leftJoin(couponIssue).on(couponIssue.coupon.sid.eq(coupon.sid))

                // 3. Reservation과 CouponIssue 조인
                .leftJoin(reservation).on(reservation.couponIssue.sid.eq(couponIssue.sid))

                .groupBy(promo.sid)
                .fetch();
    }
}
