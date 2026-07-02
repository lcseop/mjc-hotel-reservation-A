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
import com.mjc.hotel.review.request.ReviewUpdateRequest;
import com.mjc.hotel.review.response.ReviewCategoryResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.response.ReviewTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final ReviewPhotoRepository reviewPhotoRepository;

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

        review.prePersist();

        Review reviewResult = reviewRepository.save(review);

        List<ReviewCategory> categories = this.insertReviewCategories(reviewRequest.getCategories(), reviewResult);
        List<ReviewTag> tags = this.insertReviewTags(reviewRequest.getTags(), reviewResult);

        ReviewResponse result = this.toReviewResponse(reviewResult,categories, tags);

        return result;
    }

    @Transactional
    public ReviewResponse updateReview(ReviewUpdateRequest reviewUpdateRequest) {
        Review updateBefore = reviewRepository.findBySidAndDeletedFalse(reviewUpdateRequest.getSid());

        Review review = Review.builder()
                .sid(updateBefore.getSid())
                .hotel(updateBefore.getHotel())
                .member(updateBefore.getMember())
                .reservation(updateBefore.getReservation())
                .rating(reviewUpdateRequest.getRating())
                .travelType(reviewUpdateRequest.getTravelType())
                .content(reviewUpdateRequest.getContent())
                .likeCount(updateBefore.getLikeCount())
                .dislikeCount(updateBefore.getDislikeCount())
                .build();
        //생성시간 그대로 넘겨주기
        review.setCreatedAt(updateBefore.getCreatedAt());
        review.prePersist();

        Review updateAfter = reviewRepository.save(review);

        //기존 리뷰에 있던 항목별 평점(청결도, 서비스 등등)을 리뷰ID로 삭제
        //ex) 사용자가 청결도 리뷰를 삭제하고 위치 리뷰를 추가하는 식으로 리뷰를 수정할 수 있음 그러면 기존 리뷰에 딸린 항목별 리뷰를 삭제하고 새로 넣는게 좋다고 봄.
        reviewCategoryRepository.deleteByReviewSid(reviewUpdateRequest.getSid());
        //마찬가지로 기존 리뷰에 딸린 장단점 항목을 삭제하고 새로 넣음
        reviewTagRepository.deleteByReviewSid(reviewUpdateRequest.getSid());

        List<ReviewCategory> categories = this.insertReviewCategories(reviewUpdateRequest.getCategories(), updateAfter);
        List<ReviewTag> tags = this.insertReviewTags(reviewUpdateRequest.getTags(), updateAfter);

        ReviewResponse result = this.toReviewResponse(updateAfter,categories, tags);

        return result;
    }

    public ReviewResponse findByReviewId(Long reviewId) {
        Review review = reviewRepository.findBySidAndDeletedFalse(reviewId);

        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(reviewId);
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(reviewId);

        ReviewResponse result = this.toReviewResponse(review,categories,tags);

        return result;
    }

    public Page<ReviewResponse> search(Long reviewId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findBySidAndDeletedFalse(reviewId,pageable);

        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(reviewId);
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(reviewId);

        List<ReviewResponse> list = reviews.stream()
                .map(review ->
                        this.toReviewResponse(review,categories,tags)
                )
                .toList();
        Page<ReviewResponse> responses = new PageImpl<>(list, pageable, reviews.getTotalElements());
        return responses;
    }

    public ReviewResponse deleteReviewId(Long reviewId) {
        Review find = reviewRepository.findBySidAndDeletedFalse(reviewId);
        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(reviewId);
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(reviewId);

        find.markDeleted();

        Review save = reviewRepository.save(find);

        ReviewResponse result = toReviewResponse(save,categories,tags);

        List<ReviewPhoto> photos = reviewPhotoRepository.findByReviewSidAndDeletedFalse(reviewId);
        if(photos != null && !photos.isEmpty()){
            for(ReviewPhoto photo : photos) {
                photo.prePersist();
                reviewPhotoRepository.save(photo);
            }
        }

        return result;
    }

    private ReviewResponse toReviewResponse(Review reviewResult, List<ReviewCategory> categories, List<ReviewTag> tags) {
        return  ReviewResponse.builder()
                .sid(reviewResult.getSid())
                .hotelId(reviewResult.getHotel().getSid())
                .memberId(reviewResult.getMember().getSid())
                .reservationId(reviewResult.getReservation().getSid())
                .rating(reviewResult.getRating())
                .travelType(reviewResult.getTravelType())
                .content(reviewResult.getContent())
                .likeCount(reviewResult.getLikeCount())
                .dislikeCount(reviewResult.getDislikeCount())
                .categories(
                        categories.stream()
                                .map(reviewCategory -> ReviewCategoryResponse.builder()
                                        .sid(reviewCategory.getSid())
                                        .reviewCategoryId(reviewCategory.getReviewCategoryMaster().getSid())
                                        .reviewId(reviewCategory.getReview().getSid())
                                        .rating(reviewCategory.getRating())
                                        .build())
                                .toList()
                )
                .tags(
                        tags.stream()
                                .map(reviewTag -> ReviewTagResponse.builder()
                                        .reviewId(reviewTag.getReview().getSid())
                                        .reviewTagId(reviewTag.getReviewTagMaster().getSid())
                                        .build()
                                )
                                .toList()
                )
                .createdAt(reviewResult.getCreatedAt())
                .updatedAt(reviewResult.getUpdatedAt())
                .deletedAt(reviewResult.getDeletedAt())
                .deleted(reviewResult.getDeleted())
                .build();
    }

    private List<ReviewCategory> insertReviewCategories(List<ReviewCategoryRequest> categories, Review review) {
        List<ReviewCategory> results = new ArrayList<>();
        if(categories != null && !categories.isEmpty()){

            for(ReviewCategoryRequest reviewCategoryRequest : categories){
                ReviewCategoryMaster reviewCategoryMaster = reviewCategoryMasterRepository
                        .findById(reviewCategoryRequest.getCategoryId())
                        .orElseThrow();

                ReviewCategory reviewCategory = ReviewCategory.builder()
                        .review(review)
                        .reviewCategoryMaster(reviewCategoryMaster)
                        .rating(reviewCategoryRequest.getRating())
                        .build();

                ReviewCategory categoryResult = reviewCategoryRepository.save(reviewCategory);

                results.add(categoryResult);
            }
        }

        return results;
    }

    private List<ReviewTag> insertReviewTags(List<ReviewTagRequest> tags , Review review) {
        List<ReviewTag> results = new ArrayList<>();
        if(tags != null && !tags.isEmpty()) {
            for(ReviewTagRequest reviewTagRequest : tags) {
                ReviewTagMaster reviewTagMaster = reviewTagMasterRepository.findById(reviewTagRequest.getTagId()).orElseThrow();

                ReviewTag reviewTag = ReviewTag.builder()
                        .review(review)
                        .reviewTagMaster(reviewTagMaster)
                        .build();

                ReviewTag tagResult = reviewTagRepository.save(reviewTag);

                results.add(tagResult);
            }
        }
        return results;
    }


}
