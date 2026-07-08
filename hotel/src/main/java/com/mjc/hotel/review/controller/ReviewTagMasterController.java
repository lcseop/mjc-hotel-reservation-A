package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.ReviewTagMaster;
import com.mjc.hotel.review.repository.ReviewTagMasterRepository;
import com.mjc.hotel.util.ApiResponse;
import com.mjc.hotel.util.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
