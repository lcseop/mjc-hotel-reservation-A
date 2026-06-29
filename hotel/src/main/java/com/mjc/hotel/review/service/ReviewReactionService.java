package com.mjc.hotel.review.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewReaction;
import com.mjc.hotel.review.repository.ReviewReactionRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewReactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewReactionService {
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    private final ReviewReactionRepository reviewReactionRepository;

    public ReviewReaction addReviewReaction(ReviewReactionRequest reviewReactionRequest) {
        Review review = reviewRepository.findById(reviewReactionRequest.getReviewId()).orElseThrow();
        Member member = memberRepository.findById(reviewReactionRequest.getMemberId()).orElseThrow();

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(member)
                .reactionType(reviewReactionRequest.getReactionType())
                .build();

        ReviewReaction result = reviewReactionRepository.save(reviewReaction);
        return result;
    }
}
