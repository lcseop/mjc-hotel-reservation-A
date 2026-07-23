package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.ReviewTagMaster;
import com.mjc.hotel.review.repository.ReviewTagMasterRepository;
import com.mjc.hotel.review.request.ReviewTagMasterRequest;
import com.mjc.hotel.review.response.ReviewTagMasterResponse;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewTagMasterService {
    public static final String ERROR_MESSAGE = "Review Tag Master Not Found";
    private final ReviewTagMasterRepository reviewTagMasterRepository;

    public ReviewTagMasterResponse insert(ReviewTagMasterRequest request){
        ReviewTagMaster reviewTagMaster = ReviewTagMaster.builder()
                .reviewTagName(request.getReviewTagName())
                .reviewTagCategory(request.getReviewTagCategory())

                .build();

        ReviewTagMaster save = reviewTagMasterRepository.save(reviewTagMaster);

        return this.toResponse(save);
    }

    public ReviewTagMasterResponse update(ReviewTagMasterRequest request){
        ReviewTagMaster find = reviewTagMasterRepository.findById(request.getSid())
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, ERROR_MESSAGE));

        ReviewTagMaster update = ReviewTagMaster.builder()
                .sid(find.getSid())
                .reviewTagName(request.getReviewTagName())
                .reviewTagCategory(request.getReviewTagCategory())
                .build();

        ReviewTagMaster save = reviewTagMasterRepository.save(update);

        return this.toResponse(save);
    }

    public ReviewTagMasterResponse findById(Long sid){
        ReviewTagMaster find = reviewTagMasterRepository.findById(sid)
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, ERROR_MESSAGE));

        return this.toResponse(find);
    }

    public ReviewTagMasterResponse deleteById(Long sid){
        ReviewTagMaster find = reviewTagMasterRepository.findById(sid)
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, ERROR_MESSAGE));

        reviewTagMasterRepository.deleteById(sid);

        return this.toResponse(find);
    }

    public List<ReviewTagMasterResponse> findAll(){
        List<ReviewTagMaster> reviewTagMasters = reviewTagMasterRepository.findAll();

        return reviewTagMasters.stream()
                .map(this::toResponse)
                .toList();
    }

    private ReviewTagMasterResponse toResponse(ReviewTagMaster reviewTagMaster){
        return ReviewTagMasterResponse.builder()
                .sid(reviewTagMaster.getSid())
                .reviewTagName(reviewTagMaster.getReviewTagName())
                .reviewTagCategory(reviewTagMaster.getReviewTagCategory())
                .build();
    }
}
