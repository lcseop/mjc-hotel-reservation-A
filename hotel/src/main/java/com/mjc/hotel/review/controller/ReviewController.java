package com.mjc.hotel.review.controller;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewCategory;
import com.mjc.hotel.review.mapper.ReviewMapper;
import com.mjc.hotel.review.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCategoryRepository reviewCategoryRepository;
    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewTagMasterRepository reviewTagMasterRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;


    private final ReviewMapper reviewMapper;


    @GetMapping("/mapperGetReviews")
    public List<Review> getReviews() {
        return reviewMapper.getReviews();
    }

    @GetMapping("/repositoryGetRevies")
    public List<ReviewCategory> getReviewsR() {
        return reviewCategoryRepository.findAll();
    }
}
