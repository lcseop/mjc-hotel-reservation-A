package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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

    List<Reservation> findByReservationStatus(ReservationStatus status);

    Page<Reservation> findByReservationStatus(ReservationStatus status, Pageable pAgeable);

    List<Reservation> findByMember_Sid(Long memberId);

    Page<Reservation> findByMember_Sid(Long memberId, Pageable pageable);

    List<Reservation> findByMember_SidAndReservationStatus(Long memberId, ReservationStatus status);

    Page<Reservation> findByMember_SidAndReservationStatus(Long memberId, ReservationStatus status, Pageable pageable);

    Page<Reservation> findAll(Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckInDateBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckOutDateBetween(LocalDateTime start, LocalDateTime end);
    long countByReservationStatus(ReservationStatus status);

}
