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
import com.mjc.hotel.review.response.ReviewReactionResponse;
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

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(member)
                .reactionType(reviewReactionRequest.getReactionType())
                .build();

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(save.getReview().getReviewId())
                .memberId(save.getMember().getMemberId())
                .reactionType(save.getReactionType())
                .build();
        return result;
    }

    public ReviewReactionResponse updateReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(reviewReactionRequest.getReviewId(), reviewReactionRequest.getMemberId());

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId).orElseThrow();

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(find.getReview())
                .member(find.getMember())
                .reactionType(reviewReactionRequest.getReactionType())
                .build();

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(save.getReview().getReviewId())
                .memberId(save.getMember().getMemberId())
                .reactionType(save.getReactionType())
                .build();
        return result;
    }

    public ReviewReactionResponse findReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(reviewReactionRequest.getReviewId(), reviewReactionRequest.getMemberId());

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId).orElseThrow();

        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(find.getReview().getReviewId())
                .memberId(find.getMember().getMemberId())
                .reactionType(find.getReactionType())
                .build();
        return result;
    }

    public int findByReviewReviewIdAndReactionType(Review review, ReactionType reactionType) {
        List<ReviewReaction> reviewReactions = reviewReactionRepository.findByReviewReviewIdAndReactionType(review.getReviewId(),reactionType);
        return reviewReactions.size();
    }
}
