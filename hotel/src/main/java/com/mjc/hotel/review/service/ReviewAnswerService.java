package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewAnswer;
import com.mjc.hotel.review.repository.ReviewAnswerRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewAnswerCreateRequest;
import com.mjc.hotel.review.request.ReviewAnswerUpdateRequest;
import com.mjc.hotel.review.response.*;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewAnswerService {
    private final ReviewAnswerRepository reviewAnswerRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewAnswerResponse insertReviewAnswer(ReviewAnswerCreateRequest request) {
        Review review = reviewRepository.findBySidAndDeletedFalse(request.getReviewId());
        if(review == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        if(reviewAnswerRepository.existsByReviewSidAndDeletedFalse(review.getSid())) {
            throw new IllegalArgumentException("Review Answer Is Already Exists");
        }
        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .review(review)
                .reviewAnswer(request.getReviewAnswer())
                .build();

        reviewAnswer.prePersist();

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }
    @Transactional
    public ReviewAnswerResponse updateReviewAnswer(ReviewAnswerUpdateRequest request) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(request.getSid());
        if(find == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Answer Not Found");
        }

        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .sid(find.getSid())
                .review(find.getReview())
                .reviewAnswer(request.getReviewAnswer())
                .build();

        //생성시간 그대로 넘겨주기
        reviewAnswer.setCreatedAt(find.getCreatedAt());
        reviewAnswer.prePersist();

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }
    @Transactional
    public ReviewAnswerResponse findBySidReviewAnswer(Long sid) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(sid);
        if(find == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Answer Not Found");
        }

        ReviewAnswerResponse result = this.toReviewAnswerResponse(find);

        return result;
    }
    @Transactional
    public ReviewAnswerResponse deleteReviewAnswer(Long sid) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(sid);
        if(find == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Answer Not Found");
        }
        find.markDeleted();

        ReviewAnswer save = reviewAnswerRepository.save(find);

        ReviewAnswerResponse result = toReviewAnswerResponse(save);

        return result;
    }

    private ReviewAnswerResponse toReviewAnswerResponse(ReviewAnswer reviewAnswer) {
        return ReviewAnswerResponse.builder()
                .sid(reviewAnswer.getSid())
                .reviewId(reviewAnswer.getReview().getSid())
                .reviewAnswer(reviewAnswer.getReviewAnswer())
                .createdAt(reviewAnswer.getCreatedAt())
                .updatedAt(reviewAnswer.getUpdatedAt())
                .deletedAt(reviewAnswer.getDeletedAt())
                .deleted(reviewAnswer.getDeleted())
                .build();
    }
    @Transactional
    public ReviewAnswerResponse findByReviewSid(Long reviewId){
        ReviewAnswer find = reviewAnswerRepository.findByReviewSidAndDeletedFalse(reviewId);
        if(find == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        ReviewAnswerResponse result = this.toReviewAnswerResponse(find);
        return result;
    }
}
