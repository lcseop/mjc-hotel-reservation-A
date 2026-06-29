package com.mjc.hotel.hotels.repository;

import com.mjc.hotel.hotels.dto.HotelSearchRequestDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.QHotel;
import com.mjc.hotel.reservations.entity.QReservation;
import com.mjc.hotel.reservations.entity.QReservationCancel;
import com.mjc.hotel.room.entity.QRoom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class HotelRepositoryImpl implements HotelRepositorySub {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Hotel> search(HotelSearchRequestDto req) {
        QHotel h = QHotel.hotel;
        QRoom r = QRoom.room;
        QReservation res = QReservation.reservation;
        QReservationCancel rc = QReservationCancel.reservationCancel;

        return queryFactory
                .selectFrom(h)
                .where(
                        locationCond(h, req.getLocation()),
                        starCond(h, req.getStar()),
                        existsAvailableRoom(h, r, res, rc, req)
                )
                .distinct()
                .fetch();
    }

    private BooleanExpression locationCond(QHotel h, String location) {
        return location != null ? h.location.contains(location) : null;
    }

    private BooleanExpression starCond(QHotel h, Integer star) {
        return star != null ? h.starRating.eq(star) : null;
    }

    private BooleanExpression existsAvailableRoom(
            QHotel h, QRoom r, QReservation res, QReservationCancel rc, HotelSearchRequestDto req
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
            QRoom r, QReservation res, QReservationCancel rc
            , LocalDateTime checkIn, LocalDateTime checkOut
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
