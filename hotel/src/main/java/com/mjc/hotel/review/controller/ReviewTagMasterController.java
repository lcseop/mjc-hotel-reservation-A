package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.request.ReviewTagMasterRequest;
import com.mjc.hotel.review.response.ReviewTagMasterResponse;
import com.mjc.hotel.review.service.ReviewTagMasterService;
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

    private final ReviewTagMasterService reviewTagMasterService;

    @Operation(summary = "리뷰 태그 마스터 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewTagMasterResponse>>> findAll() {
        List<ReviewTagMasterResponse> results = reviewTagMasterService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master search ok", results)
        );
    }

    @Operation(summary = "리뷰 태그 마스터 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewTagMasterResponse>> create(@RequestBody ReviewTagMasterRequest request) {
        ReviewTagMasterResponse saved = reviewTagMasterService.insert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master insert ok", saved)
        );
    }

    @Operation(summary = "리뷰 태그 마스터 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<ReviewTagMasterResponse>> update(@RequestBody ReviewTagMasterRequest request) {
        ReviewTagMasterResponse tag = reviewTagMasterService.update(request);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master update ok", tag)
        );
    }

    @Operation(summary = "리뷰 태그 마스터 삭제")
    @DeleteMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReviewTagMasterResponse>> delete(@PathVariable Long sid) {
        ReviewTagMasterResponse response = reviewTagMasterService.deleteById(sid);
        return ResponseEntity.ok(
                new ApiResponse<>(ResponseCode.SUCCESS, "review tag master delete ok", response)
        );
    }
}
