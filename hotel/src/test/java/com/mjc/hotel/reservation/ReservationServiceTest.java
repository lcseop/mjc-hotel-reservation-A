package com.mjc.hotel.reservation;

import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.coupon.entity.CouponDiscountType;
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
import com.mjc.hotel.reservations.service.EmailLogService;
import com.mjc.hotel.reservations.service.ReservationService;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private CouponIssueRepository couponIssueRepository;
    @Mock private PointHistoryRepository pointHistoryRepository;
    @Mock private ReservationCancelRepository reservationCancelRepository;
    @Mock private EmailLogService emailLogService;

    @InjectMocks private ReservationService reservationService;

    private Member member;
    private Room room;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .sid(1L).name("홍길동")
                .email("test@test.com")
                .point(10000)
                .build();
        room = Room.builder()
                .sid(1L).
                roomNumber(101)
                .roomPrice(100000)
                .build();
    }

    private ReservationRequestDto baseRequest() {
        return ReservationRequestDto.builder()
                .memberId(1L)
                .roomId(1L)
                .checkInDate(LocalDateTime.now().plusDays(7))
                .checkOutDate(LocalDateTime.now().plusDays(9))
                .adults(2)
                .children(0)
                .guestName("홍길동")
                .usePoint(0)
                .build();
    }

    @Test
    @DisplayName("createReservation - 정상 생성 시 결제 금액과 상태가 올바르게 계산된다")
    void createReservation_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponseDto response = reservationService.createReservation(baseRequest());

        assertThat(response.getOriginalAmount()).isEqualTo(200000);
        assertThat(response.getTotalAmount()).isEqualTo(200000);
        assertThat(response.getReservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.getGuestName()).isEqualTo("홍길동");
        verify(emailLogService, times(1)).sendEmailAndLog(any());
    }

    @Test
    @DisplayName("createReservation - 존재하지 않는 회원이면 예외가 발생한다")
    void createReservation_memberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(baseRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("createReservation - 존재하지 않는 객실이면 예외가 발생한다")
    void createReservation_roomNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(baseRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("객실을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("createReservation - 체크아웃이 체크인보다 빠르면 예외가 발생한다")
    void createReservation_invalidDateRange() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        ReservationRequestDto request = baseRequest();
        request.setCheckInDate(LocalDateTime.now().plusDays(5));
        request.setCheckOutDate(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다");
    }

    @Test
    @DisplayName("createReservation - 보유 포인트보다 많은 포인트 사용 시 예외가 발생한다")
    void createReservation_insufficientPoint() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        ReservationRequestDto request = baseRequest();
        request.setUsePoint(999999);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유 포인트가 부족합니다");
    }

    @Test
    @DisplayName("createReservation - 쿠폰 적용 시 할인 금액이 반영된다")
    void createReservation_withCoupon() {
        Coupon coupon = Coupon.builder().discountType(CouponDiscountType.FIXED).discountValue(20000.0).build();
        CouponIssue couponIssue = CouponIssue.builder().sid(1L).member(member).coupon(coupon).isUsed(false).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(couponIssueRepository.findById(1L)).thenReturn(Optional.of(couponIssue));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationRequestDto request = baseRequest();
        request.setCouponIssueId(1L);

        ReservationResponseDto response = reservationService.createReservation(request);

        assertThat(response.getCouponDiscount()).isEqualTo(0);
        assertThat(response.getTotalAmount()).isEqualTo(180000);
        assertThat(couponIssue.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("createReservation - 이미 사용된 쿠폰이면 예외가 발생한다")
    void createReservation_alreadyUsedCoupon() {
        Coupon coupon = Coupon.builder().discountType(CouponDiscountType.FIXED).discountValue(20000.0).build();
        CouponIssue couponIssue = CouponIssue.builder().sid(1L).member(member).coupon(coupon).isUsed(true).build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(couponIssueRepository.findById(1L)).thenReturn(Optional.of(couponIssue));

        ReservationRequestDto request = baseRequest();
        request.setCouponIssueId(1L);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용된 쿠폰입니다");
    }

    @Test
    @DisplayName("getReservation - 예약 ID로 조회에 성공한다")
    void getReservation_success() {
        Reservation reservation = sampleReservation();
        when(reservationRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(reservation));

        ReservationResponseDto response = reservationService.getReservation(1L);

        assertThat(response.getSid()).isEqualTo(1L);
        assertThat(response.getReservationNumber()).isEqualTo("RSV-TEST1234");
    }

    @Test
    @DisplayName("getReservation - 존재하지 않으면 예외가 발생한다")
    void getReservation_notFound() {
        when(reservationRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("getAllReservations - 전체 목록을 반환한다")
    void getAllReservations_success() {
        when(reservationRepository.findAllWithDetails()).thenReturn(List.of(sampleReservation()));

        List<ReservationResponseDto> result = reservationService.getAllReservations();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("cancelReservation - 체크인 3일 이상 남으면 전액 환불된다")
    void cancelReservation_fullRefund() {
        Reservation reservation = sampleReservation();
        reservation.setCheckInDate(LocalDateTime.now().plusDays(5));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationCancelDto cancelDto = ReservationCancelDto.builder().sid(1L).cancelReason("일정 변경").build();
        reservationService.cancelReservation(cancelDto);

        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationCancelRepository, times(1)).save(any(ReservationCancel.class));
    }

    @Test
    @DisplayName("cancelReservation - 이미 취소된 예약이면 예외가 발생한다")
    void cancelReservation_alreadyCancelled() {
        Reservation reservation = sampleReservation();
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationCancelDto cancelDto = ReservationCancelDto.builder().sid(1L).cancelReason("사유").build();

        assertThatThrownBy(() -> reservationService.cancelReservation(cancelDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 취소된 예약입니다");
    }

    @Test
    @DisplayName("cancelReservation - 완료된 예약이면 예외가 발생한다")
    void cancelReservation_alreadyCompleted() {
        Reservation reservation = sampleReservation();
        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationCancelDto cancelDto = ReservationCancelDto.builder().sid(1L).cancelReason("사유").build();

        assertThatThrownBy(() -> reservationService.cancelReservation(cancelDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 완료된 예약은 취소할 수 없습니다");
    }

    @Test
    @DisplayName("checkIn - CONFIRMED 상태의 예약은 체크인에 성공한다")
    void checkIn_success() {
        Reservation reservation = sampleReservation();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponseDto response = reservationService.checkIn(1L);

        assertThat(response.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
    }

    @Test
    @DisplayName("checkIn - CONFIRMED 상태가 아니면 예외가 발생한다")
    void checkIn_invalidStatus() {
        Reservation reservation = sampleReservation();
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.checkIn(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("확정된 예약만 체크인 가능합니다");
    }

    @Test
    @DisplayName("checkOut - CHECKED_IN 상태의 예약은 체크아웃에 성공한다")
    void checkOut_success() {
        Reservation reservation = sampleReservation();
        reservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponseDto response = reservationService.checkOut(1L);

        assertThat(response.getReservationStatus()).isEqualTo(ReservationStatus.CHECKED_OUT);
    }

    @Test
    @DisplayName("checkOut - CHECKED_IN 상태가 아니면 예외가 발생한다")
    void checkOut_invalidStatus() {
        Reservation reservation = sampleReservation();
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.checkOut(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("체크인 예약만 체크아웃 가능합니다");
    }

    private Reservation sampleReservation() {
        return Reservation.builder()
                .sid(1L)
                .member(member)
                .room(room)
                .reservationNumber("RSV-TEST1234")
                .checkInDate(LocalDateTime.now().plusDays(7))
                .checkOutDate(LocalDateTime.now().plusDays(9))
                .adults(2)
                .children(0)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .reservationChannel(ReservationChannel.DIRECT)
                .originalAmount(200000)
                .discountAmount(0)
                .couponDiscount(0)
                .pointDiscount(0)
                .totalAmount(200000)
                .earnedPoint(10000)
                .totalNights(2)
                .guestName("홍길동")
                .build();
    }
}


