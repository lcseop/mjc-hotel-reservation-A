package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewReactionRequest;
import com.mjc.hotel.review.response.ReviewReactionResponse;
import com.mjc.hotel.review.service.ReviewReactionService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/review-reaction")
@RequiredArgsConstructor
@Tag(name = "리뷰 좋아요 싫어요", description = "리뷰에 대한 좋아요 싫어요를 저장, 수정, 리뷰의 전체 좋아요/싫어요 수를 조회하는 API")
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
            description = "특정 리뷰에 대해 모든 좋아요 수 혹은 싫어요 수를 찾습니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Long>> search(@RequestParam Long reviewId, @RequestParam String reactionType){
        Long size = reviewReactionService.findAllByReviewIdAndReactionType(reviewId,reactionType);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_reaction search ok",size)
        );
    }
}
