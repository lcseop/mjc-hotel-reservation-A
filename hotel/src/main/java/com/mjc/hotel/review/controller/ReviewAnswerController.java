package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewAnswerCreateRequest;
import com.mjc.hotel.review.request.ReviewAnswerUpdateRequest;
import com.mjc.hotel.review.response.ReviewAnswerResponse;
import com.mjc.hotel.review.service.ReviewAnswerService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/review-answer")
@RequiredArgsConstructor
@Tag(name = "리뷰 사진", description = "리뷰 답변 저장, 수정, 검색, 삭제, 리뷰 검색하는 API")
public class ReviewAnswerController {
    private final ReviewAnswerService reviewAnswerService;

    @Operation(
            summary = "리뷰 답변 생성",
            description = "리뷰 답변을 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewAnswerResponse>> insert(@RequestBody ReviewAnswerCreateRequest request){
        ReviewAnswerResponse response = reviewAnswerService.insertReviewAnswer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer insert ok",response)
        );
    }

    @Operation(
            summary = "리뷰 답변 수정",
            description = "리뷰 답변을 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewAnswerResponse>> update(@RequestBody ReviewAnswerUpdateRequest request){
        ReviewAnswerResponse response = reviewAnswerService.updateReviewAnswer(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer update ok",response)
        );
    }

    @Operation(
            summary = "리뷰 답변 검색",
            description = "리뷰 답변을 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ReviewAnswerResponse>> search(@RequestParam Long sid){
        ReviewAnswerResponse response = reviewAnswerService.findBySidReviewAnswer(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer search ok",response)
        );
    }

    @Operation(
            summary = "리뷰 답변 삭제",
            description = "리뷰 답변을 삭제합니다."
    )
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReviewAnswerResponse>> delete(@PathVariable Long sid){
        ReviewAnswerResponse response = reviewAnswerService.deleteReviewAnswer(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer delete ok",response)
        );
    }
    @Operation(
            summary = "리뷰 리뷰 답변 검색",
            description = "리뷰에 달린 리뷰 답변을 검색합니다."
    )
    @GetMapping("/review-search")
    public ResponseEntity<ApiResponse<ReviewAnswerResponse>> reviewSearch(@RequestParam Long reviewId){
        ReviewAnswerResponse response = reviewAnswerService.findByReviewSid(reviewId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer review_search ok",response)
        );

    }
}
