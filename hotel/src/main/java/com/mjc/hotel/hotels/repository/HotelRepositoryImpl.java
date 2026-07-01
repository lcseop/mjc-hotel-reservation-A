package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelResponseDto;
import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.QHotel;
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
        QReservation res = QReservation.reservation;
        QReservationCancel rc = QReservationCancel.reservationCancel;

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
                        h.longitude
                ))
                .from(h)
                .where(
                        locationCond(h, req.getLocation()),
                        starCond(h, req.getStar()),
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
                        locationCond(h, req.getLocation()),
                        starCond(h, req.getStar()),
                        existsAvailableRoom(h, r, res, rc, req)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression locationCond(QHotel h, String location) {
        return location != null ? h.location.contains(location) : null;
    }

    private BooleanExpression starCond(QHotel h, Integer star) {
        return star != null ? h.starRating.eq(star) : null;
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
                        capacityCond(r, req.getPeople()),
                        priceCond(r, req.getMinPrice(), req.getMaxPrice()),
                        roomTypeCond(r, req.getRoomTypeIds()),
                        noReservationConflict(r, res, rc,
                                req.getCheckIn(), req.getCheckOut())
                )
                .exists();
    }

    private BooleanExpression capacityCond(QRoom r, Integer people) {
        return people != null ? r.maximumPeople.goe(people) : null;
    }

    private BooleanExpression priceCond(QRoom r, Integer min, Integer max) {
        if (min == null || max == null) return null;
        return r.roomPrice.between(min, max);
    }

    private BooleanExpression roomTypeCond(QRoom r, List<Long> roomTypeIds) {
        return (roomTypeIds != null && !roomTypeIds.isEmpty())
                ? r.roomTypeId.sid.in(roomTypeIds)
                : null;
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
                        res.checkInDate.lt(checkOut),
                        res.checkOutDate.gt(checkIn)
                )
                .notExists();
    }
}
