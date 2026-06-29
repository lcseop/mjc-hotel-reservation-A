package com.mjc.hotel.reservations.service;

import com.mjc.hotel.coupon.repository.CouponIssueRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.dto.ReservationRequestDto;
import com.mjc.hotel.reservations.dto.ReservationResponseDto;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationCancelRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ReservationCancelRepository reservationCancelRepository;

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. ID: " + requestDto.getMemberId()));
    }

}
