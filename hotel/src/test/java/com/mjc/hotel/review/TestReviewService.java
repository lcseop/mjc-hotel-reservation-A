package com.mjc.hotel.review;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.service.MemberService;
import com.mjc.hotel.review.entity.enums.ReactionType;
import com.mjc.hotel.review.entity.enums.TravelType;
import com.mjc.hotel.review.request.*;
import com.mjc.hotel.review.response.ReviewAnswerResponse;
import com.mjc.hotel.review.response.ReviewPhotoResponse;
import com.mjc.hotel.review.response.ReviewReactionResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.service.ReviewAnswerService;
import com.mjc.hotel.review.service.ReviewPhotoService;
import com.mjc.hotel.review.service.ReviewReactionService;
import com.mjc.hotel.review.service.ReviewService;
import com.mjc.hotel.util.excep.DataNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestReviewService {
    //카테고리, 태그 마스터 테이블에 데이터가 없다면 TestMasterTable 테스트 클래스를 실행하여 데이터들을 만들것.
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewReactionService reviewReactionService;
    @Autowired
    private ReviewPhotoService reviewPhotoService;
    @Autowired
    private ReviewAnswerService reviewAnswerService;
    @Autowired
    private MemberService memberService;

    private static ReviewResponse TEST_REVIEW;
    private static ReviewReactionResponse TEST_REVIEW_REACTION;
    private static ReviewAnswerResponse TEST_REVIEW_ANSWER;
    private static final List<ReviewPhotoResponse> TEST_REVIEW_PHOTOS = new ArrayList<>();

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

    @DisplayName("리뷰 테이블 저장 테스트")
    @Test
    @Order(1)
    @Commit
    public void testInsertReview() {
        LocalDateTime now = LocalDateTime.now();

        //저장 전 포인트
        Member member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();
        Integer point = member.getPoint();

        List<ReviewCategoryRequest> categories = new ArrayList<>();
        setCategories(categories,1L,5);

        List<ReviewTagRequest> tags = new ArrayList<>();
        setTags(tags, 1L);

        ReviewCreateRequest request = new ReviewCreateRequest(
                1L,
                1L,
                1L,
                5,
                TravelType.SOLO,
                "리뷰 저장 테스트",
                categories,
                tags
        );

        ReviewResponse inserted = reviewService.insertReview(request);

        assertThat(inserted)
                .isNotNull();
        assertThat(inserted.getSid())
                .isNotNull();
        assertThat(inserted.getHotelId())
                .isEqualTo(1L);
        assertThat(inserted.getMemberId())
                .isEqualTo(1L);
        assertThat(inserted.getReservationId())
                .isEqualTo(1L);
        assertThat(inserted.getRating())
                .isEqualTo(5);
        assertThat(inserted.getTravelType())
                .isEqualTo(TravelType.SOLO);
        assertThat(inserted.getContent())
                .isEqualTo("리뷰 저장 테스트");
        assertThat(inserted.getCategories().size())
                .isEqualTo(1);
        assertThat(inserted.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(1L);
        assertThat(inserted.getCategories().getFirst().getRating())
                .isEqualTo(5);
        assertThat(inserted.getTags().size())
                .isEqualTo(1);
        assertThat(inserted.getTags().getFirst().getReviewTagId())
                .isEqualTo(1L);
        assertThat(inserted.getCreatedAt())
                .isNotNull();
        assertThat(inserted.getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getUpdatedAt())
                .isNotNull();
        assertThat(inserted.getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getDeletedAt())
                .isNull();
        assertThat(inserted.getDeleted())
                .isEqualTo(false);

        //저장 후 포인트,
        //같은 예약으로 리뷰가 저장된 기록이 있을 때 포인트 중복 지급 방지용, 예약으로 이미 리뷰가 존재할 때 실패함.
        member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();

        assertThat(member.getPoint())
                .isGreaterThan(point);

        TEST_REVIEW = inserted;
    }

    @DisplayName("리뷰 테이블 검색 테스트")
    @Test
    @Order(2)
    @Commit
    public void testFindByReviewId() {
        assertThrows(DataNotFoundException.class, () -> reviewService.findByReviewId(0L));

        ReviewResponse found = reviewService.findByReviewId(TEST_REVIEW.getSid());

        assertThat(found)
                .isNotNull();
        assertThat(found.getSid())
                .isEqualTo(TEST_REVIEW.getSid());
        assertThat(found.getHotelId())
                .isEqualTo(TEST_REVIEW.getHotelId());
        assertThat(found.getMemberId())
                .isEqualTo(TEST_REVIEW.getMemberId());
        assertThat(found.getReservationId())
                .isEqualTo(TEST_REVIEW.getReservationId());
        assertThat(found.getRating())
                .isEqualTo(TEST_REVIEW.getRating());
        assertThat(found.getTravelType())
                .isEqualTo(TEST_REVIEW.getTravelType());
        assertThat(found.getContent())
                .isEqualTo(TEST_REVIEW.getContent());
        assertThat(found.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getReviewCategoryId());
        assertThat(found.getCategories().getFirst().getRating())
                .isEqualTo(TEST_REVIEW.getCategories().getFirst().getRating());
        assertThat(found.getTags().getFirst().getReviewTagId())
                .isEqualTo(TEST_REVIEW.getTags().getFirst().getReviewTagId());
        assertThat(found.getCreatedAt())
                .isCloseTo(TEST_REVIEW.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(TEST_REVIEW.getUpdatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getDeletedAt())
                .isNull();
        assertThat(found.getDeleted())
                .isEqualTo(TEST_REVIEW.getDeleted());
    }

    @DisplayName("리뷰 테이블 수정 테스트")
    @Test
    @Order(3)
    @Commit
    public void testUpdateReview() {
        ReviewUpdateRequest fail =  new ReviewUpdateRequest(
                0L,
                null,
                null,
                null,
                null,
                null
        );
        assertThrows(DataNotFoundException.class,() -> reviewService.updateReview(fail));

        List<ReviewCategoryRequest> categories = new ArrayList<>();
        setCategories(categories, 1L, 5);
        setCategories(categories, 2L, 4);

        List<ReviewTagRequest> tags = new ArrayList<>();
        setTags(tags, 1L);
        setTags(tags, 2L);

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                TEST_REVIEW.getSid(),
                4,
                TravelType.FAMILY,
                "리뷰 수정 테스트",
                categories,
                tags
        );
        ReviewResponse updated = reviewService.updateReview(request);

        assertThat(updated)
                .isNotNull();
        assertThat(updated.getSid())
                .isNotNull();

        assertThat(updated.getRating())
                .isNotEqualTo(TEST_REVIEW.getRating());
        assertThat(updated.getRating())
                .isEqualTo(4);

        assertThat(updated.getTravelType())
                .isNotEqualTo(TEST_REVIEW.getTravelType());
        assertThat(updated.getTravelType())
                .isEqualTo(TravelType.FAMILY);

        assertThat(updated.getContent())
                .isNotEqualTo(TEST_REVIEW.getContent());
        assertThat(updated.getContent())
                .isEqualTo("리뷰 수정 테스트");

        assertThat(updated.getCategories().size())
                .isNotEqualTo(TEST_REVIEW.getCategories().size());
        assertThat(updated.getCategories().size())
                .isEqualTo(2);
        assertThat(updated.getCategories().getFirst().getReviewCategoryId())
                .isEqualTo(1L);
        assertThat(updated.getCategories().getFirst().getRating())
                .isEqualTo(5);
        assertThat(updated.getCategories().getLast().getReviewCategoryId())
                .isEqualTo(2L);
        assertThat(updated.getCategories().getLast().getRating())
                .isEqualTo(4);

        assertThat(updated.getTags().size())
                .isNotEqualTo(TEST_REVIEW.getTags().size());
        assertThat(updated.getTags().size())
                .isEqualTo(2);
        assertThat(updated.getTags().getFirst().getReviewTagId())
                .isEqualTo(1L);
        assertThat(updated.getTags().getLast().getReviewTagId())
                .isEqualTo(2L);

        TEST_REVIEW = updated;
    }

    @DisplayName("리뷰 좋아요 싫어요 테이블 저장 테스트")
    @Test
    @Order(4)
    @Commit
    public void testAddReviewReaction(){
        LocalDateTime now = LocalDateTime.now();

        ReviewReactionRequest request = new ReviewReactionRequest(
                TEST_REVIEW.getSid(),
                TEST_REVIEW.getMemberId(),
                ReactionType.GOOD
        );
        ReviewReactionResponse inserted = reviewReactionService.addReviewReaction(request);

        assertThat(inserted)
                .isNotNull();
        assertThat(inserted.getReviewId())
                .isNotNull();
        assertThat(inserted.getMemberId())
                .isNotNull();
        assertThat(inserted.getReactionType())
                .isEqualTo(ReactionType.GOOD);
        assertThat(inserted.getCreatedAt())
                .isNotNull();
        assertThat(inserted.getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getUpdatedAt())
                .isNotNull();
        assertThat(inserted.getUpdatedAt())
                .isAfterOrEqualTo(now);

        TEST_REVIEW_REACTION = inserted;

        ReviewResponse reviewUpdated = reviewService.findByReviewId(TEST_REVIEW.getSid());
        assertThat(reviewUpdated)
                .isNotNull();
        assertThat(reviewUpdated.getLikeCount())
                .isEqualTo(1);
        assertThat(reviewUpdated.getDislikeCount())
                .isEqualTo(0);

        TEST_REVIEW = reviewUpdated;
    }
    @DisplayName("리뷰 좋아요 싫어요 테이블 검색 테스트")
    @Test
    @Order(5)
    @Commit
    public void testFindReviewReaction(){
        assertThrows(DataNotFoundException.class, () -> reviewReactionService.findReviewReaction(0L,0L));

        ReviewReactionResponse found = reviewReactionService.findReviewReaction(TEST_REVIEW_REACTION.getReviewId(),TEST_REVIEW_REACTION.getMemberId());

        assertThat(found)
                .isNotNull();
        assertThat(found.getReviewId())
                .isEqualTo(TEST_REVIEW_REACTION.getReviewId());
        assertThat(found.getMemberId())
                .isEqualTo(TEST_REVIEW_REACTION.getMemberId());
        assertThat(found.getReactionType())
                .isEqualTo(TEST_REVIEW_REACTION.getReactionType());
        assertThat(found.getCreatedAt())
                .isCloseTo(TEST_REVIEW_REACTION.getCreatedAt(),within(1,ChronoUnit.MILLIS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(TEST_REVIEW_REACTION.getUpdatedAt(),within(1,ChronoUnit.MILLIS));
    }

    @DisplayName("리뷰 좋아요 싫어요 테이블 수정 테스트")
    @Test
    @Order(6)
    @Commit
    public void testUpdateReviewReaction(){
        ReviewReactionRequest fail1 =  new ReviewReactionRequest(
                0L,
                TEST_REVIEW_REACTION.getMemberId(),
                null
        );
        assertThrows(DataNotFoundException.class, ()-> reviewReactionService.updateReviewReaction(fail1));

        ReviewReactionRequest fail2 =  new ReviewReactionRequest(
                TEST_REVIEW_REACTION.getReviewId(),
                0L,
                null
        );
        assertThrows(DataNotFoundException.class, ()-> reviewReactionService.updateReviewReaction(fail2));

        ReviewReactionRequest request1 = new ReviewReactionRequest(
                TEST_REVIEW.getSid(),
                TEST_REVIEW.getMemberId(),
                ReactionType.BAD
        );

        ReviewReactionResponse reactionUpdated1 = reviewReactionService.updateReviewReaction(request1);
        assertThat(reactionUpdated1)
                .isNotNull();
        assertThat(reactionUpdated1.getReviewId())
                .isNotNull();
        assertThat(reactionUpdated1.getMemberId())
                .isNotNull();

        assertThat(reactionUpdated1.getReactionType())
                .isNotEqualTo(TEST_REVIEW_REACTION.getReactionType());
        assertThat(reactionUpdated1.getReactionType())
                .isEqualTo(ReactionType.BAD);

        TEST_REVIEW_REACTION = reactionUpdated1;

        ReviewResponse reviewUpdated1 = reviewService.findByReviewId(TEST_REVIEW.getSid());
        assertThat(reviewUpdated1)
                .isNotNull();

        assertThat(reviewUpdated1.getLikeCount())
                .isNotEqualTo(1);
        assertThat(reviewUpdated1.getLikeCount())
                .isEqualTo(0);

        assertThat(reviewUpdated1.getDislikeCount())
                .isNotEqualTo(0);
        assertThat(reviewUpdated1.getDislikeCount())
                .isEqualTo(1);

        TEST_REVIEW = reviewUpdated1;

        ReviewReactionRequest request2 = new ReviewReactionRequest(
                TEST_REVIEW.getSid(),
                TEST_REVIEW.getMemberId(),
                ReactionType.NONE
        );

        ReviewReactionResponse reactionUpdated2 = reviewReactionService.updateReviewReaction(request2);
        assertThat(reactionUpdated2)
                .isNotNull();
        assertThat(reactionUpdated2.getReviewId())
                .isNotNull();
        assertThat(reactionUpdated2.getMemberId())
                .isNotNull();

        assertThat(reactionUpdated2.getReactionType())
                .isNotEqualTo(TEST_REVIEW_REACTION.getReactionType());
        assertThat(reactionUpdated2.getReactionType())
                .isEqualTo(ReactionType.NONE);

        TEST_REVIEW_REACTION = reactionUpdated2;

        ReviewResponse reviewUpdated2 = reviewService.findByReviewId(TEST_REVIEW.getSid());
        assertThat(reviewUpdated2)
                .isNotNull();

        assertThat(reviewUpdated2.getLikeCount())
                .isNotEqualTo(1);
        assertThat(reviewUpdated2.getLikeCount())
                .isEqualTo(0);

        assertThat(reviewUpdated2.getDislikeCount())
                .isNotEqualTo(1);
        assertThat(reviewUpdated2.getDislikeCount())
                .isEqualTo(0);

        TEST_REVIEW = reviewUpdated2;
    }

    @DisplayName("리뷰 좋아요 싫어요 테이블 리뷰 Id 조회 테스트")
    @Test
    @Order(7)
    @Commit
    public void testFindAllByReviewIdAndReactionType(){
        assertThrows(DataNotFoundException.class, () -> reviewReactionService.findAllByReviewIdAndReactionType(0L,null));

        Long goodCount = reviewReactionService.findAllByReviewIdAndReactionType(TEST_REVIEW.getSid(),"GOOD");
        Long badCount = reviewReactionService.findAllByReviewIdAndReactionType(TEST_REVIEW.getSid(),"BAD");

        assertThat(goodCount)
                .isEqualTo(0);
        assertThat(badCount)
                .isEqualTo(0);
    }

    @DisplayName("리뷰 답변 테이블 저장 테스트")
    @Test
    @Order(8)
    @Commit
    public void testInsertReviewAnswer(){
        LocalDateTime now = LocalDateTime.now();
        ReviewAnswerCreateRequest request = new ReviewAnswerCreateRequest(
                TEST_REVIEW.getSid(),
                "리뷰 답변 저장 테스트"
        );
        ReviewAnswerResponse inserted = reviewAnswerService.insertReviewAnswer(request);

        assertThat(inserted)
                .isNotNull();
        assertThat(inserted.getSid())
                .isNotNull();
        assertThat(inserted.getReviewId())
                .isEqualTo(TEST_REVIEW.getSid());
        assertThat(inserted.getReviewAnswer())
                .isEqualTo("리뷰 답변 저장 테스트");
        assertThat(inserted.getCreatedAt())
                .isNotNull();
        assertThat(inserted.getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getUpdatedAt())
                .isNotNull();
        assertThat(inserted.getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getDeletedAt())
                .isNull();
        assertThat(inserted.getDeleted())
                .isEqualTo(false);

        TEST_REVIEW_ANSWER = inserted;
    }

    @DisplayName("리뷰 답변 테이블 검색 테스트")
    @Test
    @Order(9)
    @Commit
    public void testFindBySidReviewAnswer(){
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.findBySidReviewAnswer(0L));

        ReviewAnswerResponse found = reviewAnswerService.findBySidReviewAnswer(TEST_REVIEW_ANSWER.getSid());

        assertThat(found)
                .isNotNull();
        assertThat(found.getSid())
                .isEqualTo(TEST_REVIEW_ANSWER.getSid());
        assertThat(found.getReviewId())
                .isEqualTo(TEST_REVIEW_ANSWER.getReviewId());
        assertThat(found.getReviewAnswer())
                .isEqualTo(TEST_REVIEW_ANSWER.getReviewAnswer());
        assertThat(found.getCreatedAt())
                .isCloseTo(TEST_REVIEW_ANSWER.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(TEST_REVIEW_ANSWER.getUpdatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getDeletedAt())
                .isNull();
        assertThat(found.getDeleted())
                .isEqualTo(TEST_REVIEW_ANSWER.getDeleted());
    }

    @DisplayName("리뷰 답변 테이블 리뷰 Id 검색 테스트")
    @Test
    @Order(10)
    @Commit
    public void testFindByReviewSid(){
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.findByReviewSid(0L));

        ReviewAnswerResponse found = reviewAnswerService.findByReviewSid(TEST_REVIEW.getSid());

        assertThat(found)
                .isNotNull();
        assertThat(found.getSid())
                .isEqualTo(TEST_REVIEW_ANSWER.getSid());
        assertThat(found.getReviewId())
                .isEqualTo(TEST_REVIEW.getSid());
        assertThat(found.getReviewAnswer())
                .isEqualTo(TEST_REVIEW_ANSWER.getReviewAnswer());
        assertThat(found.getCreatedAt())
                .isCloseTo(TEST_REVIEW_ANSWER.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getUpdatedAt())
                .isCloseTo(TEST_REVIEW_ANSWER.getUpdatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(found.getDeletedAt())
                .isNull();
        assertThat(found.getDeleted())
                .isEqualTo(TEST_REVIEW_ANSWER.getDeleted());
    }

    @DisplayName("리뷰 답변 테이블 수정 테스트")
    @Test
    @Order(11)
    @Commit
    public void testUpdateReviewAnswer(){
        ReviewAnswerUpdateRequest fail = new ReviewAnswerUpdateRequest(
                0L,
                null,
                null
        );
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.updateReviewAnswer(fail));

        ReviewAnswerUpdateRequest request = new ReviewAnswerUpdateRequest(
                TEST_REVIEW_ANSWER.getSid(),
                TEST_REVIEW.getSid(),
                "리뷰 답변 수정 테스트"
        );
        ReviewAnswerResponse updated = reviewAnswerService.updateReviewAnswer(request);

        assertThat(updated)
                .isNotNull();
        assertThat(updated.getSid())
                .isNotNull();

        assertThat(updated.getReviewAnswer())
                .isNotEqualTo(TEST_REVIEW_ANSWER.getReviewAnswer());
        assertThat(updated.getReviewAnswer())
                .isEqualTo(request.getReviewAnswer());

        TEST_REVIEW_ANSWER = updated;
    }

    @DisplayName("리뷰 답변 테이블 삭제 테스트")
    @Test
    @Order(12)
    @Commit
    public void testDeleteReviewAnswer(){
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.deleteReviewAnswer(0L));

        ReviewAnswerResponse deleted = reviewAnswerService.deleteReviewAnswer(TEST_REVIEW_ANSWER.getSid());

        assertThat(deleted)
                .isNotNull();
        assertThat(deleted.getDeletedAt())
                .isNotNull();
        assertThat(deleted.getDeleted())
                .isEqualTo(true);

        TEST_REVIEW_ANSWER = deleted;
    }

    @DisplayName("리뷰 답변 테이블 삭제 컬럼 검색 테스트")
    @Test
    @Order(13)
    @Commit
    public void testFindDeletedReviewAnswer(){
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.findBySidReviewAnswer(TEST_REVIEW_ANSWER.getSid()));
    }

    @DisplayName("리뷰 사진 테이블 저장 테스트")
    @Test
    @Order(14)
    @Commit
    public void testInsertReviewPhotos(){
        LocalDateTime now = LocalDateTime.now();

        //저장 전 포인트
        Member member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();
        Integer point = member.getPoint();

        List<MultipartFile> photos = new ArrayList<>();

        MockMultipartFile photo = new MockMultipartFile(
                "file",
                "insert.jpg",
                "image/jpeg",
                new byte[10]
        );
        photos.add(photo);

        ReviewPhotoCreateRequest request = new ReviewPhotoCreateRequest(
                TEST_REVIEW.getSid(),
                photos
        );

        List<ReviewPhotoResponse> inserted = reviewPhotoService.insertReviewPhotos(request);

        assertThat(inserted)
                .isNotNull();
        assertThat(inserted.size())
                .isEqualTo(1);
        assertThat(inserted.getFirst().getOriginalFileName())
                .isEqualTo("insert.jpg");
        assertThat(inserted.getFirst().getImagePath().substring(0,16))
                .isEqualTo("/images/reviews/");
        assertThat(inserted.getFirst().getCreatedAt())
                .isNotNull();
        assertThat(inserted.getFirst().getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getFirst().getUpdatedAt())
                .isNotNull();
        assertThat(inserted.getFirst().getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(inserted.getFirst().getDeletedAt())
                .isNull();
        assertThat(inserted.getFirst().getDeleted())
                .isEqualTo(false);

        //저장 후 포인트,
        //리뷰에 이미 사진을 저장한 기록이 있는데 새로 저장 할 때 중복 지급 방지용, 리뷰에 사진이 존재할 때 실패함.
        member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();

        assertThat(member.getPoint())
                .isGreaterThan(point);

        TEST_REVIEW_PHOTOS.clear();
        TEST_REVIEW_PHOTOS.addAll(inserted);
    }

    @DisplayName("리뷰 사진 테이블 리뷰 Id 검색 테스트")
    @Test
    @Order(15)
    @Commit
    public void testSearchPhoto(){
        assertThrows(DataNotFoundException.class, () -> reviewPhotoService.search(0L,null));

        Page<ReviewPhotoResponse> responses = reviewPhotoService.search(TEST_REVIEW.getSid(), DEFAULT_PAGEABLE);

        assertThat(responses)
                .isNotNull();
        assertThat(responses)
                .allMatch(response -> response.getImagePath().startsWith("/images/reviews/"));

        for(int i = 0; i < TEST_REVIEW_PHOTOS.size(); i++){
            assertThat(responses.getContent().get(i).getSid())
                    .isEqualTo(TEST_REVIEW_PHOTOS.get(i).getSid());
            assertThat(responses.getContent().get(i).getOriginalFileName())
                    .isEqualTo(TEST_REVIEW_PHOTOS.get(i).getOriginalFileName());
            assertThat(responses.getContent().get(i).getCreatedAt())
                    .isCloseTo(TEST_REVIEW_PHOTOS.get(i).getCreatedAt(),within(1, ChronoUnit.MILLIS));
            assertThat(responses.getContent().get(i).getUpdatedAt())
                    .isCloseTo(TEST_REVIEW_PHOTOS.get(i).getUpdatedAt(),within(1, ChronoUnit.MILLIS));
            assertThat(responses.getContent().get(i).getDeletedAt())
                    .isNull();
            assertThat(responses.getContent().get(i).getDeleted())
                    .isEqualTo(false);
        }
    }

    @DisplayName("리뷰 사진 테이블 수정 테스트")
    @Test
    @Order(16)
    @Commit
    public void testUpdateReviewPhoto(){
        MockMultipartFile photo = new MockMultipartFile(
                "file",
                "update.jpg",
                "image/jpeg",
                new byte[10]
        );

        ReviewPhotoUpdateRequest fail1 = new ReviewPhotoUpdateRequest(
                0L,
                TEST_REVIEW.getSid(),
                photo
        );
        assertThrows(DataNotFoundException.class, ()-> reviewPhotoService.updateReviewPhoto(fail1));

        ReviewPhotoUpdateRequest fail2 = new ReviewPhotoUpdateRequest(
                TEST_REVIEW_PHOTOS.getFirst().getSid(),
                0L,
                photo
        );
        assertThrows(DataNotFoundException.class, ()-> reviewPhotoService.updateReviewPhoto(fail2));

        ReviewPhotoUpdateRequest fail3 = new ReviewPhotoUpdateRequest(
                TEST_REVIEW_PHOTOS.getFirst().getSid(),
                TEST_REVIEW.getSid(),
                null
        );
        assertThrows(IllegalArgumentException.class, ()-> reviewPhotoService.updateReviewPhoto(fail3));

        ReviewPhotoUpdateRequest request = new ReviewPhotoUpdateRequest(
                TEST_REVIEW_PHOTOS.getFirst().getSid(),
                TEST_REVIEW.getSid(),
                photo
        );

        ReviewPhotoResponse updated = reviewPhotoService.updateReviewPhoto(request);

        assertThat(updated)
                .isNotNull();

        assertThat(updated.getOriginalFileName())
                .isNotEqualTo(TEST_REVIEW_PHOTOS.getFirst().getOriginalFileName());
        assertThat(updated.getOriginalFileName())
                .isEqualTo("update.jpg");

        assertThat(updated.getImagePath().substring(0,16))
                .isEqualTo("/images/reviews/");

        TEST_REVIEW_PHOTOS.clear();
        TEST_REVIEW_PHOTOS.add(updated);
    }

    @DisplayName("리뷰 사진 테이블 삭제 테스트")
    @Test
    @Order(17)
    @Commit
    public void testDeleteReviewPhoto(){
        assertThrows(DataNotFoundException.class,()-> reviewPhotoService.deleteReviewImage(0L));

        ReviewPhotoResponse delete = reviewPhotoService.deleteReviewImage(TEST_REVIEW_PHOTOS.getFirst().getSid());

        assertThat(delete)
                .isNotNull();
        assertThat(delete.getDeletedAt())
                .isNotNull();
        assertThat(delete.getDeleted())
                .isEqualTo(true);

        TEST_REVIEW_PHOTOS.clear();
        TEST_REVIEW_PHOTOS.add(delete);
    }

    @DisplayName("리뷰 사진 테이블 삭제 컬럼 검색 테스트")
    @Test
    @Order(18)
    @Commit
    public void testFindDeletedPhoto(){
        for(ReviewPhotoResponse reviewPhoto : TEST_REVIEW_PHOTOS){
            assertThrows(DataNotFoundException.class,()-> reviewPhotoService.search(reviewPhoto.getSid(),DEFAULT_PAGEABLE));
        }
    }

    @DisplayName("리뷰 테이블 호텔 Id 리뷰 조회 테스트")
    @Test
    @Order(19)
    @Commit
    public void testReviewsInHotel(){
        Page<ReviewResponse> responses = reviewService.reviewsInHotel(1L, DEFAULT_PAGEABLE);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
    }

    @DisplayName("리뷰 테이블 호텔 Id 긍정 리뷰 조회 테스트")
    @Test
    @Order(20)
    @Commit
    public void testPositiveReviewsInHotel(){
        Page<ReviewResponse> responses = reviewService.positiveReviewsInHotel(1L, DEFAULT_PAGEABLE);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
        assertThat(responses.getContent())
                .allMatch(response -> response.getRating() >= 4);
    }

    @DisplayName("리뷰 테이블 호텔 Id 사진 포함 리뷰 조회 테스트")
    @Test
    @Order(21)
    @Commit
    public void testExistsPhotoReviewsInHotel(){
        Page<ReviewResponse> responses = reviewService.existsPhotoReviewsInHotel(1L, DEFAULT_PAGEABLE);
        assertThat(responses.getContent())
                .allMatch(response -> response.getHotelId() == 1L);
        assertThat(responses.getTotalElements())
                .isEqualTo(1);
    }

    @DisplayName("리뷰 테이블 삭제 전 리뷰 답변, 리뷰 사진 저장 테스트")
    @Test
    @Order(22)
    @Commit
    public void testBeforeDeleteReviewInsertAnswerAndPhoto(){
        LocalDateTime now = LocalDateTime.now();
        ReviewAnswerCreateRequest requestAnswer = new ReviewAnswerCreateRequest(
                TEST_REVIEW.getSid(),
                "리뷰 테이블 삭제 전 리뷰 답변 저장 테스트"
        );
        ReviewAnswerResponse insertedAnswer = reviewAnswerService.insertReviewAnswer(requestAnswer);

        assertThat(insertedAnswer)
                .isNotNull();
        assertThat(insertedAnswer.getSid())
                .isNotNull();
        assertThat(insertedAnswer.getReviewId())
                .isEqualTo(TEST_REVIEW.getSid());
        assertThat(insertedAnswer.getReviewAnswer())
                .isEqualTo("리뷰 답변 저장 테스트");
        assertThat(insertedAnswer.getCreatedAt())
                .isNotNull();
        assertThat(insertedAnswer.getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insertedAnswer.getUpdatedAt())
                .isNotNull();
        assertThat(insertedAnswer.getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insertedAnswer.getDeletedAt())
                .isNull();
        assertThat(insertedAnswer.getDeleted())
                .isEqualTo(false);

        TEST_REVIEW_ANSWER = insertedAnswer;

        //저장 전 포인트
        Member member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();
        Integer point = member.getPoint();

        List<MultipartFile> photos = new ArrayList<>();

        MockMultipartFile photo = new MockMultipartFile(
                "file",
                "before_delete_review_insert.jpg",
                "image/jpeg",
                new byte[10]
        );
        photos.add(photo);

        ReviewPhotoCreateRequest requestPhoto = new ReviewPhotoCreateRequest(
                TEST_REVIEW.getSid(),
                photos
        );

        List<ReviewPhotoResponse> insertedPhotos = reviewPhotoService.insertReviewPhotos(requestPhoto);

        assertThat(insertedPhotos)
                .isNotNull();
        assertThat(insertedPhotos.size())
                .isEqualTo(1);
        assertThat(insertedPhotos.getFirst().getOriginalFileName())
                .isEqualTo("before_delete_review_insert.jpg");
        assertThat(insertedPhotos.getFirst().getImagePath().substring(0,16))
                .isEqualTo("/images/reviews/");
        assertThat(insertedPhotos.getFirst().getCreatedAt())
                .isNotNull();
        assertThat(insertedPhotos.getFirst().getCreatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insertedPhotos.getFirst().getUpdatedAt())
                .isNotNull();
        assertThat(insertedPhotos.getFirst().getUpdatedAt())
                .isAfterOrEqualTo(now);
        assertThat(insertedPhotos.getFirst().getDeletedAt())
                .isNull();
        assertThat(insertedPhotos.getFirst().getDeleted())
                .isEqualTo(false);

        //저장 후 포인트
        member = memberService.getMember(1L);
        assertThat(member)
                .isNotNull();

        //여기선 이미 리뷰 사진을 추가, 삭제한 상태에서 새로 사진을 저장하기 때문에 포인트를 지급하면 안됨.
        assertThat(member.getPoint())
                .isEqualTo(point);

        TEST_REVIEW_PHOTOS.clear();
        TEST_REVIEW_PHOTOS.addAll(insertedPhotos);
    }

    @DisplayName("리뷰 테이블 삭제 테스트")
    @Test
    @Order(23)
    @Commit
    public void testDeleteReview() {
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

    @DisplayName("리뷰 테이블 삭제 후 리뷰 답변, 리뷰 사진 검색 테스트")
    @Test
    @Order(24)
    @Commit
    public void testAfterDeleteReviewFindAnswerAndPhoto() {
        assertThrows(DataNotFoundException.class, () -> reviewAnswerService.findByReviewSid(TEST_REVIEW.getSid()));
        assertThrows(DataNotFoundException.class, () -> reviewPhotoService.search(TEST_REVIEW.getSid(), DEFAULT_PAGEABLE));
    }

    @DisplayName("리뷰 테이블 삭제 컬럼 검색 테스트")
    @Test
    @Order(25)
    @Commit
    public void testFindDeletedReview(){
        assertThrows(DataNotFoundException.class, () -> reviewService.findByReviewId(TEST_REVIEW.getSid()));
    }

    private void setCategories(List<ReviewCategoryRequest> categories, Long categoryId, Integer rating) {
        ReviewCategoryRequest category = new ReviewCategoryRequest(categoryId,rating);
        categories.add(category);
    }

    private void setTags(List<ReviewTagRequest> tags , Long tagId) {
        ReviewTagRequest tag = new ReviewTagRequest(tagId);
        tags.add(tag);
    }
}
