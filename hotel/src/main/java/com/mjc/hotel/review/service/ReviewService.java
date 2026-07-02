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
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ReviewAnswerRepository reviewAnswerRepository;

    private final HotelRepository hotelRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public ReviewResponse insertReview(ReviewCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId()).orElseThrow();
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow();
        Reservation reservation = reservationRepository.findById(request.getReservationId()).orElseThrow();

        //request로 빌더 패턴 사용
        Review review = Review.builder()
                .hotel(hotel)
                .member(member)
                .reservation(reservation)
                .rating(request.getRating())
                .travelType(request.getTravelType())
                .content(request.getContent())
                .likeCount(0)
                .dislikeCount(0)
                .build();

        review.prePersist();

        Review reviewResult = reviewRepository.save(review);
        List<ReviewCategory> categories = this.insertReviewCategories(request.getCategories(), reviewResult);
        List<ReviewTag> tags = this.insertReviewTags(request.getTags(), reviewResult);

        ReviewResponse result = this.toReviewResponse(reviewResult,categories, tags);

        return result;
    }

    @Transactional
    public ReviewResponse updateReview(ReviewUpdateRequest request) {
        Review find = reviewRepository.findBySidAndDeletedFalse(request.getSid());
        if(find == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        Review review = Review.builder()
                .sid(find.getSid())
                .hotel(find.getHotel())
                .member(find.getMember())
                .reservation(find.getReservation())
                .rating(request.getRating())
                .travelType(request.getTravelType())
                .content(request.getContent())
                .likeCount(find.getLikeCount())
                .dislikeCount(find.getDislikeCount())
                .build();
        //생성시간 그대로 넘겨주기
        review.setCreatedAt(find.getCreatedAt());
        review.prePersist();

        Review updateAfter = reviewRepository.save(review);

        //기존 리뷰에 있던 항목별 평점(청결도, 서비스 등등)을 리뷰ID로 삭제
        //ex) 사용자가 청결도 리뷰를 삭제하고 위치 리뷰를 추가하는 식으로 리뷰를 수정할 수 있음 그러면 기존 리뷰에 딸린 항목별 리뷰를 삭제하고 새로 넣는게 좋다고 봄.
        reviewCategoryRepository.deleteByReviewSid(updateAfter.getSid());
        //마찬가지로 기존 리뷰에 딸린 장단점 항목을 삭제하고 새로 넣음
        reviewTagRepository.deleteByReviewSid(updateAfter.getSid());

        List<ReviewCategory> categories = this.insertReviewCategories(request.getCategories(), updateAfter);
        List<ReviewTag> tags = this.insertReviewTags(request.getTags(), updateAfter);

        ReviewResponse result = this.toReviewResponse(updateAfter,categories, tags);

        return result;
    }

    public ReviewResponse findByReviewId(Long reviewId) {
        Review review = reviewRepository.findBySidAndDeletedFalse(reviewId);
        if(review == null){
            throw new  DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(review.getSid());
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(review.getSid());

        ReviewResponse result = this.toReviewResponse(review,categories,tags);

        return result;
    }

    public ReviewResponse deleteReviewId(Long sid) {
        Review find = reviewRepository.findBySidAndDeletedFalse(sid);
        if(find == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }

        find.markDeleted();
        Review save = reviewRepository.save(find);

        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(find.getSid());
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(find.getSid());

        List<ReviewPhoto> photos = reviewPhotoRepository.findAllByReviewSidAndDeletedFalse(find.getSid());
        if(photos != null && !photos.isEmpty()){
            for(ReviewPhoto photo : photos) {
                photo.markDeleted();
                reviewPhotoRepository.save(photo);
            }
        }
        ReviewAnswer answer = reviewAnswerRepository.findByReviewSidAndDeletedFalse(find.getSid());
        if(answer != null){
            answer.markDeleted();
            reviewAnswerRepository.save(answer);
        }

        ReviewResponse result = toReviewResponse(save,categories,tags);
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
