package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
    "LEFT JOIN FETCH r.member " +
    "LEFT JOIN FETCH r.room room " +
    "LEFT JOIN FETCH room.hotelId " +
    "LEFT JOIN FETCH room.roomTypeId " +
    "LEFT JOIN FETCH r.couponIssue " +
    "WHERE r.sid = :sid")
    Optional<Reservation> findByIdWithDetails(@Param("sid") Long sid);

    @Query("SELECT r FROM Reservation r " +
    "LEFT JOIN FETCH r.member " +
    "LEFT JOIN FETCH r.room room " +
    "LEFT JOIN FETCH room.hotelId " +
    "LEFT JOIN FETCH room.roomTypeId")
    List<Reservation> findAllWithDetails();

    List<Reservation> findByReservationStatus(ReservationStatus status);

    @EntityGraph(attributePaths = {"member", "room", "room.hotelId", "room.roomTypeId"})
    Page<Reservation> findByReservationStatus(ReservationStatus status, Pageable pAgeable);

    List<Reservation> findByMember_Sid(Long memberId);

    @EntityGraph(attributePaths = {"member", "room", "room.hotelId", "room.roomTypeId"})
    Page<Reservation> findByMember_Sid(Long memberId, Pageable pageable);

    List<Reservation> findByMember_SidAndReservationStatus(Long memberId, ReservationStatus status);

    @EntityGraph(attributePaths = {"member", "room", "room.hotelId", "room.roomTypeId"})
    Page<Reservation> findByMember_SidAndReservationStatus(Long memberId, ReservationStatus status, Pageable pageable);

    Optional<Reservation> findByCheckInQr(String checkInQr);

    Optional<Reservation> findByReservationNumber(String reservationNumber);

    @EntityGraph(attributePaths = {"member", "room", "room.hotelId", "room.roomTypeId"})
    Page<Reservation> findAll(Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckInDateBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckOutDateBetween(LocalDateTime start, LocalDateTime end);
    long countByReservationStatus(ReservationStatus status);

}
