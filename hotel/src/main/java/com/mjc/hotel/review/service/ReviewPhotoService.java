package com.mjc.hotel.review.service;

import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.entity.ReviewPhoto;
import com.mjc.hotel.review.repository.ReviewPhotoRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.request.ReviewPhotoRequest;
import com.mjc.hotel.review.response.ReviewPhotoResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    public List<ReviewPhotoResponse> insertReviewImages(ReviewPhotoRequest request) throws IOException {
        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow();

        if (request.getReviewPhotos() == null || request.getReviewPhotos().isEmpty()) {
            return null;
        }
        List<ReviewPhotoResponse> result = new ArrayList<>();
        for (MultipartFile photo : request.getReviewPhotos()) {
            if (photo.isEmpty()) {
                continue;
            }

            this.validatePhotoFile(photo);

            ReviewPhoto reviewImage = this.saveImageFile(review, photo);
            ReviewPhoto save = reviewPhotoRepository.save(reviewImage);

            ReviewPhotoResponse response = ReviewPhotoResponse.builder()
                    .sid(save.getSid())
                    .originalFileName(save.getOriginalFileName())
                    .imagePath(save.getImagePath())
                    .build();

            result.add(response);
        }
        return result;
    }

    private ReviewPhoto saveImageFile(Review review, MultipartFile photo) {
        try {
            String originalFileName = photo.getOriginalFilename();
            String extension = getExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFileName);

            photo.transferTo(filePath.toFile());

            return ReviewPhoto.builder()
                    .review(review)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath.toString())
                    .imagePath("/images/reviews/" + storedFileName)
                    .build();

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

    private void validatePhotoFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
    }

    @Transactional
    public void deleteReviewImage(Long reviewId, Long reviewImageId) {
        ReviewPhoto reviewImage = reviewPhotoRepository
                .findBySidAndReviewSid(reviewImageId, reviewId)
                .orElseThrow();

        try {
            Path path = Paths.get(reviewImage.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 삭제 중 오류가 발생했습니다.", e);
        }

        reviewPhotoRepository.delete(reviewImage);
    }
}
