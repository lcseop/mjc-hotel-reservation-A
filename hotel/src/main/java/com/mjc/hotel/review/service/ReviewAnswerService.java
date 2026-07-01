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

import java.time.LocalDateTime;

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

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }

    public ReviewAnswerResponse updateReviewAnswer(ReviewAnswerUpdateRequest reviewAnswerRequest) {
        ReviewAnswer find = reviewAnswerRepository.findById(reviewAnswerRequest.getSid()).orElseThrow();

        if(Boolean.TRUE.equals(find.getDeleted())) return null;

        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .sid(find.getSid())
                .review(find.getReview())
                .reviewAnswer(reviewAnswerRequest.getReviewAnswer())
                .build();

        ReviewAnswer save = reviewAnswerRepository.save(reviewAnswer);

        ReviewAnswerResponse result = this.toReviewAnswerResponse(save);
        return result;
    }

    public ReviewAnswerResponse findReviewAnswer(Long id) {
        ReviewAnswer find = reviewAnswerRepository.findById(id).orElseThrow();

        if(Boolean.TRUE.equals(find.getDeleted())) return null;

        ReviewAnswerResponse result = this.toReviewAnswerResponse(find);

        return result;
    }

    public ReviewAnswerResponse deleteReviewAnswerId(Long id) {
        ReviewAnswer find = reviewAnswerRepository.findById(id).orElseThrow();

        find.setDeletedAt(LocalDateTime.now());
        find.setDeleted(true);

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
