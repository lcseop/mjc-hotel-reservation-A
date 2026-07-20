package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.ReviewTagMaster;
import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import com.mjc.hotel.review.repository.ReviewTagMasterRepository;
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
@RequestMapping("api/review-tag-master")
@RequiredArgsConstructor
@Tag(name = "리뷰 태그 마스터", description = "리뷰 작성용 장단점 태그 마스터 조회 API")
public class ReviewTagMasterController {

    private final ReviewTagMasterRepository reviewTagMasterRepository;

    @Operation(summary = "리뷰 태그 마스터 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewTagMaster>>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master search ok", reviewTagMasterRepository.findAll())
        );
    }

    @Operation(summary = "리뷰 태그 마스터 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewTagMaster>> create(@RequestBody ReviewTagMaster request) {
        ReviewTagMaster saved = reviewTagMasterRepository.save(ReviewTagMaster.builder()
                .reviewTagName(request.getReviewTagName())
                .reviewTagCategory(resolveCategory(request.getReviewTagCategory()))
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master insert ok", saved)
        );
    }

    @Operation(summary = "리뷰 태그 마스터 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewTagMaster>> update(@RequestBody ReviewTagMaster request) {
        ReviewTagMaster tag = reviewTagMasterRepository.findById(request.getSid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰 태그입니다. sid=" + request.getSid()));
        tag.setReviewTagName(request.getReviewTagName());
        tag.setReviewTagCategory(resolveCategory(request.getReviewTagCategory()));
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master update ok", reviewTagMasterRepository.save(tag))
        );
    }

    @Operation(summary = "리뷰 태그 마스터 삭제")
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long sid) {
        reviewTagMasterRepository.deleteById(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master delete ok", null)
        );
    }

    private ReviewTagCategory resolveCategory(ReviewTagCategory category) {
        return category != null ? category : ReviewTagCategory.PROS;
    }
}
