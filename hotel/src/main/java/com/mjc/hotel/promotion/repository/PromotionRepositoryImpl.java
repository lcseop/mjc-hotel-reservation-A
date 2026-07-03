package com.mjc.hotel.promotion.repository;

import com.mjc.hotel.promotion.dto.PromotionDto;
import com.mjc.hotel.promotion.dto.PromotionSearchRequestDto;
import com.mjc.hotel.promotion.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PromotionDto> search(PromotionSearchRequestDto req, Pageable pageable) {
        QPromotion p = QPromotion.promotion;
        QCondition c = QCondition.condition;

        List<PromotionDto> content = queryFactory
                .select(Projections.constructor(
                        PromotionDto.class,
                        p.sid,
                        p.roomType.sid,
                        p.promotionName,
                        p.startDate,
                        p.endDate,
                        Expressions.asString("Active")
                ))
                .from(p)
                .leftJoin(c).on(c.promotion.eq(p))
                .where(
                        nameCond(p, req.getPromotionName()),
                        typeCond(c, req.getConditionType()),
                        dateCond(p, req.getStartDate(), req.getEndDate())
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(p.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(p.count())
                .from(p)
                .leftJoin(c).on(c.promotion.eq(p))
                .where(
                        nameCond(p, req.getPromotionName()),
                        typeCond(c, req.getConditionType()),
                        dateCond(p, req.getStartDate(), req.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression nameCond(QPromotion p, String name) {
        return (name != null && !name.isEmpty()) ? p.promotionName.contains(name) : null;
    }

    private BooleanExpression typeCond(QCondition c, ConditionType type) {
        return type != null ? c.conditiontype.eq(type) : null;
    }

    private BooleanExpression dateCond(QPromotion p, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return p.startDate.loe(end).and(p.endDate.goe(start));
    }
}