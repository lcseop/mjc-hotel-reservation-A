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
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewReactionService {
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    private final ReviewReactionRepository reviewReactionRepository;

    @Transactional
    public ReviewReactionResponse addReviewReaction(ReviewReactionRequest request) {
        Review review = reviewRepository.findBySidAndDeletedFalse(request.getReviewId());
        if(review == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Member Not Found"));

        ReviewReactionId reviewReactionId = new ReviewReactionId(request.getReviewId(), request.getMemberId());
        ReviewReaction existing = reviewReactionRepository.findById(reviewReactionId).orElse(null);

        if (existing != null) {
            if (existing.getReactionType() == request.getReactionType()) {
                return this.toReviewReactionResponse(existing);
            }
            return this.updateReviewReaction(request);
        }

        if(request.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
            reviewRepository.save(review);
        }
        else if(request.getReactionType() == ReactionType.BAD){
            review.increaseDislike();
            reviewRepository.save(review);
        }

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(member)
                .reactionType(request.getReactionType())
                .build();

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = this.toReviewReactionResponse(save);
        return result;
    }
    @Transactional
    public ReviewReactionResponse updateReviewReaction(ReviewReactionRequest request) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(request.getReviewId(), request.getMemberId());

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"ReviewReaction Not Found"));

        Review review = reviewRepository.findBySidAndDeletedFalse(find.getReview().getSid());
        if(review == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        memberRepository.findById(find.getMember().getSid())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Member Not Found"));
        //좋아요에서 취소상태 (삭제)
        if(find.getReactionType() == ReactionType.GOOD && request.getReactionType() == ReactionType.NONE){
            review.decreaseLike();
        }
        //싫어요에서 취소상태 (삭제)
        if(find.getReactionType() == ReactionType.BAD && request.getReactionType() == ReactionType.NONE){
            review.decreaseDislike();
        }
        //좋아요에서 싫어요
        if(find.getReactionType() == ReactionType.GOOD && request.getReactionType() == ReactionType.BAD){
            review.decreaseLike();
            review.increaseDislike();
        }
        //싫어요에서 좋아요
        if(find.getReactionType() == ReactionType.BAD && request.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
            review.decreaseDislike();
        }
        //취소상태에서 좋아요
        if(find.getReactionType() == ReactionType.NONE && request.getReactionType() == ReactionType.GOOD){
            review.increaseLike();
        }
        //취소상태에서 싫어요
        if(find.getReactionType() == ReactionType.NONE && request.getReactionType() == ReactionType.BAD){
            review.increaseDislike();
        }
        reviewRepository.save(review);

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(find.getMember())
                .reactionType(request.getReactionType())
                .build();

        //생성일 그대로 넘겨주기
        reviewReaction.setCreatedAt(find.getCreatedAt());

        ReviewReaction save = reviewReactionRepository.save(reviewReaction);

        ReviewReactionResponse result = this.toReviewReactionResponse(save);
        return result;
    }
    @Transactional
    public ReviewReactionResponse findReviewReaction(Long reviewId, Long memberId) {
        ReviewReactionId reviewReactionId = new ReviewReactionId(reviewId, memberId);

        ReviewReaction find = reviewReactionRepository.findById(reviewReactionId)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"ReviewReaction Not Found"));

        ReviewReactionResponse result = this.toReviewReactionResponse(find);
        return result;
    }

    private ReviewReactionResponse toReviewReactionResponse(ReviewReaction find) {
        ReviewReactionResponse result = ReviewReactionResponse.builder()
                .reviewId(find.getReview().getSid())
                .memberId(find.getMember().getSid())
                .reactionType(find.getReactionType())
                .createdAt(find.getCreatedAt())
                .updatedAt(find.getUpdatedAt())
                .build();
        return result;
    }
    @Transactional
    public Long findAllByReviewIdAndReactionType(Long reviewId, String reactionTypeName) {
        Review find = reviewRepository.findBySidAndDeletedFalse(reviewId);
        if(find == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        ReactionType reactionType = reactionTypeName.equals("GOOD") ? ReactionType.GOOD : reactionTypeName.equals("BAD") ? ReactionType.BAD : ReactionType.NONE;
        List<ReviewReaction> reviewReactions = reviewReactionRepository.findAllByReviewSidAndReactionTypeEquals(find.getSid(),reactionType);
        return Long.valueOf(reviewReactions.size());
    }
}
