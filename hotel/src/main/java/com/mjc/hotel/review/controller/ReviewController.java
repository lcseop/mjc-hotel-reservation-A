package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.mapper.ReviewMapper;
import com.mjc.hotel.review.repository.*;
import com.mjc.hotel.review.request.ReviewCreateRequest;
import com.mjc.hotel.review.request.ReviewUpdateRequest;
import com.mjc.hotel.review.request.ReviewWriteStatusRequest;
import com.mjc.hotel.review.response.ReviewCategoryResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.response.ReviewTagResponse;
import com.mjc.hotel.review.response.ReviewWriteStatusResponse;
import com.mjc.hotel.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "리뷰", description = "리뷰를 저장, 수정, 검색, 삭제, 정렬(조회, 긍정 조회, 사진 포함 조회)하는 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 생성",
            description = "리뷰를 만듭니다."
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
            description = "리뷰를 수정합니다."
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

    @Operation(
            summary = "리뷰 항목별 평점 리뷰 ID 조회",
            description = "리뷰에 딸린 항목별 평점(청결도, 서비스, 위치...)를 리뷰 ID로 조회합니다."
    )
    @GetMapping("review-category-search")
    public ResponseEntity<ApiResponse<List<ReviewCategoryResponse>>> reviewCategorySearch(@RequestParam Long reviewId){
        List<ReviewCategoryResponse> responses = reviewService.findReviewCategoriesByReviewSid(reviewId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category search ok", responses)
        );
    }

    @Operation(
            summary = "리뷰 태그 리뷰 ID 조회",
            description = "리뷰에 딸린 태그(청결함, 친절한 직원, 주차 불편, 체크인 대기...)를 리뷰 ID로 조회합니다."
    )
    @GetMapping("review-tag-search")
    public ResponseEntity<ApiResponse<List<ReviewTagResponse>>> reviewTagSearch(@RequestParam Long reviewId){
        List<ReviewTagResponse> responses = reviewService.findReviewTagsByReviewSid(reviewId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag search ok", responses)
        );
    }

//    @Operation(
//            summary = "회원의 리뷰 쓰기 상태 판단",
//            description = "회원이 예약으로 숙박 완료 상태인지 판단합니다. 또한 완료 상태라면 리뷰를 새로 써야하는지, 혹은 수정하는지 판단 합니다."
//    )
//    @GetMapping("review-status-opinion")
//    public ResponseEntity<ApiResponse<ReviewWriteStatusResponse>> opinionReviewWriteStatus(
//            @RequestBody ReviewWriteStatusRequest request
//            ,@PageableDefault(page = 0, size = 5) Pageable pageable){
//        ReviewWriteStatusResponse response = reviewService.opinionReviewWriteStatus(request,pageable);
//        return ResponseEntity.status(HttpStatus.OK).body(
//                new ApiResponse<>(ResponseCode.SUCCESS, "opinion review write status ok", response)
//        );
//    }
}
