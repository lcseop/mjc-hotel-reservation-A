package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.ReviewCategoryMaster;
import com.mjc.hotel.review.repository.ReviewCategoryMasterRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mjc.hotel.review.request.ReviewCategoryMasterRequest;
import com.mjc.hotel.review.response.ReviewCategoryMasterResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCategoryMasterService {
    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    
    public ReviewCategoryMasterResponse insert(ReviewCategoryMasterRequest request){
        ReviewCategoryMaster newReviewCategoryMaster = ReviewCategoryMaster.builder()
                .reviewCategoryName(request.getReviewCategoryName())
                .build();
        
        ReviewCategoryMaster save = reviewCategoryMasterRepository.save(newReviewCategoryMaster);

        ReviewCategoryMasterResponse result = this.toReviewCategoryMasterResponse(save);
        return result;
    }

    public ReviewCategoryMasterResponse update(ReviewCategoryMasterRequest request){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(request.getSid())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Category Master Not Found"));
        
        ReviewCategoryMaster update = ReviewCategoryMaster.builder()
                .sid(find.getSid())
                .reviewCategoryName(request.getReviewCategoryName())
                .build();
        
        ReviewCategoryMaster save = reviewCategoryMasterRepository.save(update);

        ReviewCategoryMasterResponse result = this.toReviewCategoryMasterResponse(save);
        return result;
    }

    public ReviewCategoryMasterResponse findById(Long sid){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(sid)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Category Master Not Found"));

        ReviewCategoryMasterResponse result = this.toReviewCategoryMasterResponse(find);
        return result;
    }

    public ReviewCategoryMasterResponse deleteById(Long sid){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(sid)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Category Master Not Found"));

        reviewCategoryMasterRepository.deleteById(find.getSid());

        ReviewCategoryMasterResponse result = this.toReviewCategoryMasterResponse(find);
        return result;
    }

    public List<ReviewCategoryMasterResponse> findAll(){
        List<ReviewCategoryMaster> reviewCategoryMasters = reviewCategoryMasterRepository.findAll();

        List<ReviewCategoryMasterResponse> results = reviewCategoryMasters.stream()
                .map(this::toReviewCategoryMasterResponse)
                .toList();
        return results;
    }

    private ReviewCategoryMasterResponse toReviewCategoryMasterResponse(ReviewCategoryMaster save) {
        ReviewCategoryMasterResponse result = ReviewCategoryMasterResponse.builder()
                .sid(save.getSid())
                .reviewCategoryName(save.getReviewCategoryName())
                .build();
        return result;
    }
}
