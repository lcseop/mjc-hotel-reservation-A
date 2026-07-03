package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.Review;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper;

    private final ReviewService reviewService;

    @GetMapping("/mapperGetReviews")
    public List<Review> getReviews() {
        return reviewMapper.getReviews();
    }

    @GetMapping("/repositoryGetReviews")
    public List<Review> getReviewsR() {
        return reviewRepository.findAll();
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
            summary = "리뷰 수정",
            description = "리뷰 수정합니다."
    )
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> update(@RequestBody ReviewUpdateRequest request){
        ReviewResponse response = reviewService.updateReview(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review update ok",response)
        );
    }

    @Operation(
            summary = "리뷰 검색",
            description = "리뷰를 검색합니다."
    )
    @GetMapping("/find")
    public ResponseEntity<ApiResponse<ReviewResponse>> find(@RequestParam Long sid){
        ReviewResponse response = reviewService.findByReviewId(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review search ok", response)
        );
    }
    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰를 삭제합니다."
    )
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReviewResponse>> delete(@PathVariable Long sid){
        ReviewResponse response = reviewService.deleteReviewId(sid);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS,"review delete ok",response)
        );
    }


    @Operation(
            summary = "호텔 리뷰 정렬 조회",
            description = "호텔의 모든 리뷰를 정렬 조건에 맞춰 조회합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> search(
            @RequestParam Long hotelId
            , @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ReviewResponse> responses = reviewService.reviewsInHotel(hotelId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review sort_search ok", responses)
        );
    }

    @Operation(
            summary = "호텔 긍정 리뷰 정렬 조회",
            description = "호텔의 모든 긍정 리뷰를 정렬 조건에 맞춰 조회합니다."
    )
    @GetMapping("/positive-search")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> positiveSearch(
            @RequestParam Long hotelId
            ,@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<ReviewResponse> responses = reviewService.positiveReviewsInHotel(hotelId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review positive_search ok", responses)
        );
    }

    @Operation(
            summary = "호텔 사진 포함 리뷰 정렬 조회",
            description = "호텔의 모든 리뷰중 사진을 포함하고 있는 리뷰를 정렬 조건에 맞춰 조회합니다."
    )
    @GetMapping("/exists-photo-search")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> existsPhotoSearch(
            @RequestParam Long hotelId
            ,@PageableDefault(size = 5 ,sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> responses = reviewService.existsPhotoReviewsInHotel(hotelId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review exist_photo_search ok", responses)
        );
    }
}
