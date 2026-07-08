package com.mjc.hotel.reservations.controller;

import com.mjc.hotel.reservations.dto.ReservationCancelDto;
import com.mjc.hotel.reservations.dto.ReservationRequestDto;
import com.mjc.hotel.reservations.dto.ReservationResponseDto;
import com.mjc.hotel.reservations.dto.ReservationStatsDto;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.GenericResponseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/reservation")
@RequiredArgsConstructor
@Tag(name = "예약", description = "예약 관리 API")
public class ReservationController {

    private final ReservationService reservationService;
    private final GenericResponseService responseBuilder;

    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 예약을 생성합니다")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @Valid @RequestBody ReservationRequestDto requestDto) {
        ReservationResponseDto response = reservationService.createReservation(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 조회", description = "예약 ID로 예약 정보를 조회합니다")
    public ResponseEntity<ReservationResponseDto> getReservation(
            @PathVariable Long reservationId) {
                ReservationResponseDto response = reservationService.getReservation(reservationId);
                return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "예약 목록 조회", description = "모든 예약 목록을 조회합니다")
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        List<ReservationResponseDto> response = reservationService.getAllReservations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "예약 검색", description = "상태/회원 기준으로 예약을 페이징 조회합니다")
    public ResponseEntity<Page<ReservationResponseDto>> searchReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Long memberId,
            Pageable pageable) {
        Page<ReservationResponseDto> response = reservationService.searchReservations(status, memberId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "예약 통계", description = "관리자 대시보드용 예약 통계를 조회합니다")
    public ResponseEntity<ReservationStatsDto> getReservationStats() {
        ReservationStatsDto response = reservationService.getReservationStats();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    @Operation(summary = "예약 취소", description = "예약을 취소하고 환불을 처리합니다")
    public ResponseEntity<Void> cancelReservation(
            @Valid @RequestBody ReservationCancelDto cancelDto) {
        reservationService.cancelReservation(cancelDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reservationId}/check-in")
    @Operation(summary = "체크인", description = "예약에 대한 체크인을 처리합니다")
    public ResponseEntity<ReservationResponseDto> checkIn(
            @PathVariable Long reservationId) {
        ReservationResponseDto response = reservationService.checkIn(reservationId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/check-in/qr")
    @Operation(summary = "QR 체크인", description = "체크인 QR 값으로 예약 체크인을 처리합니다")
    public ResponseEntity<ReservationResponseDto> checkInByQr(
            @RequestBody Map<String, String> request) {
        ReservationResponseDto response = reservationService.checkInByQr(request.get("qrValue"));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reservationId}/check-out")
    @Operation(summary = "체크아웃", description = "예약에 대한 체크아웃을 처리합니다")
    public ResponseEntity<ReservationResponseDto> checkOut(
            @PathVariable Long reservationId) {
        ReservationResponseDto response = reservationService.checkOut(reservationId);
        return ResponseEntity.ok(response);
    }
}
