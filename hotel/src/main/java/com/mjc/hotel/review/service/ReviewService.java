package com.mjc.hotel.review.service;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.review.entity.*;
import com.mjc.hotel.review.repository.*;
import com.mjc.hotel.review.request.ReviewCategoryRequest;
import com.mjc.hotel.review.request.ReviewCreateRequest;
import com.mjc.hotel.review.request.ReviewTagRequest;
import com.mjc.hotel.review.response.ReviewCategoryResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.response.ReviewTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;

    private final ReviewCategoryRepository reviewCategoryRepository;

    private final ReviewTagMasterRepository reviewTagMasterRepository;

    private final ReviewTagRepository reviewTagRepository;

    private final HotelRepository hotelRepository;

    private final MemberRepository memberRepository;

    private final ReservationRepository reservationRepository;

    public ReviewResponse insertReview(ReviewCreateRequest reviewRequest) {
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
                .likeCount(0)
                .dislikeCount(0)
                .build();

        Review reviewResult = reviewRepository.save(review);

        List<ReviewCategory> categories = this.insertReviewCategories(reviewRequest, reviewResult);

        List<ReviewTag> tags = this.insertReviewTags(reviewRequest, reviewResult);

        ReviewResponse result = ReviewResponse.builder()
                .reviewId(reviewResult.getReviewId())
                .hotelId(reviewResult.getHotel().getSid())
                .memberId(reviewResult.getMember().getMemberId())
                .reservationId(reviewResult.getReservation().getSid())
                .rating(reviewResult.getRating())
                .travelType(reviewResult.getTravelType())
                .content(reviewResult.getContent())
                .likeCount(reviewResult.getLikeCount())
                .dislikeCount(reviewResult.getDislikeCount())
                .categories(
                        categories.stream()
                                .map(reviewCategory -> ReviewCategoryResponse.builder()
                                        .reviewCategoryId(reviewCategory.getReviewCategoryId())
                                        .reviewCategoryMasterId(reviewCategory.getReviewCategoryMaster().getReviewCategoryMasterId())
                                        .reviewId(reviewCategory.getReview().getReviewId())
                                        .rating(reviewCategory.getRating())
                                        .build())
                                .toList()
                )
                .tags(
                        tags.stream()
                                .map(reviewTag -> ReviewTagResponse.builder()
                                        .reviewId(reviewTag.getReview().getReviewId())
                                        .reviewTageMapId(reviewTag.getReviewTagMaster().getReviewTagMasterId())
                                        .build()
                                )
                                .toList()
                )
                .createdAt(reviewResult.getCreatedAt())
                .updatedAt(reviewResult.getUpdatedAt())
                .deletedAt(reviewResult.getDeletedAt())
                .deleted(reviewResult.getDeleted())
                .build();

        return result;
    }

    private List<ReviewCategory> insertReviewCategories(ReviewCreateRequest reviewRequest, Review reviewResult) {
        List<ReviewCategory> categories = new ArrayList<>();

        if(reviewRequest.getCategories() != null && !reviewRequest.getCategories().isEmpty()){

            for(ReviewCategoryRequest reviewCategoryRequest : reviewRequest.getCategories()){
                ReviewCategoryMaster reviewCategoryMaster = reviewCategoryMasterRepository
                        .findById(reviewCategoryRequest.getCategoryId())
                        .orElseThrow();

                ReviewCategory reviewCategory = ReviewCategory.builder()
                        .review(reviewResult)
                        .reviewCategoryMaster(reviewCategoryMaster)
                        .rating(reviewCategoryRequest.getRating())
                        .build();

                ReviewCategory categoryResult = reviewCategoryRepository.save(reviewCategory);

                categories.add(categoryResult);
            }
        }

        return categories;
    }

    private List<ReviewTag> insertReviewTags(ReviewCreateRequest reviewRequest, Review reviewResult) {
        List<ReviewTag> tags = new ArrayList<>();
        if(reviewRequest.getTags() != null && !reviewRequest.getTags().isEmpty()) {
            for(ReviewTagRequest reviewTagRequest : reviewRequest.getTags()) {
                ReviewTagMaster reviewTagMaster = reviewTagMasterRepository.findById(reviewTagRequest.getTagId()).orElseThrow();

                ReviewTag reviewTag = ReviewTag.builder()
                        .review(reviewResult)
                        .reviewTagMaster(reviewTagMaster)
                        .build();

                ReviewTag tagResult = reviewTagRepository.save(reviewTag);

                tags.add(tagResult);
            }
        }
        return tags;
    }
}
