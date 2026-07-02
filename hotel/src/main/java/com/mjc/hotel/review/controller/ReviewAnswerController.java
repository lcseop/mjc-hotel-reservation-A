package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewAnswerCreateRequest;
import com.mjc.hotel.review.request.ReviewAnswerUpdateRequest;
import com.mjc.hotel.review.response.ReviewAnswerResponse;
import com.mjc.hotel.review.service.ReviewAnswerService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/review-answer")
@RequiredArgsConstructor
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
        if(response == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(ResponseCode.UPDATE_ERROR,"review_answer is deleted so don't update", null)
            );
        }
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
        if(response == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(ResponseCode.SELECT_ERROR,"review_answer not found", null)
            );
        }
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
        if(response == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(ResponseCode.DELETE_ERROR,"review_answer is deleted so don't delete", null)
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review_answer delete ok",response)
        );
    }
}
