package com.mjc.hotel.payments.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.converter.PaymentsDtoMapper;
import com.mjc.hotel.payments.dto.PaymentsRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentConfirmRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentFailRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentReadyRequestDto;
import com.mjc.hotel.payments.dto.TossPaymentReadyResponseDto;
import com.mjc.hotel.payments.entity.PaymentMethod;
import com.mjc.hotel.payments.entity.PaymentStatus;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.entity.PointHistory;
import com.mjc.hotel.reservations.entity.PointStatus;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.reservations.service.EmailLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final EmailLogService emailLogService;
    private final PaymentsDtoMapper paymentsDtoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${toss.payments.secret-key:}")
    private String tossSecretKey;

    public List<Payments> getPayments() {
        return paymentsRepository.findAll();
    }

    public Payments getPayment(Long sid) {
        return paymentsRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. sid=" + sid));
    }

    @Transactional
    public Payments savePayment(PaymentsRequestDto dto) {
        return paymentsRepository.save(
                paymentsDtoMapper.toEntity(dto, getReservation(dto.getReservationId()), getMember(dto.getSid()))
        );
    }

    @Transactional
    public TossPaymentReadyResponseDto readyTossPayment(TossPaymentReadyRequestDto dto) {
        Reservation reservation = getReservation(dto.getReservationId());
        Member member = getMember(dto.getMemberId());
        BigDecimal amount = requirePositiveAmount(dto.getAmount());

        if (!reservation.getMember().getSid().equals(member.getSid())) {
            throw new IllegalArgumentException("예약자와 결제 회원이 일치하지 않습니다.");
        }

        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태의 예약만 결제를 시작할 수 있습니다.");
        }

        BigDecimal reservationAmount = BigDecimal.valueOf(reservation.getTotalAmount());
        if (reservationAmount.compareTo(amount) != 0) {
            throw new IllegalArgumentException("예약 금액과 결제 금액이 일치하지 않습니다.");
        }

        String orderId = createTossOrderId(reservation.getSid());
        String orderName = normalizeOrderName(dto.getOrderName(), reservation);

        Payments payment = Payments.builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(amount)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .provider("TOSS")
                .requestedAt(LocalDateTime.now())
                .point(0)
                .build();

        Payments saved = paymentsRepository.save(payment);

        return TossPaymentReadyResponseDto.builder()
                .paymentId(saved.getSid())
                .reservationId(reservation.getSid())
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .build();
    }

    @Transactional
    public Payments confirmTossPayment(TossPaymentConfirmRequestDto dto) {
        Payments payment = paymentsRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 요청 정보를 찾을 수 없습니다. orderId=" + dto.getOrderId()));

        if (!payment.getReservation().getSid().equals(dto.getReservationId())) {
            throw new IllegalArgumentException("예약 정보와 결제 요청 정보가 일치하지 않습니다.");
        }

        BigDecimal amount = requirePositiveAmount(dto.getAmount());
        if (payment.getPaymentAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("결제 요청 금액과 승인 금액이 일치하지 않습니다.");
        }

        JsonNode tossResponse = requestTossConfirm(dto);
        String approvedAt = text(tossResponse, "approvedAt");
        String receiptUrl = tossResponse.path("receipt").path("url").asText(null);

        payment.setPaymentKey(text(tossResponse, "paymentKey"));
        payment.setTransactionNo(text(tossResponse, "paymentKey"));
        payment.setPaymentMethod(resolvePaymentMethod(text(tossResponse, "method")));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setReceiptUrl(receiptUrl);
        payment.setApprovedAt(parseTossDateTime(approvedAt));
        payment.setPaidAt(payment.getApprovedAt() != null ? payment.getApprovedAt() : LocalDateTime.now());
        payment.setFailCode(null);
        payment.setFailMessage(null);
        payment.setPoint((int) Math.floor(amount.doubleValue() * 0.005));

        confirmReservationAfterPayment(payment);

        return payment;
    }

    @Transactional
    public Payments failTossPayment(TossPaymentFailRequestDto dto) {
        Payments payment = paymentsRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 요청 정보를 찾을 수 없습니다. orderId=" + dto.getOrderId()));

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailCode(dto.getCode());
        payment.setFailMessage(dto.getMessage());

        cancelPendingReservationAfterPaymentFailure(payment);

        return payment;
    }

    private void confirmReservationAfterPayment(Payments payment) {
        Reservation reservation = payment.getReservation();
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약은 결제를 승인할 수 없습니다.");
        }

        boolean shouldSendEmail = reservation.getReservationStatus() != ReservationStatus.CONFIRMED;
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);

        Member member = reservation.getMember();
        Integer earnPoint = payment.getPoint() != null ? payment.getPoint() : 0;
        boolean alreadyEarned = !pointHistoryRepository
                .findByReservationSidAndPointStatus(reservation.getSid(), PointStatus.ACCUMULATION)
                .isEmpty();

        if (earnPoint > 0 && !alreadyEarned) {
            Integer memberPoint = member.getPoint() != null ? member.getPoint() : 0;
            member.setPoint(memberPoint + earnPoint);
            reservation.setEarnedPoint(earnPoint);
            pointHistoryRepository.save(PointHistory.builder()
                    .reservation(reservation)
                    .member(member)
                    .amount(earnPoint)
                    .pointStatus(PointStatus.ACCUMULATION)
                    .build());
        }

        if (shouldSendEmail) {
            sendConfirmationEmailSafely(reservation, member);
        }
    }

    private void cancelPendingReservationAfterPaymentFailure(Payments payment) {
        Reservation reservation = payment.getReservation();
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            return;
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);

        if (reservation.getCouponIssue() != null) {
            reservation.getCouponIssue().setIsUsed(false);
            reservation.getCouponIssue().setUsedAt(null);
        }

        Integer pointDiscount = reservation.getPointDiscount() != null ? reservation.getPointDiscount() : 0;
        boolean alreadyRefunded = !pointHistoryRepository
                .findByReservationSidAndPointStatus(reservation.getSid(), PointStatus.USE_CANCEL_REFUND)
                .isEmpty();

        if (pointDiscount > 0 && !alreadyRefunded) {
            Member member = reservation.getMember();
            Integer memberPoint = member.getPoint() != null ? member.getPoint() : 0;
            member.setPoint(memberPoint + pointDiscount);
            pointHistoryRepository.save(PointHistory.builder()
                    .reservation(reservation)
                    .member(member)
                    .amount(pointDiscount)
                    .pointStatus(PointStatus.USE_CANCEL_REFUND)
                    .build());
        }
    }

    private void sendConfirmationEmailSafely(Reservation reservation, Member member) {
        try {
            EmailLogRequestDto emailRequest = new EmailLogRequestDto();
            emailRequest.setSid(reservation.getSid());
            emailRequest.setRecipientEmail(member.getEmail());
            emailLogService.sendEmailAndLog(emailRequest);
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public String refundPayment(Payments payment, BigDecimal refundAmount, String reason) {
        if (payment == null) {
            throw new IllegalArgumentException("환불할 결제 정보가 없습니다.");
        }

        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (!"TOSS".equalsIgnoreCase(payment.getProvider())) {
            throw new IllegalStateException("토스 결제가 아니어서 자동 환불할 수 없습니다. provider=" + payment.getProvider());
        }

        if (payment.getPaymentKey() == null || payment.getPaymentKey().isBlank()) {
            throw new IllegalStateException("토스 결제 취소에 필요한 paymentKey가 없습니다.");
        }

        JsonNode cancelResponse = requestTossCancel(payment.getPaymentKey(), refundAmount, reason);
        String pgTransactionKey = extractCancelTransactionKey(cancelResponse);

        BigDecimal paidAmount = payment.getPaymentAmount() != null ? payment.getPaymentAmount() : BigDecimal.ZERO;
        if (refundAmount.compareTo(paidAmount) >= 0) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        return pgTransactionKey;
    }

    @Transactional
    public Payments updatePayment(Long sid, PaymentsRequestDto dto) {
        Payments payment = getPayment(sid);
        if (dto.getReservationId() != null) {
            payment.setReservation(getReservation(dto.getReservationId()));
        }
        if (dto.getSid() != null) {
            payment.setMember(getMember(dto.getSid()));
        }
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setTransactionNo(dto.getTransactionNo());
        payment.setOrderId(dto.getOrderId());
        payment.setPaymentKey(dto.getPaymentKey());
        payment.setProvider(dto.getProvider());
        payment.setReceiptUrl(dto.getReceiptUrl());
        payment.setFailCode(dto.getFailCode());
        payment.setFailMessage(dto.getFailMessage());
        payment.setRequestedAt(dto.getRequestedAt());
        payment.setApprovedAt(dto.getApprovedAt());
        payment.setPaidAt(dto.getPaidAt());
        payment.setPoint(dto.getPoint());

        return payment;
    }

    @Transactional
    public void deletePayment(Long sid) {
        Payments payment = getPayment(sid);
        payment.markDeleted();
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. reservationId=" + reservationId));
    }

    private Member getMember(Long sid) {
        return memberRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. sid=" + sid));
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }
        return amount;
    }

    private String createTossOrderId(Long reservationId) {
        return "SN-" + reservationId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
    }

    private String normalizeOrderName(String orderName, Reservation reservation) {
        if (orderName != null && !orderName.isBlank()) {
            return orderName.trim();
        }
        return reservation.getRoom().getHotelId().getHotelName() + " " + reservation.getRoom().getRoomName();
    }

    private JsonNode requestTossConfirm(TossPaymentConfirmRequestDto dto) {
        if (tossSecretKey == null || tossSecretKey.isBlank()) {
            throw new IllegalStateException("토스 시크릿 키가 설정되지 않았습니다.");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "paymentKey", dto.getPaymentKey(),
                    "orderId", dto.getOrderId(),
                    "amount", dto.getAmount().longValue()
            ));
            String basicToken = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                    .header("Authorization", "Basic " + basicToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(body.path("message").asText("토스 결제 승인에 실패했습니다."));
            }

            return body;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 승인 요청 중 오류가 발생했습니다.", e);
        }
    }

    private JsonNode requestTossCancel(String paymentKey, BigDecimal refundAmount, String reason) {
        if (tossSecretKey == null || tossSecretKey.isBlank()) {
            throw new IllegalStateException("토스 시크릿 키가 설정되지 않았습니다.");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "cancelReason", reason != null && !reason.isBlank() ? reason : "예약 취소",
                    "cancelAmount", refundAmount.longValue()
            ));
            String basicToken = Base64.getEncoder()
                    .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            String encodedPaymentKey = URLEncoder.encode(paymentKey, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tosspayments.com/v1/payments/" + encodedPaymentKey + "/cancel"))
                    .header("Authorization", "Basic " + basicToken)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", "refund-" + paymentKey + "-" + refundAmount.longValue())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(body.path("message").asText("토스 결제 취소에 실패했습니다."));
            }

            return body;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 취소 요청 중 오류가 발생했습니다.", e);
        }
    }

    private String extractCancelTransactionKey(JsonNode cancelResponse) {
        JsonNode cancels = cancelResponse.path("cancels");
        if (cancels.isArray() && !cancels.isEmpty()) {
            JsonNode lastCancel = cancels.get(cancels.size() - 1);
            String transactionKey = lastCancel.path("transactionKey").asText(null);
            if (transactionKey != null && !transactionKey.isBlank()) {
                return transactionKey;
            }
        }

        return text(cancelResponse, "paymentKey");
    }

    private PaymentMethod resolvePaymentMethod(String method) {
        if (method == null) {
            return PaymentMethod.ONLINE;
        }

        if (method.contains("카드") || method.equalsIgnoreCase("CARD")) {
            return PaymentMethod.CARD;
        }

        if (method.contains("계좌") || method.contains("이체")) {
            return PaymentMethod.BANK_TRANSFER;
        }

        return PaymentMethod.ONLINE;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private LocalDateTime parseTossDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
