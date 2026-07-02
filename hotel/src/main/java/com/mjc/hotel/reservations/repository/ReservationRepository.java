package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
    "LEFT JOIN FETCH r.member " +
    "LEFT JOIN FETCH r.room " +
    "LEFT JOIN FETCH r.couponIssue " +
    "WHERE r.sid = :sid")
    Optional<Reservation> findByIdWithDetails(@Param("sid") Long sid);

    @Query("SELECT r FROM Reservation r " +
    "LEFT JOIN FETCH r.member " +
    "LEFT JOIN FETCH r.room")
    List<Reservation> findAllWithDetails();
}
