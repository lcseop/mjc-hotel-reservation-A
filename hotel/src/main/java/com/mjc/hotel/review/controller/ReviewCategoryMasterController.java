package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewCategoryMasterCreateRequest;
import com.mjc.hotel.review.request.ReviewCategoryMasterUpdateRequest;
import com.mjc.hotel.review.response.ReviewCategoryMasterResponse;
import com.mjc.hotel.review.service.ReviewCategoryMasterService;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/review-category-master")
@RequiredArgsConstructor
@Tag(name = "리뷰 카테고리 마스터", description = "리뷰 항목별 평점 카테고리 마스터 관리 API")
public class ReviewCategoryMasterController {

    private final ReviewCategoryMasterService reviewCategoryMasterService;

    @Operation(summary = "리뷰 카테고리 마스터 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewCategoryMasterResponse>>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master search ok", reviewCategoryMasterService.findAll())
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewCategoryMasterResponse>> create(@RequestBody ReviewCategoryMasterCreateRequest request) {
        ReviewCategoryMasterResponse result = reviewCategoryMasterService.insert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master insert ok", result)
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewCategoryMasterResponse>> update(@RequestBody ReviewCategoryMasterUpdateRequest request) {
        ReviewCategoryMasterResponse result = reviewCategoryMasterService.update(request);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master update ok", result)
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 삭제")
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReviewCategoryMasterResponse>> delete(@PathVariable Long sid) {
        ReviewCategoryMasterResponse result = reviewCategoryMasterService.deleteById(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master delete ok", result)
        );
    }
}
