package com.mjc.hotel.review.service;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final HotelRepository hotelRepository;

    private final MemberRepository memberRepository;

    private final ReservationRepository reservationRepository;

    public Review insertReview(ReviewRequest reviewRequest) {
        Hotel hotel = hotelRepository.findById(reviewRequest.getHotelId()).orElseThrow();
        Member member = memberRepository.findById(reviewRequest.getMemberId()).orElseThrow();
        Reservation reservation = reservationRepository.findById(reviewRequest.getReservationId()).orElseThrow();

        //request로 빌더 패턴 사용
        Review review = Review.builder()
                .hotel(hotel)
                .member(member)
                .reservation(reservation)
                .rating(reviewRequest.getRating())
                .travelType(reviewRequest.getTravelType())
                .content(reviewRequest.getContent())
                .build();

        Review result = reviewRepository.save(review);
        return result;
    }
}
