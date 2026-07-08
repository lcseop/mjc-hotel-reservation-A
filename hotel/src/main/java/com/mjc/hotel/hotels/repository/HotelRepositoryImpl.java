package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.QHotel;
import com.mjc.hotel.promotion.entity.QDiscountRate;
import com.mjc.hotel.promotion.entity.QPromotion;
import com.mjc.hotel.reservations.entity.QReservation;
import com.mjc.hotel.reservations.entity.QReservationCancel;
import com.mjc.hotel.room.entity.QRoom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class HotelRepositoryImpl implements HotelRepositorySub {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HotelResponseDto> search(HotelSearchRequestDto req, Pageable pageable) {

        QHotel h = QHotel.hotel;
        QRoom r = QRoom.room;
        QRoom promoRoom = new QRoom("promoRoom");
        QReservation res = QReservation.reservation;
        QReservationCancel rc = QReservationCancel.reservationCancel;
        QPromotion p = QPromotion.promotion;
        QDiscountRate dr = QDiscountRate.discountRate;
        LocalDateTime now = LocalDateTime.now();

        List<HotelResponseDto> content = queryFactory
                .select(Projections.constructor(
                        HotelResponseDto.class,
                        h.sid,
                        h.type.title,
                        h.hotelName,
                        h.hotelPrice,
                        h.location,
                        h.starRating,
                        h.description,
                        h.latitude,
                        h.longitude,
                        JPAExpressions
                                .select(dr.sale.max())
                                .from(dr)
                                .join(dr.promotion, p)
                                .where(
                                        p.roomType.in(
                                                JPAExpressions
                                                        .select(promoRoom.roomTypeId)
                                                        .from(promoRoom)
                                                        .where(
                                                                promoRoom.hotelId.eq(h),
                                                                notDeletedRoom(promoRoom)
                                                        )
                                        ),
                                        notDeletedPromotion(p),
                                        p.startDate.loe(now),
                                        p.endDate.goe(now)
                                )
                ))
                .from(h)
                .where(
                        notDeletedHotel(h),
                        keywordCond(h, req.getLocation()),
                        starCond(h, req.getStar()),
                        hotelTypeCond(h, req.getRoomTypeIds()),
                        existsAvailableRoom(h, r, res, rc, req)
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(h.count())
                .from(h)
                .where(
                        notDeletedHotel(h),
                        keywordCond(h, req.getLocation()),
                        starCond(h, req.getStar()),
                        hotelTypeCond(h, req.getRoomTypeIds()),
                        existsAvailableRoom(h, r, res, rc, req)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression notDeletedHotel(QHotel h) {
        return h.deleted.isFalse().or(h.deleted.isNull());
    }

    private BooleanExpression keywordCond(QHotel h, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        String trimmedKeyword = keyword.trim();

        return h.location.contains(trimmedKeyword)
                .or(h.hotelName.contains(trimmedKeyword));
    }

    private BooleanExpression starCond(QHotel h, Integer star) {
        return star != null ? h.starRating.eq(star) : null;
    }

    private BooleanExpression hotelTypeCond(QHotel h, List<Long> typeIds) {
        return (typeIds != null && !typeIds.isEmpty())
                ? h.type.sid.in(typeIds)
                : null;
    }

    private BooleanExpression existsAvailableRoom(
            QHotel h, QRoom r, QReservation res, QReservationCancel rc,
            HotelSearchRequestDto req
    ) {
        return JPAExpressions
                .selectOne()
                .from(r)
                .where(
                        r.hotelId.eq(h),
                        notDeletedRoom(r),

                        capacityCond(r, req.getTotalPeople()),
                        priceCond(r, req.getMinPrice(), req.getMaxPrice()),

                        noReservationConflict(r, res, rc,
                                req.getCheckIn(), req.getCheckOut())
                )
                .exists();
    }

    private BooleanExpression notDeletedRoom(QRoom r) {
        return r.deleted.isFalse().or(r.deleted.isNull());
    }

    private BooleanExpression notDeletedPromotion(QPromotion p) {
        return p.deleted.isFalse().or(p.deleted.isNull());
    }

    private BooleanExpression capacityCond(QRoom r, Integer people) {
        return people != null ? r.maximumPeople.goe(people) : null;
    }

    private BooleanExpression priceCond(QRoom r, Integer min, Integer max) {
        if (min != null && max != null) return r.roomPrice.between(min, max);
        if (min != null) return r.roomPrice.goe(min);
        if (max != null) return r.roomPrice.loe(max);
        return null;
    }

    private BooleanExpression noReservationConflict(
            QRoom r, QReservation res, QReservationCancel rc,
            LocalDateTime checkIn, LocalDateTime checkOut
    ) {
        if (checkIn == null || checkOut == null) return null;

        return JPAExpressions
                .selectOne()
                .from(res)
                .where(
                        res.room.eq(r),

                        res.sid.notIn(
                                JPAExpressions
                                        .select(rc.reservation.sid)
                                        .from(rc)
                        ),

                        // 날짜 겹침 조건
                        res.checkInDate.lt(checkOut),
                        res.checkOutDate.gt(checkIn)
                )
                .notExists();
    }
}
