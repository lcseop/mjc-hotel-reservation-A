package com.mjc.hotel.reservation;

import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.coupon.repository.CouponRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.entity.*;
import com.mjc.hotel.reservations.repository.EmailLogRepository;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationCancelRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.reservations.service.ReservationService;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.awt.*;
import java.time.LocalDateTime;


@SpringBootTest
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    private ReservationCancelRepository reservationCancelRepository;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @DisplayName("reservationTestDate")
    @Test
    @Commit
    @Transactional

    public void reservationTest() {

        Member member = Member.builder().sid(1L).build();
        Room room = Room.builder().sid(1L).build();
        CouponIssue couponIssue = CouponIssue.builder().sid(1L).build();

        Reservation reservation = Reservation
                .builder()
                .member(member)
                .room(room)
                .couponIssue(couponIssue)
                .reservationNumber("reservationNumber")
                .checkInDate(LocalDateTime.now())
                .checkOutDate(LocalDateTime.now())
                .adults(1)
                .children(1)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(180000)
                .specialRequests("TEXT")
                .checkInQr("TEXT")
                .totalNights(2)
                .guestName("guest")
                .build();

        reservationRepository.save(reservation);


        ReservationCancel reservationCancel = ReservationCancel
                .builder()
                .reservation(reservation)
                .cancelReason("cancelreason")
                .refundAmount(180000)
                .cancelledAt(LocalDateTime.now())
                .build();

        reservationCancelRepository.save(reservationCancel);


        EmailLog emailLog = EmailLog
                .builder()
                .reservation(reservation)
                .recipientEmail("recipientEmail")
                .emailStatus(EmailStatus.SEND)
                .sentAt(LocalDateTime.now())
                .build();

        emailLogRepository.save(emailLog);


        PointHistory pointHistory = PointHistory
                .builder()
                .reservation(reservation)
                .member(member)
                .amount(5000)
                .pointStatus(PointStatus.USE)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepository.save(pointHistory);
    }

}



