package com.mjc.hotel.review;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.review.entity.*;
import com.mjc.hotel.review.entity.enums.ReactionType;
import com.mjc.hotel.review.entity.enums.ReviewTagCategory;
import com.mjc.hotel.review.entity.enums.TravelType;
import com.mjc.hotel.review.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class ReviewServiceTest {
    @Autowired
    private ReviewPhotoRepository reviewPhotoRepository;
    @Autowired
    private ReviewReactionRepository reviewReactionRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewCategoryRepository reviewCategoryRepository;
    @Autowired
    private ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    @Autowired
    private ReviewTagRepository reviewTagRepository;
    @Autowired
    private ReviewTagMasterRepository reviewTagMasterRepository;
    @Autowired
    private ReviewAnswerRepository reviewAnswerRepository;

    @DisplayName("리뷰 테이블 테스트")
    @Test
    @Commit
    public void insertReview() {
        Hotel hotel = Hotel
                .builder()
                .sid(1L)
                .build();

        Member member = Member
                .builder()
                .memberId(1L)
                .build();

        Reservation reservation = Reservation
                .builder()
                .sid(1L)
                .build();

        Review review = Review.builder()
                .hotel(hotel)
                .member(member)
                .reservation(reservation)
                .rating(5)
                .travelType(TravelType.SOLO)
                .content("혼자 여행할때 묵기 좋은 호텔 객실입니다.")
                .likeCount(0)
                .dislikeCount(0)
                .build();

        reviewRepository.save(review);
    }

    @DisplayName("항목 마스터 테이블 테스트")
    @Test
    @Commit
    public void insertReviewCategoryMaster(){
        ReviewCategoryMaster reviewCategoryMaster = ReviewCategoryMaster.builder()
                .reviewCategoryName("청결도")
                .build();

        reviewCategoryMasterRepository.save(reviewCategoryMaster);
    }

    @DisplayName("항목별 리뷰 테이블 테스트")
    @Test
    @Commit
    public void insertReviewCategory(){
        Review review = Review.builder()
                .reviewId(1L)
                .build();

        ReviewCategoryMaster reviewCategoryMaster = ReviewCategoryMaster.builder()
                .reviewCategoryMasterId(1L)
                .reviewCategoryName("청결도")
                .build();

        ReviewCategory reviewCategory = ReviewCategory.builder()
                .review(review)
                .reviewCategoryMaster(reviewCategoryMaster)
                .rating(4)
                .build();

        reviewCategoryRepository.save(reviewCategory);
    }

    @DisplayName("태그 마스터 테이블 테스트")
    @Test
    @Commit
    public void insertReviewTagMaster(){
        ReviewTagMaster reviewTagMaster = ReviewTagMaster.builder()
                .reviewTagName("청결함")
                .reviewTagCategory(ReviewTagCategory.CONS)
                .build();
        reviewTagMasterRepository.save(reviewTagMaster);
    }

    @DisplayName("태그 테이블 테스트")
    @Test
    @Commit
    public void insertReviewTag(){
        Review review = Review.builder()
                .reviewId(1L)
                .build();

        ReviewTagMaster reviewTagMaster = ReviewTagMaster.builder()
                .reviewTagMasterId(1L)
                .build();

        ReviewTag reviewTag = ReviewTag.builder()
                .reviewTagMaster(reviewTagMaster)
                .review(review)
                .build();

        reviewTagRepository.save(reviewTag);
    }

    @DisplayName("리뷰 사진 테이블 테스트")
    @Test
    @Commit
    public void insertReviewPhoto(){
        Review review = Review.builder()
                .reviewId(1L)
                .build();

        ReviewPhoto reviewPhoto = ReviewPhoto.builder()
                .review(review)
                .imagePath("이미지 경로")
                .build();

        reviewPhotoRepository.save(reviewPhoto);
    }

    @DisplayName("리뷰 평가 테이블 테스트")
    @Test
    @Commit
    public void insertReviewReaction(){
        Review review = Review.builder()
                .reviewId(1L)
                .build();

        Member member = Member
                .builder()
                .memberId(1L)
                .build();

        ReviewReaction reviewReaction = ReviewReaction.builder()
                .review(review)
                .member(member)
                .reactionType(ReactionType.GOOD)
                .build();

        reviewReactionRepository.save(reviewReaction);
    }

    @DisplayName("리뷰 답변 테이블 테스트")
    @Test
    @Commit
    public void insertReviewAnswer(){
        Review review = Review.builder()
                .reviewId(1L)
                .build();

        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .review(review)
                .reviewAnswer("좋은 리뷰 감사합니다.")
                .build();

        reviewAnswerRepository.save(reviewAnswer);
    }
}
