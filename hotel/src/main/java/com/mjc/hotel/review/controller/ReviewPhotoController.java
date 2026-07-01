package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewPhotoCreateRequest;
import com.mjc.hotel.review.request.ReviewPhotoUpdateRequest;
import com.mjc.hotel.review.response.ReviewPhotoResponse;
import com.mjc.hotel.review.service.ReviewPhotoService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/review-photo")
@RequiredArgsConstructor
public class ReviewPhotoController {

    private final ReviewPhotoService reviewPhotoService;
    @Operation(
            summary = "리뷰 사진 모두 추가",
            description = "리뷰 사진 모두를 추가합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<List<ReviewPhotoResponse>>> insert(@RequestBody ReviewPhotoCreateRequest request) {
        List<ReviewPhotoResponse> responses = reviewPhotoService.insertReviewPhotos(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photos insert ok", responses)
        );
    }
    @Operation(
            summary = "리뷰 사진 수정",
            description = "리뷰 사진 수정시 원래 사진은 논리 삭제하고 수정된 사진으로 새로 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewPhotoResponse>> update(@RequestBody ReviewPhotoUpdateRequest request) {
        ReviewPhotoResponse response = reviewPhotoService.updateReviewPhoto(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photo update ok", response)
        );
    }
    @Operation(
            summary = "리뷰 사진 모두 검색",
            description = "리뷰 사진을 모두 검색합니다."
    )
    @GetMapping("/findAllByReviewId")
    public ResponseEntity<ApiResponse<List<ReviewPhotoResponse>>> findAllByReviewId(@RequestParam Long reviewId) {
        List<ReviewPhotoResponse> responses = reviewPhotoService.findAllByReviewId(reviewId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photos find ok", responses)
        );
    }
    @Operation(
            summary = "리뷰 사진 삭제",
            description = "리뷰 사진 삭제시 사진을 논리 삭제합니다."
    )
    @DeleteMapping("{sid}")
    public ResponseEntity<ApiResponse<ReviewPhotoResponse>> delete(@PathVariable Long sid) {
        ReviewPhotoResponse response = reviewPhotoService.deleteReviewImage(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photo delete ok", response)
        );
    }
}
