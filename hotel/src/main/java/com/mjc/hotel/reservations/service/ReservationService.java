package com.mjc.hotel.reservations.service;

import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.dto.ReservationCancelDto;
import com.mjc.hotel.reservations.dto.ReservationRequestDto;
import com.mjc.hotel.reservations.dto.ReservationResponseDto;
import com.mjc.hotel.reservations.entity.*;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationCancelRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ReservationCancelRepository reservationCancelRepository;

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId()).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + requestDto.getMemberId()));

        Room room = roomRepository.findById(requestDto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다. ID: " + requestDto.getRoomId()));

        long totalNights = ChronoUnit.DAYS.between(requestDto.getCheckInDate().toLocalDate(), requestDto.getCheckOutDate().toLocalDate());

        if (totalNights <= 0) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }

        Integer totalAmount = room.getRoomPrice() * (int) totalNights;

        CouponIssue couponIssue = null;
        if (requestDto.getCouponIssueId() != null) {
            couponIssue = couponIssueRepository.findById(requestDto.getCouponIssueId()).orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다. ID: " + requestDto.getCouponIssueId()));

            validateCoupon(couponIssue, member, totalAmount);

            Integer discountAmount = calculateDiscount(couponIssue.getCoupon(), totalAmount);
            totalAmount = Math.max(0, totalAmount - discountAmount);

            couponIssue.setIsUsed(true);
            couponIssue.setUsedAt(LocalDateTime.now());
        }

        Integer usePoint = requestDto.getUsePoint() != null ? requestDto.getUsePoint() : 0;
        if (usePoint > 0) {
            if (member.getPoint() < usePoint) {
                throw new IllegalArgumentException("보유 포인트가 부족합니다.");
            }
            totalAmount = Math.max(0, totalAmount - usePoint);
            member.setPoint(member.getPoint() - usePoint);
        }

        String reservationNumber = generateReservationNumber();

        Reservation reservation = Reservation.builder().member(member).room(room).couponIssue(couponIssue).reservationNumber(reservationNumber).checkInDate(requestDto.getCheckInDate()).checkOutDate(requestDto.getCheckOutDate()).adults(requestDto.getAdults()).children(requestDto.getChildren() != null ? requestDto.getChildren() : 0).reservationStatus(ReservationStatus.CONFIRMED).totalAmount(totalAmount).specialRequests(requestDto.getSpecialRequests()).totalNights((int) totalNights).guestName(requestDto.getGuestName()).build();

        Reservation savedReservation = reservationRepository.save(reservation);

        if (usePoint > 0) {
            PointHistory pointHistory = PointHistory.builder().reservation(savedReservation).member(member).amount(-usePoint).pointStatus(PointStatus.USE).build();
            pointHistoryRepository.save(pointHistory);
        }

        Integer earnPoint = (int) (totalAmount * 0.05);
        if (earnPoint > 0) {
            member.setPoint(member.getPoint() + earnPoint);
            PointHistory earnHistory = PointHistory.builder().reservation(savedReservation).member(member).amount(earnPoint).pointStatus(PointStatus.ACCUMULATION).build();
            pointHistoryRepository.save(earnHistory);
        }
        return convertToResponseDto(savedReservation);
    }


    public ReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));
        return convertToResponseDto(reservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAllWithDetails().stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(ReservationCancelDto cancelDto) {
        Reservation reservation = reservationRepository.findById(cancelDto.getSid()).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + cancelDto.getSid()));

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED || reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new IllegalArgumentException("이미 완료된 예약은 취소할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(now, reservation.getCheckInDate());

        Integer refundAmount = 0;
        if (daysUntilCheckIn >= 3) {
            refundAmount = reservation.getTotalAmount();
        } else if (daysUntilCheckIn >= 1) {
            refundAmount = (int) (reservation.getTotalAmount() * 0.5); // 50% 환불
        }

        Member member = reservation.getMember();
        List<PointHistory> useHistories = pointHistoryRepository.findAll().stream().filter(ph -> ph.getReservation().getSid().equals(reservation.getSid()) && ph.getPointStatus() == PointStatus.USE).collect(Collectors.toList());

        for (PointHistory history : useHistories) {
            member.setPoint(member.getPoint() - history.getAmount());
        }

        List<PointHistory> earnHistories = pointHistoryRepository.findAll().stream().filter(ph -> ph.getReservation().getSid().equals(reservation.getSid()) && ph.getPointStatus() == PointStatus.ACCUMULATION).collect(Collectors.toList());

        for (PointHistory history : earnHistories) {
            member.setPoint(Math.max(0, member.getPoint() - history.getAmount()));
        }

        if (reservation.getCouponIssue() != null) {
            CouponIssue couponIssue = reservation.getCouponIssue();
            couponIssue.setIsUsed(false);
            couponIssue.setUsedAt(null);
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);

        ReservationCancel reservationCancel = ReservationCancel.builder().reservation(reservation).cancelReason(cancelDto.getCancelReason()).refundAmount(refundAmount).build();
        reservationCancelRepository.save(reservationCancel);
    }

    @Transactional
    public ReservationResponseDto checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("확정된 예약만 체크인 가능합니다.");
        }

        reservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        String qrCode = generateQRCode(reservation);
        reservation.setCheckInQr(qrCode);

        return convertToResponseDto(reservation);
    }

    @Transactional
    public ReservationResponseDto checkOut(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getReservationStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalArgumentException("체크인 예약만 체크아웃 가능합니다.");
        }

        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        return convertToResponseDto(reservation);
    }

    private void validateCoupon(CouponIssue couponIssue, Member member, Integer totalAmount) {
        var coupon = couponIssue.getCoupon();

        if (Boolean.TRUE.equals(couponIssue.getIsUsed())) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }

        if (!couponIssue.getMember().getSid().equals(member.getSid())) {
            throw new IllegalArgumentException("본인에게 발급된 쿠폰만 사용할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new IllegalArgumentException("쿠폰 사용 가능 기간이 아닙니다.");
        }

        if (coupon.getMinOrderAmount() != null && totalAmount < coupon.getMinOrderAmount()) {
            throw new IllegalArgumentException(String.format("최소 주문 금액 %d원 이상부터 사용 가능한 쿠폰입니다.", coupon.getMinOrderAmount()));
        }
    }

    private Integer calculateDiscount(com.mjc.hotel.coupon.entity.Coupon coupon, Integer totalAmount) {
        switch(coupon.getDiscountType()) {
            case PERCENT:
                return(int)(totalAmount * (coupon.getDiscountValue() / 100.0));
            case FIXED:
                return coupon.getDiscountValue().intValue();
            default:
                return 0;
        }
    }

    private String generateReservationNumber() {
        return "RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateQRCode(Reservation reservation) {
        return "QR-" + reservation.getReservationNumber() + "-" + System.currentTimeMillis();
    }

    private ReservationResponseDto convertToResponseDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .sid(reservation.getSid())
                .reservationNumber(reservation.getReservationNumber())
                .memberId(reservation.getMember().getSid())
                .memberName(reservation.getMember().getName())
                .roomId(reservation.getRoom().getSid())
                .roomNumber(reservation.getRoom().getRoomNumber())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .adults(reservation.getAdults())
                .children(reservation.getChildren())
                .reservationStatus(reservation.getReservationStatus())
                .totalAmount(reservation.getTotalAmount())
                .specialRequests(reservation.getSpecialRequests())
                .guestName(reservation.getGuestName())
                .checkInQr(reservation.getCheckInQr())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
