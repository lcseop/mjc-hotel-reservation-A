package com.mjc.hotel.reservations.service;

import com.mjc.hotel.coupon.entity.Coupon;
import com.mjc.hotel.coupon.entity.CouponIssue;
import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.dto.*;
import com.mjc.hotel.reservations.entity.*;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationCancelRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ReservationCancelRepository reservationCancelRepository;
    private final EmailLogService emailLogService;

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + requestDto.getMemberId()));

        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다. ID: " + requestDto.getRoomId()));

        long totalNights = ChronoUnit.DAYS.between(requestDto.getCheckInDate().toLocalDate(), requestDto.getCheckOutDate().toLocalDate());

        if (totalNights <= 0) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }

        Integer originalAmount = room.getRoomPrice() * (int) totalNights;
        Integer totalAmount = originalAmount;
        Integer couponDiscount = 0;
        Integer pointDiscount = 0;
        Integer memberPoint = member.getPoint() != null ? member.getPoint() : 0;

        CouponIssue couponIssue = null;
        if (requestDto.getCouponIssueId() != null) {
            couponIssue = couponIssueRepository.findById(requestDto.getCouponIssueId())
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다. ID: " + requestDto.getCouponIssueId()));

            validateCoupon(couponIssue, member, totalAmount);

            couponDiscount = calculateDiscount(couponIssue.getCoupon(), totalAmount);
            totalAmount = Math.max(0, totalAmount - couponDiscount);

            couponIssue.setIsUsed(true);
            couponIssue.setUsedAt(LocalDateTime.now());
        }

        Integer usePoint = requestDto.getUsePoint() != null ? requestDto.getUsePoint() : 0;
        if (usePoint > 0) {
            if (memberPoint < usePoint) {
                throw new IllegalArgumentException("보유 포인트가 부족합니다.");
            }
            pointDiscount = usePoint;
            totalAmount = Math.max(0, totalAmount - usePoint);
            memberPoint -= usePoint;
            member.setPoint(memberPoint);
        }

        Integer discountAmount = couponDiscount + pointDiscount;
        String reservationNumber = generateReservationNumber();

        Reservation reservation = Reservation.builder()
                .member(member)
                .room(room)
                .couponIssue(couponIssue)
                .reservationNumber(reservationNumber)
                .checkInDate(requestDto.getCheckInDate())
                .checkOutDate(requestDto.getCheckOutDate())
                .adults(requestDto.getAdults())
                .children(requestDto.getChildren() != null ? requestDto.getChildren() : 0)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .specialRequests(requestDto.getSpecialRequests())
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .couponDiscount(couponDiscount)
                .pointDiscount(pointDiscount)
                .totalNights((int) totalNights)
                .guestName(requestDto.getGuestName())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        if (usePoint > 0) {
            PointHistory pointHistory = PointHistory.builder()
                    .reservation(savedReservation)
                    .member(member)
                    .amount(-usePoint)
                    .pointStatus(PointStatus.USE)
                    .build();
            pointHistoryRepository.save(pointHistory);
        }

        Integer earnPoint = (int) (totalAmount * 0.05);
        if (earnPoint > 0) {
            member.setPoint(memberPoint + earnPoint);
            savedReservation.setEarnedPoint(earnPoint);
            PointHistory earnHistory = PointHistory.builder()
                    .reservation(savedReservation)
                    .member(member)
                    .amount(earnPoint)
                    .pointStatus(PointStatus.ACCUMULATION)
                    .build();
            pointHistoryRepository.save(earnHistory);
        }

        String qrCode = generateQRCode(savedReservation);
        savedReservation.setCheckInQr(qrCode);

        sendConfirmationEmailSafely(savedReservation, member);

        return convertToResponseDto(savedReservation);
    }


    public ReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));
        return convertToResponseDto(reservation);
    }

    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAllWithDetails().stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    public Page<ReservationResponseDto> searchReservations(
            ReservationStatus status,
            Long memberId,
            Long hotelId,
            String keyword,
            String roomKeyword,
            Long roomTypeId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ){
        Page<Reservation> page = reservationRepository.searchAdminReservations(
                status,
                memberId,
                hotelId,
                normalizeKeyword(keyword),
                normalizeKeyword(roomKeyword),
                roomTypeId,
                dateFrom,
                dateTo,
                pageable
        );
        return page.map(this::convertToResponseDto);
    }

    private String normalizeKeyword(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    @Transactional
    public void cancelReservation(ReservationCancelDto cancelDto) {
        Reservation reservation = reservationRepository.findById(cancelDto.getSid())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + cancelDto.getSid()));

        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED || reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new IllegalArgumentException("이미 완료된 예약은 취소할 수 없습니다.");
        }

        Integer refundAmount = calculateRefundAmount(reservation);

        Member member = reservation.getMember();
        List<PointHistory> useHistories = pointHistoryRepository.findAll().stream()
                .filter(ph -> ph.getReservation().getSid().equals(reservation.getSid()) && ph.getPointStatus() == PointStatus.USE)
                .toList();
        for (PointHistory history : useHistories) {
            member.setPoint(member.getPoint() - history.getAmount());
        }

        List<PointHistory> earnHistories = pointHistoryRepository.findAll().stream()
                .filter(ph -> ph.getReservation().getSid().equals(reservation.getSid()) && ph.getPointStatus() == PointStatus.ACCUMULATION)
                .toList();
        for (PointHistory history : earnHistories) {
            member.setPoint(Math.max(0, member.getPoint() - history.getAmount()));
        }

        if (reservation.getCouponIssue() != null) {
            CouponIssue couponIssue = reservation.getCouponIssue();
            couponIssue.setIsUsed(false);
            couponIssue.setUsedAt(null);
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);

        ReservationCancel reservationCancel = ReservationCancel.builder()
                .reservation(reservation)
                .cancelReason(cancelDto.getCancelReason())
                .refundAmount(refundAmount)
                .build();
        reservationCancelRepository.save(reservationCancel);
    }

    @Transactional
    public ReservationResponseDto checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED
                && reservation.getReservationStatus() != ReservationStatus.UPCOMING) {
            throw new IllegalArgumentException("확정된 예약만 체크인 가능합니다.");
        }

        reservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        return convertToResponseDto(reservation);
    }

    @Transactional
    public ReservationResponseDto checkInByQr(String qrValue) {
        if (qrValue == null || qrValue.isBlank()) {
            throw new IllegalArgumentException("QR 값은 필수입니다.");
        }

        String value = qrValue.trim();
        Reservation reservation = reservationRepository.findByCheckInQr(value)
                .or(() -> reservationRepository.findByReservationNumber(extractReservationNumber(value)))
                .orElseThrow(() -> new IllegalArgumentException("QR에 해당하는 예약을 찾을 수 없습니다."));

        return checkIn(reservation.getSid());
    }

    private String extractReservationNumber(String qrValue) {
        if (qrValue != null && qrValue.startsWith("QR-")) {
            int lastDash = qrValue.lastIndexOf("-");

            if (lastDash > 3) {
                return qrValue.substring(3, lastDash);
            }
        }

        return qrValue;
    }

    @Transactional
    public ReservationResponseDto checkOut(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID: " + reservationId));

        if (reservation.getReservationStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalArgumentException("체크인 예약만 체크아웃 가능합니다.");
        }

        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        return convertToResponseDto(reservation);
    }

    private Integer calculateRefundAmount(Reservation reservation) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getCheckInDate());
        if (daysUntilCheckIn >= 3) {
            return reservation.getTotalAmount();
        } else if (daysUntilCheckIn >= 1) {
            return (int) (reservation.getTotalAmount() * 0.5);
        }
        return 0;
    }

    private List<CancellationPolicyDto> buildCancellationPolicies(Reservation reservation) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getCheckInDate());
        Integer totalAmount = reservation.getTotalAmount();

        List<CancellationPolicyDto> policies = new ArrayList<>();
        policies.add(CancellationPolicyDto.builder()
                .periodDescription("체크인 3일 전까지")
                .refundPercentage(100)
                .expectedRefundAmount(totalAmount)
                .applicable(daysUntilCheckIn >= 3)
                .build());
        policies.add(CancellationPolicyDto.builder()
                .periodDescription("체크인 1~2일 전")
                .refundPercentage(50)
                .expectedRefundAmount((int) (totalAmount * 0.5))
                .applicable(daysUntilCheckIn >= 1 && daysUntilCheckIn < 3)
                .build());
        return policies;
    }

    private void sendConfirmationEmailSafely(Reservation reservation, Member member) {
        try {
            EmailLogRequestDto emailRequest = new EmailLogRequestDto();
            emailRequest.setSid(reservation.getSid());
            emailRequest.setRecipientEmail(member.getEmail());
            emailLogService.sendEmailAndLog(emailRequest);
        } catch (Exception e) {

        }
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

    private Integer calculateDiscount(Coupon coupon, Integer totalAmount) {
        switch (coupon.getDiscountType()) {
            case PERCENT:
                return (int) (totalAmount * (coupon.getDiscountValue() / 100.0));
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
        Room room = reservation.getRoom();
        var hotel = room.getHotelId();

        return ReservationResponseDto.builder()
                .sid(reservation.getSid())
                .reservationNumber(reservation.getReservationNumber())
                .memberId(reservation.getMember().getSid())
                .memberName(reservation.getMember().getName())
                .roomId(room.getSid())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .roomPrice(room.getRoomPrice())
                .roomTypeTitle(room.getRoomTypeId() != null ? room.getRoomTypeId().getTitle() : null)
                .roomParking(room.getParking())
                .hotelId(hotel.getSid())
                .hotelName(hotel.getHotelName())
                .hotelLocation(hotel.getLocation())
                .hotelStarRating(hotel.getStarRating())
                .guestName(reservation.getGuestName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .totalNights(reservation.getTotalNights())
                .adults(reservation.getAdults())
                .children(reservation.getChildren())
                .originalAmount(reservation.getOriginalAmount())
                .discountAmount(reservation.getDiscountAmount())
                .couponDiscount(reservation.getCouponDiscount())
                .pointDiscount(reservation.getPointDiscount())
                .totalAmount(reservation.getTotalAmount())
                .earnedPoint(reservation.getEarnedPoint())
                .reservationStatus(reservation.getReservationStatus())
                .specialRequests(reservation.getSpecialRequests())
                .checkInQr(reservation.getCheckInQr())
                .cancellationPolicyDto(buildCancellationPolicies(reservation))
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    public ReservationStatsDto getReservationStats() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();

        return ReservationStatsDto.builder()
                .todayNewReservations(reservationRepository.countByCreatedAtBetween(todayStart, todayEnd))
                .todayCheckIns(reservationRepository.countByCheckInDateBetween(todayStart, todayEnd))
                .todayCheckOuts(reservationRepository.countByCheckOutDateBetween(todayStart, todayEnd))
                .monthlyReservations(reservationRepository.countByCreatedAtBetween(monthStart, todayEnd))
                .pendingCount(reservationRepository.countByReservationStatus(ReservationStatus.PENDING))
                .build();
    }

}
