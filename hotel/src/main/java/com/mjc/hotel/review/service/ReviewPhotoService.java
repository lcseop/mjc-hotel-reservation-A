package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewPhoto;
import com.mjc.hotel.review.repository.ReviewPhotoRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewPhotoCreateRequest;
import com.mjc.hotel.review.request.ReviewPhotoUpdateRequest;
import com.mjc.hotel.review.response.ReviewPhotoResponse;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewPhotoService {

    private final ReviewRepository reviewRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;

    @Value("${review.images}")
    private String uploadDir;

    @Transactional
    public List<ReviewPhotoResponse> insertReviewPhotos(ReviewPhotoCreateRequest request) {
        Review review = reviewRepository.findBySidAndDeletedFalse(request.getReviewId());
        if(review == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }

        if (request.getPhotos() == null || request.getPhotos().isEmpty()) {
            throw new IllegalArgumentException("Files Cannot Be Empty");
        }
        List<ReviewPhotoResponse> result = new ArrayList<>();
        for (MultipartFile photo : request.getPhotos()) {
            if (photo.isEmpty() || this.falseValidatePhotoFile(photo)) {
                continue;
            }

            ReviewPhoto reviewPhoto = this.saveImageFile(review, photo);
            ReviewPhoto save = reviewPhotoRepository.save(reviewPhoto);

            ReviewPhotoResponse response = this.toReviewPhotoResponse(save);

            result.add(response);
        }
        return result;
    }

    public ReviewPhotoResponse updateReviewPhoto(ReviewPhotoUpdateRequest request){
        Review review = reviewRepository.findBySidAndDeletedFalse(request.getReviewId());
        if(review == null) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        ReviewPhoto oldPhoto = reviewPhotoRepository.findBySidAndDeletedFalse(request.getSid());
        if(oldPhoto == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Photo Not Found");
        }
        MultipartFile photo = request.getPhoto();
        if(photo.isEmpty() || this.falseValidatePhotoFile(photo)) {
            throw new IllegalArgumentException("Files Cannot Be Empty");
        }
        oldPhoto.markDeleted();
        reviewPhotoRepository.save(oldPhoto);

        ReviewPhoto newPhoto = this.saveImageFile(review, photo);
        ReviewPhoto save = reviewPhotoRepository.save(newPhoto);

        ReviewPhotoResponse response = this.toReviewPhotoResponse(save);
        return response;
    }

    public List<ReviewPhotoResponse> findAllByReviewId(Long reviewId){
        List<ReviewPhoto> photos = reviewPhotoRepository.findAllByReviewSidAndDeletedFalse(reviewId);
        if(photos.isEmpty()){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Photos Not Found");
        }
        List<ReviewPhotoResponse> result = photos.stream()
                .map(this::toReviewPhotoResponse)
                .toList();

        return result;
    }
    public Page<ReviewPhotoResponse> search(Long reviewId, Pageable pageable) {
        Page<ReviewPhoto> reviewPhotos = reviewPhotoRepository.findAllByReviewSidAndDeletedFalse(reviewId,pageable);
        if(reviewPhotos.isEmpty()){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Photos Not Found");
        }
        List<ReviewPhotoResponse> list = reviewPhotos.stream()
                .map(this::toReviewPhotoResponse)
                .toList();

        Page<ReviewPhotoResponse> responses = new PageImpl<>(list, pageable, reviewPhotos.getTotalElements());
        return responses;
    }

    public ReviewPhotoResponse deleteReviewImage(Long reviewPhotoId) {
        ReviewPhoto reviewPhoto = reviewPhotoRepository.findBySidAndDeletedFalse(reviewPhotoId);
        if(reviewPhoto == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Photo Not Found");
        }
        reviewPhoto.markDeleted();
        ReviewPhoto save = reviewPhotoRepository.save(reviewPhoto);

        ReviewPhotoResponse response = this.toReviewPhotoResponse(save);

        return response;
    }

    private ReviewPhoto saveImageFile(Review review, MultipartFile photo) {
        try {
            String originalFileName = photo.getOriginalFilename();
            String extension = this.getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFileName);

            photo.transferTo(filePath.toFile());

            ReviewPhoto result = ReviewPhoto.builder()
                    .review(review)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath.toString())
                    .imagePath("/images/reviews/" + storedFileName)
                    .build();

            result.prePersist();

            return result;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

    private Boolean falseValidatePhotoFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            return true;
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return true;
        }
        return false;
    }

    private ReviewPhotoResponse toReviewPhotoResponse(ReviewPhoto reviewPhoto) {
        ReviewPhotoResponse response = ReviewPhotoResponse.builder()
                .sid(reviewPhoto.getSid())
                .originalFileName(reviewPhoto.getOriginalFileName())
                .imagePath(reviewPhoto.getImagePath())
                .createdAt(reviewPhoto.getCreatedAt())
                .updatedAt(reviewPhoto.getUpdatedAt())
                .deletedAt(reviewPhoto.getDeletedAt())
                .deleted(reviewPhoto.getDeleted())
                .build();
        return response;
    }
}
