package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewAnswer;
import com.mjc.hotel.review.repository.ReviewAnswerRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewAnswerCreateRequest;
import com.mjc.hotel.review.request.ReviewAnswerUpdateRequest;
import com.mjc.hotel.review.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewAnswerService {
    private final ReviewAnswerRepository reviewAnswerRepository;
    private final ReviewRepository reviewRepository;

    public ReviewAnswerResponse insertReviewAnswer(ReviewAnswerCreateRequest reviewAnswerRequest) {
        Review review = reviewRepository.findById(reviewAnswerRequest.getReviewId()).orElseThrow();

        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .review(review)
                .reviewAnswer(reviewAnswerRequest.getReviewAnswer())
                .build();

        reviewAnswer.prePersist();

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }

    public ReviewAnswerResponse updateReviewAnswer(ReviewAnswerUpdateRequest reviewAnswerRequest) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(reviewAnswerRequest.getSid());
        if(find == null) {
            return null;
        }

        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .sid(find.getSid())
                .review(find.getReview())
                .reviewAnswer(reviewAnswerRequest.getReviewAnswer())
                .build();

        //생성시간 그대로 넘겨주기
        reviewAnswer.setCreatedAt(find.getCreatedAt());
        reviewAnswer.prePersist();

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }

    public ReviewAnswerResponse findReviewAnswer(Long id) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(id);
        if(find == null) {
            return null;
        }

        ReviewAnswerResponse result = this.toReviewAnswerResponse(find);

        return result;
    }

    public ReviewAnswerResponse deleteReviewAnswer(Long id) {
        ReviewAnswer find = reviewAnswerRepository.findBySidAndDeletedFalse(id);
        if(find == null) {
            return null;
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
}
