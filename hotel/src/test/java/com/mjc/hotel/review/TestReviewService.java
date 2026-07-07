package com.mjc.hotel.review;

import com.mjc.hotel.review.entity.enums.TravelType;
import com.mjc.hotel.review.request.ReviewCategoryRequest;
import com.mjc.hotel.review.request.ReviewCreateRequest;
import com.mjc.hotel.review.request.ReviewTagRequest;
import com.mjc.hotel.review.request.ReviewUpdateRequest;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.service.ReviewService;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class TestReviewService {

    @Autowired
    private ReviewService reviewService;

    private static ReviewResponse TEST_REVIEW;

    @DisplayName("리뷰 테이블 저장 테스트")
    @Test
    @Order(1)
    public void testInsertReview() {

        LocalDateTime now = LocalDateTime.now();

        List<ReviewCategoryRequest> categories = setCategories(1L,5);

        List<ReviewTagRequest> tags = setTags(1L);

        ReviewCreateRequest request = new ReviewCreateRequest(
                1L,
                1L,
                1L,
                5,
                TravelType.SOLO,
                "테스트33",
                categories,
                tags
        );

        ReviewResponse insert = reviewService.insertReview(request);

        assertThat(insert)
                .isNotNull();
        assertThat(insert.getSid())
                .isNotNull();
        assertThat(insert.getHotelId())
                .isEqualTo(request.getHotelId());
        assertThat(insert.getMemberId())
                .isEqualTo(request.getMemberId());
        assertThat(insert.getReservationId())
                .isEqualTo(request.getReservationId());
        assertThat(insert.getRating())
                .isEqualTo(request.getRating());
        assertThat(insert.getTravelType())
                .isEqualTo(request.getTravelType());
        assertThat(insert.getContent())
                .isEqualTo(request.getContent());
        assertThat(insert.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(request.getCategories().getFirst().getCategoryId());
        assertThat(insert.getCategories().getFirst().getRating())
                .isEqualTo(request.getCategories().getFirst().getRating());
        assertThat(insert.getTags().getFirst().getReviewTagId())
                .isEqualTo(request.getTags().getFirst().getTagId());
        assertThat(insert.getCreatedAt())
                .isNotNull();
        assertThat(insert.getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insert.getUpdatedAt())
                .isNotNull();
        assertThat(insert.getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insert.getDeletedAt())
                .isNull();
        assertThat(insert.getDeleted())
                .isEqualTo(false);

        TEST_REVIEW = insert;
        log.info("id : " + TEST_REVIEW.getSid().toString());
    }

    @DisplayName("리뷰 테이블 검색 테스트")
    @Test
    @Order(2)
    public void testFindByReviewId() {
        log.info("id : " + TEST_REVIEW.getSid().toString());
        assertThrows(DataNotFoundException.class, () -> reviewService.findByReviewId(0L));

        ReviewResponse find = reviewService.findByReviewId(TEST_REVIEW.getSid());

        assertThat(find)
                .isNotNull();
        assertThat(find.getSid())
                .isNotNull();
        assertThat(find.getHotelId())
                .isEqualTo(TEST_REVIEW.getHotelId());
        assertThat(find.getMemberId())
                .isEqualTo(TEST_REVIEW.getMemberId());
        assertThat(find.getReservationId())
                .isEqualTo(TEST_REVIEW.getReservationId());
        assertThat(find.getRating())
                .isEqualTo(TEST_REVIEW.getRating());
        assertThat(find.getTravelType())
                .isEqualTo(TEST_REVIEW.getTravelType());
        assertThat(find.getContent())
                .isEqualTo(TEST_REVIEW.getContent());
        assertThat(find.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getReviewCategoryId());
        assertThat(find.getCategories().getFirst().getRating())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getRating());
        assertThat(find.getTags().getFirst().getReviewTagId())
                .isEqualTo(TEST_REVIEW.getTags().getFirst().getReviewTagId());
        assertThat(find.getCreatedAt())
                .isNotNull();
        assertThat(find.getUpdatedAt())
                .isNotNull();
        assertThat(find.getDeletedAt())
                .isNull();
        assertThat(find.getDeleted())
                .isEqualTo(false);
    }

    @DisplayName("리뷰 테이블 수정 테스트")
    @Test
    @Order(3)
    public void testUpdateReview() {
        log.info("id : " + TEST_REVIEW.getSid().toString());
        ReviewUpdateRequest fail =  new ReviewUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertThrows(DataNotFoundException.class,() -> reviewService.updateReview(fail));

        List<ReviewCategoryRequest> categories = setCategories(
                TEST_REVIEW.getCategories().getFirst().getReviewCategoryId(),
                TEST_REVIEW.getCategories().getFirst().getRating()
        );
        List<ReviewTagRequest> tags = setTags(
                TEST_REVIEW.getTags().getFirst().getReviewTagId()
        );
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                TEST_REVIEW.getSid(),
                TEST_REVIEW.getRating(),
                TEST_REVIEW.getTravelType(),
                TEST_REVIEW.getContent(),
                categories,
                tags
        );
        ReviewResponse update = reviewService.updateReview(request);

        assertThat(update)
                .isNotNull();
        assertThat(update.getSid())
                .isNotNull();
        assertThat(update.getRating())
                .isEqualTo(request.getRating());
        assertThat(update.getTravelType())
                .isEqualTo(request.getTravelType());
        assertThat(update.getContent())
                .isEqualTo(request.getContent());
        assertThat(update.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getReviewCategoryId());
        assertThat(update.getCategories().getFirst().getRating())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getRating());
        assertThat(update.getTags().getFirst().getReviewTagId())
                .isEqualTo(TEST_REVIEW.getTags().getFirst().getReviewTagId());
        assertThat(update.getCreatedAt())
                .isNotNull();
        assertThat(update.getUpdatedAt())
                .isNotNull();
        assertThat(update.getDeletedAt())
                .isNull();
        assertThat(update.getDeleted())
                .isEqualTo(false);
        TEST_REVIEW = update;
    }

    @DisplayName("리뷰 테이블 삭제 테스트")
    @Test
    @Order(4)
    public void testDeleteReview() {
        log.info("id : " + TEST_REVIEW.getSid().toString());

        LocalDateTime now = LocalDateTime.now();

        assertThrows(DataNotFoundException.class, () -> reviewService.deleteReviewId(0L));

        ReviewResponse delete = reviewService.deleteReviewId(TEST_REVIEW.getSid());

        assertThat(delete)
                .isNotNull();
        assertThat(delete.getSid())
                .isNotNull();
        assertThat(delete.getCreatedAt())
                .isNotNull();
        assertThat(delete.getUpdatedAt())
                .isNotNull();
        assertThat(delete.getDeletedAt())
                .isNotNull();
        assertThat(delete.getDeletedAt())
                .isAfterOrEqualTo(now);
        assertThat(delete.getDeleted())
                .isEqualTo(true);

        TEST_REVIEW = delete;
    }

    @DisplayName("리뷰 테이블 삭제 컬럼 검색 테스트")
    @Test
    @Order(5)
    public void testFindDeletedReview(){
        assertThrows(DataNotFoundException.class, () -> reviewService.findByReviewId(TEST_REVIEW.getSid()));
    }

    @DisplayName("리뷰 테이블 호텔 Id 리뷰 조회 테스트")
    @Test
    @Order(6)
    public void testReviewsInHotel(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewResponse> responses = reviewService.reviewsInHotel(1L, pageable);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
    }

    @DisplayName("리뷰 테이블 호텔 Id 긍정 리뷰 조회 테스트")
    @Test
    @Order(7)
    public void testPositiveReviewsInHotel(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewResponse> responses = reviewService.positiveReviewsInHotel(1L, pageable);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
        assertThat(responses.getContent())
                .allMatch(response -> response.getRating() >= 4);
    }

    @DisplayName("리뷰 테이블 호텔 Id 사진 포함 리뷰 조회 테스트")
    @Test
    @Order(8)
    public void testExistsPhotoReviewsInHotel(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewResponse> responses = reviewService.existsPhotoReviewsInHotel(1L, pageable);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
        assertThat(responses.getTotalElements())
                .isEqualTo(1);
    }

    private List<ReviewTagRequest> setTags(Long tagId) {
        List<ReviewTagRequest> tags = new ArrayList<>();
        ReviewTagRequest tag = new ReviewTagRequest(tagId);
        tags.add(tag);
        return tags;
    }

    private List<ReviewCategoryRequest> setCategories(Long categoryId, Integer rating) {
        List<ReviewCategoryRequest> categories = new ArrayList<>();
        ReviewCategoryRequest category = new ReviewCategoryRequest(categoryId,rating);
        categories.add(category);
        return categories;
    }
}
