package com.mjc.hotel.review.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewReaction;
import com.mjc.hotel.review.entity.composite_key.ReviewReactionId;
import com.mjc.hotel.review.entity.enums.ReactionType;
import com.mjc.hotel.review.repository.ReviewReactionRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewReactionRequest;
import com.mjc.hotel.review.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReactionService {
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    private final ReviewReactionRepository reviewReactionRepository;

    public ReviewReactionResponse addReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        Review review = reviewRepository.findById(reviewReactionRequest.getReviewId()).orElseThrow();
        Member member = memberRepository.findById(reviewReactionRequest.getMemberId()).orElseThrow();

        if(reviewReactionRequest.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
            reviewRepository.save(review);
        }
        else if(reviewReactionRequest.getReactionType() == ReactionType.BAD){
            review.increaseDislike();
            reviewRepository.save(review);
        }

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(member)
                .reactionType(reviewReactionRequest.getReactionType())
                .build();

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(save.getReview().getSid())
                .memberId(save.getMember().getSid())
                .reactionType(save.getReactionType())
                .createdAt(save.getCreatedAt())
                .updatedAt(save.getUpdatedAt())
                .build();
        return result;
    }

    public ReviewReactionResponse updateReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(reviewReactionRequest.getReviewId(), reviewReactionRequest.getMemberId());

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId).orElseThrow();

        Review review = reviewRepository.findById(find.getReview().getSid()).orElseThrow();
        //좋아요에서 취소상태 (삭제)
        if(find.getReactionType() == ReactionType.GOOD && reviewReactionRequest.getReactionType() == ReactionType.NONE){
            review.decreaseLike();
        }
        //싫어요에서 취소상태 (삭제)
        if(find.getReactionType() == ReactionType.BAD && reviewReactionRequest.getReactionType() == ReactionType.NONE){
            review.decreaseDislike();
        }
        //좋아요에서 싫어요
        if(find.getReactionType() == ReactionType.GOOD && reviewReactionRequest.getReactionType() == ReactionType.BAD){
            review.decreaseLike();
            review.increaseDislike();
        }
        //싫어요에서 좋아요
        if(find.getReactionType() == ReactionType.BAD && reviewReactionRequest.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
            review.decreaseDislike();
        }
        //취소상태에서 좋아요
        if(find.getReactionType() == ReactionType.NONE && reviewReactionRequest.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
        }
        //취소상태에서 싫어요
        if(find.getReactionType() == ReactionType.NONE && reviewReactionRequest.getReactionType() == ReactionType.BAD){
            review.increaseDislike();
        }
        reviewRepository.save(review);

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(find.getMember())
                .reactionType(reviewReactionRequest.getReactionType())
                .build();

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(save.getReview().getSid())
                .memberId(save.getMember().getSid())
                .reactionType(save.getReactionType())
                .createdAt(save.getCreatedAt())
                .updatedAt(save.getUpdatedAt())
                .build();
        return result;
    }

    public ReviewReactionResponse findReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(reviewReactionRequest.getReviewId(), reviewReactionRequest.getMemberId());

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId).orElseThrow();

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(find.getReview().getSid())
                .memberId(find.getMember().getSid())
                .reactionType(find.getReactionType())
                .createdAt(find.getCreatedAt())
                .updatedAt(find.getUpdatedAt())
                .build();
        return result;
    }

    public int findByReviewReviewIdAndReactionType(Review review, ReactionType reactionType) {
        List<ReviewReaction> reviewReactions = reviewReactionRepository.findByReviewSidAndReactionType(review.getSid(),reactionType);
        return reviewReactions.size();
    }
}
