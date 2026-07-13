package com.mjc.hotel.reservations.repository;

import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"member", "room", "room.hotelId", "room.roomTypeId"})
    @Query("""
            SELECT r FROM Reservation r
            JOIN r.member m
            JOIN r.room room
            JOIN room.hotelId hotel
            LEFT JOIN room.roomTypeId roomType
            WHERE (:status IS NULL OR r.reservationStatus = :status)
              AND (:memberId IS NULL OR m.sid = :memberId)
              AND (:hotelId IS NULL OR hotel.sid = :hotelId)
              AND (:roomTypeId IS NULL OR roomType.sid = :roomTypeId)
              AND (:keyword IS NULL
                   OR r.reservationNumber LIKE CONCAT('%', :keyword, '%')
                   OR m.name LIKE CONCAT('%', :keyword, '%')
                   OR r.guestName LIKE CONCAT('%', :keyword, '%'))
              AND (:roomKeyword IS NULL
                   OR room.roomName LIKE CONCAT('%', :roomKeyword, '%')
                   OR str(room.roomNumber) LIKE CONCAT('%', :roomKeyword, '%')
                   OR roomType.title LIKE CONCAT('%', :roomKeyword, '%'))
              AND (:dateFrom IS NULL OR r.checkInDate >= :dateFrom)
              AND (:dateTo IS NULL OR r.checkInDate < :dateTo)
            """)
    Page<Reservation> searchAdminReservations(
            @Param("status") ReservationStatus status,
            @Param("memberId") Long memberId,
            @Param("hotelId") Long hotelId,
            @Param("keyword") String keyword,
            @Param("roomKeyword") String roomKeyword,
            @Param("roomTypeId") Long roomTypeId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckInDateBetween(LocalDateTime start, LocalDateTime end);
    long countByCheckOutDateBetween(LocalDateTime start, LocalDateTime end);
    long countByReservationStatus(ReservationStatus status);

}
