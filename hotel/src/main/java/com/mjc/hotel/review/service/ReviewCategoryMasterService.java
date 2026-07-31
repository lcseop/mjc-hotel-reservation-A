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
    private static final String ERROR_MESSAGE = "Review Category Master Not Found";
    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    
    public ReviewCategoryMasterResponse insert(ReviewCategoryMasterRequest request){
        ReviewCategoryMaster newReviewCategoryMaster = ReviewCategoryMaster.builder()
                .reviewCategoryName(request.getReviewCategoryName())
                .build();
        
        ReviewCategoryMaster save = reviewCategoryMasterRepository.save(newReviewCategoryMaster);

        return this.toReviewCategoryMasterResponse(save);
    }

    public ReviewCategoryMasterResponse update(ReviewCategoryMasterRequest request){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(request.getSid())
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,ERROR_MESSAGE));
        
        ReviewCategoryMaster update = ReviewCategoryMaster.builder()
                .sid(find.getSid())
                .reviewCategoryName(request.getReviewCategoryName())
                .build();
        
        ReviewCategoryMaster save = reviewCategoryMasterRepository.save(update);

        return this.toReviewCategoryMasterResponse(save);
    }

    public ReviewCategoryMasterResponse findById(Long sid){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(sid)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,ERROR_MESSAGE));

        return this.toReviewCategoryMasterResponse(find);
    }

    public ReviewCategoryMasterResponse deleteById(Long sid){
        ReviewCategoryMaster find = reviewCategoryMasterRepository.findById(sid)
                .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,ERROR_MESSAGE));

        reviewCategoryMasterRepository.deleteById(find.getSid());

        return this.toReviewCategoryMasterResponse(find);
    }

    public List<ReviewCategoryMasterResponse> findAll(){
        List<ReviewCategoryMaster> reviewCategoryMasters = reviewCategoryMasterRepository.findAll();

        return reviewCategoryMasters.stream()
                .map(this::toReviewCategoryMasterResponse)
                .toList();
    }

    private ReviewCategoryMasterResponse toReviewCategoryMasterResponse(ReviewCategoryMaster save) {
        return ReviewCategoryMasterResponse.builder()
                .sid(save.getSid())
                .reviewCategoryName(save.getReviewCategoryName())
                .build();
    }
}
