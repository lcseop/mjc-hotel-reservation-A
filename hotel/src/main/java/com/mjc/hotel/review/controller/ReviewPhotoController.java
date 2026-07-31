package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewPhotoCreateRequest;
import com.mjc.hotel.review.request.ReviewPhotoUpdateRequest;
import com.mjc.hotel.review.response.ReviewPhotoResponse;
import com.mjc.hotel.review.service.ReviewPhotoService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/review-photo")
@RequiredArgsConstructor
@Tag(name = "리뷰 사진", description = "리뷰에 사진을 저장, 수정, 조회, 삭제하는 API")
public class ReviewPhotoController {

    private final ReviewPhotoService reviewPhotoService;
    @Operation(
            summary = "리뷰 사진 모두 추가",
            description = "리뷰 사진 모두를 추가합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ReviewPhotoResponse>>> insert(@ModelAttribute ReviewPhotoCreateRequest request) {
        List<ReviewPhotoResponse> responses = reviewPhotoService.insertReviewPhotos(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photos insert ok", responses)
        );
    }
    @Operation(
            summary = "리뷰 사진 수정",
            description = "리뷰 사진 수정시 원래 사진은 논리 삭제하고 수정된 사진으로 새로 수정합니다."
    )
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewPhotoResponse>> update(@ModelAttribute ReviewPhotoUpdateRequest request) {
        ReviewPhotoResponse response = reviewPhotoService.updateReviewPhoto(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photo update ok", response)
        );
    }
    @Operation(
            summary = "리뷰 사진 조건 검색",
            description = "리뷰 사진을 조건에 맞춰 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ReviewPhotoResponse>>> search(@RequestParam Long reviewId,
                                                                                    @PageableDefault(size = 3) Pageable pageable) {
        Page<ReviewPhotoResponse> responses = reviewPhotoService.search(reviewId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photos find ok", responses)
        );
    }
    @Operation(
            summary = "리뷰 사진 삭제",
            description = "리뷰 사진 삭제시 사진을 삭제합니다."
    )
    @DeleteMapping("{sid}")
    public ResponseEntity<ApiResponse<ReviewPhotoResponse>> delete(@PathVariable Long sid) {
        ReviewPhotoResponse response = reviewPhotoService.deleteReviewImage(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review photo delete ok", response)
        );
    }
}
