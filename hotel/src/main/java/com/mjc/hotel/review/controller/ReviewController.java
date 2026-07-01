package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewCategory;
import com.mjc.hotel.review.mapper.ReviewMapper;
import com.mjc.hotel.review.repository.*;
import com.mjc.hotel.review.request.ReviewCreateRequest;
import com.mjc.hotel.review.request.ReviewUpdateRequest;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewCategoryRepository reviewCategoryRepository;


    private final ReviewMapper reviewMapper;

    private final ReviewService reviewService;

    @GetMapping("/mapperGetReviews")
    public List<Review> getReviews() {
        return reviewMapper.getReviews();
    }

    @GetMapping("/repositoryGetRevies")
    public List<ReviewCategory> getReviewsR() {
        return reviewCategoryRepository.findAll();
    }

    @Operation(
            summary = "리뷰, 항목별 리뷰, 리뷰 태그 생성",
            description = "리뷰, 항목별 리뷰, 리뷰 태그를 만듭니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> insert(@RequestBody ReviewCreateRequest request){
        ReviewResponse response = reviewService.insertReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review insert ok",response)
        );
    }

    @Operation(
            summary = "리뷰, 항목별 리뷰, 리뷰 태그 수정",
            description = "리뷰, 항목별 리뷰, 리뷰 태그를 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> update(@RequestBody ReviewUpdateRequest request){
        ReviewResponse response = reviewService.updateReview(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review update ok",response)
        );
    }

    @Operation(
            summary = "리뷰, 항목별 리뷰, 리뷰 태그 검색",
            description = "리뷰, 항목별 리뷰, 리뷰 태그를 검색합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> search(@RequestParam Long sid,
                                                                    @PageableDefault(size = 5) Pageable pageable){
        Page<ReviewResponse> responses = reviewService.search(sid, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review search ok", responses)
        );
    }
    @Operation(
            summary = "리뷰, 항목별 리뷰, 리뷰 태그 삭제",
            description = "리뷰, 항목별 리뷰, 리뷰 태그를 삭제합니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> delete(@RequestParam Long sid){
        ReviewResponse response = reviewService.deleteReviewId(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review delete ok",response)
        );
    }
}
