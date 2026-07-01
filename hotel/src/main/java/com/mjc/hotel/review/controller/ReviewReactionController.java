package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.enums.ReactionType;
import com.mjc.hotel.review.request.ReviewReactionRequest;
import com.mjc.hotel.review.response.ReviewReactionResponse;
import com.mjc.hotel.review.service.ReviewReactionService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewReactionController {
    private final ReviewReactionService reviewReactionService;

    @Operation(
            summary = "리뷰 좋아요 싫어요 생성",
            description = "특정 리뷰에 대한 특정 멤버의 좋아요 싫어요 여부를 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewReactionResponse>> insert(@RequestBody ReviewReactionRequest request){
        ReviewReactionResponse response = reviewReactionService.addReviewReaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_reaction insert ok",response)
        );
    }

    @Operation(
            summary = "리뷰 좋아요 싫어요 변경",
            description = "특정 리뷰에 대한 특정 멤버의 좋아요, 싫어요, 취소 여부를 새 상태로 변경합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewReactionResponse>> update(@RequestBody ReviewReactionRequest request){
        ReviewReactionResponse response = reviewReactionService.updateReviewReaction(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_reaction update ok",response)
        );
    }

    @Operation(
            summary = "리뷰의 모든 좋아요 싫어요 찾기",
            description = "특정 리뷰에 대해 모든 좋아요 싫어요 수를 찾습니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Long>> findAllByReviewIdAndReactionType(@RequestParam Long reviewId, @RequestParam String reactionTypeName){
        Long size = reviewReactionService.findAllByReviewIdAndReactionType(reviewId,reactionTypeName);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_reaction findAllByReviewIdAndReactionType ok",size)
        );
    }
}
