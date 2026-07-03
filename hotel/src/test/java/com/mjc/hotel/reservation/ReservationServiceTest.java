package com.mjc.hotel.reservation;

import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.coupon.repository.CouponRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.dto.ReservationCancelDto;
import com.mjc.hotel.reservations.dto.ReservationRequestDto;
import com.mjc.hotel.reservations.dto.ReservationResponseDto;
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

        Reservation reservation = Reservation.builder()
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


        ReservationCancel reservationCancel = ReservationCancel.builder()
                .reservation(reservation)
                .cancelReason("cancelreason")
                .refundAmount(180000)
                .cancelledAt(LocalDateTime.now())
                .build();

        reservationCancelRepository.save(reservationCancel);


        EmailLog emailLog = EmailLog.builder()
                .reservation(reservation)
                .recipientEmail("recipientEmail")
                .emailStatus(EmailStatus.SEND)
                .sentAt(LocalDateTime.now())
                .build();

        emailLogRepository.save(emailLog);


        PointHistory pointHistory = PointHistory.builder()
                .reservation(reservation)
                .member(member)
                .amount(5000)
                .pointStatus(PointStatus.USE)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepository.save(pointHistory);
    }

    @DisplayName("예약 생성 테스트")
    @Test
    @Commit

    public void createReservationTest() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(7))
                .checkOutDate(LocalDateTime.now().plusDays(9))
                .adults(2)
                .children(1)
                .specialRequests("조용한 방으로 부탁드립니다")
                .couponIssueId(null)
                .usePoint(0)
                .guestName("홍길동")
                .build();

        ReservationResponseDto response = reservationService.createReservation(requestDto);

        System.out.println("=== 예약 생성 완료 ===");
        System.out.println("예약번호 : " + response.getReservationNumber());
        System.out.println("예약자: " + response.getMemberName());
        System.out.println("총 금액 : " + response.getTotalAmount());
        System.out.println("상태 : " + response.getReservationStatus());
    }

    @DisplayName("포인트 사용 예약 테스트")
    @Test
    @Commit

    public void createReservationWithPointTest() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(10))
                .checkOutDate(LocalDateTime.now().plusDays(12))
                .adults(1)
                .children(0)
                .usePoint(5000)
                .guestName("김철수")
                .build();

        ReservationResponseDto response = reservationService.createReservation(requestDto);

        System.out.println("=== 포인트 사용 예약 완료 ===");
        System.out.println("사용 포인트 : 5000");
        System.out.println("최종 결제 금액 : " + response.getTotalAmount());
    }

    @DisplayName("예약 취소 테스트")
    @Test
    @Commit

    public void cancelReservationFullRefundTest() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(5))
                .checkOutDate(LocalDateTime.now().plusDays(7))
                .adults(2)
                .children(0)
                .guestName("이영희")
                .build();

        ReservationResponseDto reservation = reservationService.createReservation(requestDto);

        ReservationCancelDto cancelDto = ReservationCancelDto.builder()
                .sid(reservation.getSid())
                .cancelReason("일정 변경으로 인한 취소")
                .build();

        reservationService.cancelReservation(cancelDto);

        System.out.println("=== 예약 취소 완료 (전액 환불) ===");
        System.out.println("취소된 예약번호 : " + reservation.getReservationNumber());
        System.out.println("환불 예정 금액: " + reservation.getTotalAmount());
    }

    @DisplayName("예약 조회 테스트")
    @Test
    @Commit

    public void getReservationTest() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(3))
                .checkOutDate(LocalDateTime.now().plusDays(6))
                .adults(1)
                .children(1)
                    .guestName("황규빈")
                .build();

        ReservationResponseDto created = reservationService.createReservation(requestDto);

        ReservationResponseDto found = reservationService.getReservation(created.getSid());

    System.out.println("=== 예약 조회 완료===");
    System.out.println("예약번호 : " + found.getReservationNumber());
    System.out.println("투숙객 : " + found.getGuestName());
    System.out.println("체크인 : " + found.getCheckInDate());
    System.out.println("체크아웃 : " + found.getCheckOutDate());
    System.out.println("상태 : " + found.getReservationStatus());
    }

    @DisplayName("체크인 테스트")
    @Test
    @Commit

    public void chckInTest() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(2))
                .adults(2)
                .children(0)
                .guestName("정태환")
                .build();

        ReservationResponseDto reservation = reservationService.createReservation(requestDto);

        ReservationResponseDto checkedIn = reservationService.checkIn(reservation.getSid());

        System.out.println("=== 체크인 완료 ===");
        System.out.println("예약번호 : " + checkedIn.getReservationNumber());
        System.out.println("체크인 상태 : " + checkedIn.getReservationStatus());
        System.out.println("QR 코드 : " + (checkedIn.getCheckInQr() != null ? "생성됨" : "없음"));
    }

    @DisplayName("체크아웃 테스트")
    @Test
    @Commit

    public void checkOutTset() {
        ReservationRequestDto requestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .roomTagId(1L)
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(2))
                .adults(1)
                .children(0)
                .guestName("이상익")
                .build();

        ReservationResponseDto reservation = reservationService.createReservation(requestDto);
        reservationService.checkIn(reservation.getSid());

        ReservationResponseDto checkOut = reservationService.checkOut(reservation.getSid());

        System.out.println("=== 체크아웃 완료 ===");
        System.out.println("예약번호 : " + checkOut.getReservationNumber());
        System.out.println("최종 상태 : " + checkOut.getReservationStatus());
    }
}



