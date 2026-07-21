package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.ReviewCategoryMaster;
import com.mjc.hotel.review.repository.ReviewCategoryMasterRepository;
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

    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;

    @Operation(summary = "리뷰 카테고리 마스터 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewCategoryMaster>>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master search ok", reviewCategoryMasterRepository.findAll())
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewCategoryMaster>> create(@RequestBody ReviewCategoryMaster request) {
        ReviewCategoryMaster saved = reviewCategoryMasterRepository.save(ReviewCategoryMaster.builder()
                .reviewCategoryName(request.getReviewCategoryName())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master insert ok", saved)
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewCategoryMaster>> update(@RequestBody ReviewCategoryMaster request) {
        ReviewCategoryMaster category = reviewCategoryMasterRepository.findById(request.getSid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰 카테고리입니다. sid=" + request.getSid()));
        category.setReviewCategoryName(request.getReviewCategoryName());
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master update ok", reviewCategoryMasterRepository.save(category))
        );
    }

    @Operation(summary = "리뷰 카테고리 마스터 삭제")
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        reviewCategoryMasterRepository.deleteById(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review category master delete ok", null)
        );
    }
}
