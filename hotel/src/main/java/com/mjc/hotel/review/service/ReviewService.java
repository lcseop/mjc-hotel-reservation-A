package com.mjc.hotel.review.service;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.reservations.dto.ReservationResponseDto;
import com.mjc.hotel.reservations.entity.PointHistory;
import com.mjc.hotel.reservations.entity.PointStatus;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.PointHistoryRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.reservations.service.ReservationService;
import com.mjc.hotel.review.entity.*;
import com.mjc.hotel.review.repository.*;
import com.mjc.hotel.review.request.*;
import com.mjc.hotel.review.response.ReviewCategoryResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.response.ReviewTagResponse;
import com.mjc.hotel.review.response.ReviewWriteStatusResponse;
import com.mjc.hotel.room.dto.RoomResponseDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.service.RoomService;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final ReviewCategoryMasterRepository reviewCategoryMasterRepository;
    private final ReviewCategoryRepository reviewCategoryRepository;

    private final ReviewTagMasterRepository reviewTagMasterRepository;
    private final ReviewTagRepository reviewTagRepository;

    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;

    private final HotelRepository hotelRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private final RoomService roomService;
    private final ReservationService reservationService;

    @Transactional
    public ReviewResponse insertReview(ReviewCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "Hotel Not Found"));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "Member Not Found"));
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "Reservation Not Found"));

        if (reservation.getReservationStatus() != ReservationStatus.CHECKED_OUT) {
            throw new IllegalStateException("CHECKED_OUT 예약만 리뷰를 작성할 수 있습니다.");
        }

        if (!reservation.getMember().getSid().equals(member.getSid())) {
            throw new IllegalStateException("본인의 예약에만 리뷰를 작성할 수 있습니다.");
        }

        if (!reservation.getRoom().getHotelId().getSid().equals(hotel.getSid())) {
            throw new IllegalStateException("예약한 호텔에만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByReservationSid(reservation.getSid())) {
            throw new IllegalStateException("이미 리뷰를 작성한 예약입니다.");
        }

        //request로 빌더 패턴 사용
        Review review = Review.builder()
                .hotel(hotel)
                .member(member)
                .reservation(reservation)
                .rating(request.getRating())
                .travelType(request.getTravelType())
                .content(request.getContent())
                .likeCount(0)
                .dislikeCount(0)
                .build();

        review.prePersist();

        Review save = reviewRepository.save(review);
        List<ReviewCategory> categories = this.insertReviewCategories(request.getCategories(), save);
        List<ReviewTag> tags = this.insertReviewTags(request.getTags(), save);

        int accumulationPoint = save.getContent().length() < 50 ? 100 : 200;
        member.setPoint(member.getPoint() + accumulationPoint);
        Member updatedMember = memberRepository.save(member);

        PointHistory pointHistory = PointHistory.builder()
                .reservation(reservation)
                .member(updatedMember)
                .amount(accumulationPoint)
                .pointStatus(PointStatus.ACCUMULATION)
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(pointHistory);

        ReviewResponse result = this.toReviewResponse(save,categories, tags);

        return result;
    }
    @Transactional
    public ReviewResponse updateReview(ReviewUpdateRequest request) {
        Review find = reviewRepository.findBySidAndDeletedFalse(request.getSid());
        if(find == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        Review review = Review.builder()
                .sid(find.getSid())
                .hotel(find.getHotel())
                .member(find.getMember())
                .reservation(find.getReservation())
                .rating(request.getRating())
                .travelType(request.getTravelType())
                .content(request.getContent())
                .likeCount(find.getLikeCount())
                .dislikeCount(find.getDislikeCount())
                .build();
        //생성시간 그대로 넘겨주기
        review.setCreatedAt(find.getCreatedAt());
        review.setUpdatedAt(find.getUpdatedAt());
        review.prePersist();

        Review updateAfter = reviewRepository.save(review);

        //기존 리뷰에 있던 항목별 평점(청결도, 서비스 등등)을 리뷰ID로 삭제
        //ex) 사용자가 청결도 리뷰를 삭제하고 위치 리뷰를 추가하는 식으로 리뷰를 수정할 수 있음 그러면 기존 리뷰에 딸린 항목별 리뷰를 삭제하고 새로 넣는게 좋다고 봄.
        reviewCategoryRepository.deleteByReviewSid(updateAfter.getSid());
        //마찬가지로 기존 리뷰에 딸린 장단점 항목을 삭제하고 새로 넣음
        reviewTagRepository.deleteByReviewSid(updateAfter.getSid());

        List<ReviewCategory> categories = this.insertReviewCategories(request.getCategories(), updateAfter);
        List<ReviewTag> tags = this.insertReviewTags(request.getTags(), updateAfter);

        ReviewResponse result = this.toReviewResponse(updateAfter,categories, tags);

        return result;
    }
    @Transactional
    public ReviewResponse findByReviewId(Long reviewId) {
        Review review = reviewRepository.findBySidAndDeletedFalse(reviewId);
        if(review == null){
            throw new  DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }
        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(review.getSid());
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(review.getSid());

        ReviewResponse result = this.toReviewResponse(review,categories,tags);

        return result;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> reviewsByMember(Long memberId, Pageable pageable) {
        return reviewRepository.findByMemberSidAndDeletedFalse(memberId, pageable)
                .map(review -> {
                    List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(review.getSid());
                    List<ReviewTag> tags = reviewTagRepository.findByReviewSid(review.getSid());
                    return this.toReviewResponse(review, categories, tags);
                });
    }

    @Transactional
    public ReviewResponse deleteReviewId(Long sid) {
        Review find = reviewRepository.findBySidAndDeletedFalse(sid);
        if(find == null){
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Review Not Found");
        }

        find.markDeleted();
        Review save = reviewRepository.save(find);

        List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(find.getSid());
        List<ReviewTag> tags = reviewTagRepository.findByReviewSid(find.getSid());

        List<ReviewPhoto> photos = reviewPhotoRepository.findAllByReviewSidAndDeletedFalse(find.getSid());
        if(photos != null && !photos.isEmpty()){
            for(ReviewPhoto photo : photos) {
                photo.markDeleted();
                reviewPhotoRepository.save(photo);
            }
        }
        ReviewAnswer answer = reviewAnswerRepository.findByReviewSidAndDeletedFalse(find.getSid());
        if(answer != null){
            answer.markDeleted();
            reviewAnswerRepository.save(answer);
        }

        ReviewResponse result = toReviewResponse(save,categories,tags);
        return result;
    }

    private ReviewResponse toReviewResponse(Review review, List<ReviewCategory> categories, List<ReviewTag> tags) {
        return  ReviewResponse.builder()
                .sid(review.getSid())
                .hotelId(review.getHotel().getSid())
                .hotelName(review.getHotel().getHotelName())
                .hotelLocation(review.getHotel().getLocation())
                .hotelStarRating(review.getHotel().getStarRating())
                .memberId(review.getMember().getSid())
                .reservationId(review.getReservation().getSid())
                .rating(review.getRating())
                .travelType(review.getTravelType())
                .content(review.getContent())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .roomName(review.getReservation().getRoom().getRoomName())
                .totalNights(review.getReservation().getTotalNights())
                .categories(
                        categories.stream()
                                .map(reviewCategory -> ReviewCategoryResponse.builder()
                                        .sid(reviewCategory.getSid())
                                        .reviewCategoryId(reviewCategory.getReviewCategoryMaster().getSid())
                                        .reviewId(reviewCategory.getReview().getSid())
                                        .rating(reviewCategory.getRating())
                                        .build())
                                .toList()
                )
                .tags(
                        tags.stream()
                                .map(reviewTag -> ReviewTagResponse.builder()
                                        .reviewId(reviewTag.getReview().getSid())
                                        .reviewTagId(reviewTag.getReviewTagMaster().getSid())
                                        .reviewTagName(reviewTag.getReviewTagMaster().getReviewTagName())
                                        .reviewTagCategory(reviewTag.getReviewTagMaster().getReviewTagCategory())
                                        .build()
                                )
                                .toList()
                )
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .deletedAt(review.getDeletedAt())
                .deleted(review.getDeleted())
                .build();
    }

    private List<ReviewCategory> insertReviewCategories(List<ReviewCategoryRequest> categories, Review review) {
        List<ReviewCategory> results = new ArrayList<>();
        if(categories != null && !categories.isEmpty()){

            for(ReviewCategoryRequest category : categories){
                ReviewCategoryMaster reviewCategoryMaster = reviewCategoryMasterRepository.findById(category.getCategoryId())
                        .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Category Not Exist"));

                ReviewCategory reviewCategory = ReviewCategory.builder()
                        .review(review)
                        .reviewCategoryMaster(reviewCategoryMaster)
                        .rating(category.getRating())
                        .build();

                ReviewCategory result = reviewCategoryRepository.save(reviewCategory);

                results.add(result);
            }
        }

        return results;
    }

    private List<ReviewTag> insertReviewTags(List<ReviewTagRequest> tags , Review review) {
        List<ReviewTag> results = new ArrayList<>();
        if(tags != null && !tags.isEmpty()) {
            for(ReviewTagRequest tag : tags) {
                ReviewTagMaster reviewTagMaster = reviewTagMasterRepository.findById(tag.getTagId())
                        .orElseThrow(() -> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Tag Not Exist"));

                ReviewTag reviewTag = ReviewTag.builder()
                        .review(review)
                        .reviewTagMaster(reviewTagMaster)
                        .build();

                ReviewTag result = reviewTagRepository.save(reviewTag);

                results.add(result);
            }
        }
        return results;
    }

    /**
     호텔에 달린 리뷰를 정렬 조건에 맞춰 찾는 메소드
     @param hotelId 호텔Id
     @param pageable 정렬 조건
     *
     **/
    @Transactional
    public Page<ReviewResponse> reviewsInHotel(Long hotelId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByHotelSidAndDeletedFalse(hotelId, pageable);
        Page<ReviewResponse> responses = this.toPageReviewResponse(pageable, reviews);
        return responses;
    }
    /**
    호텔에 달린 긍정 리뷰만 정렬 조건에 맞춰 찾는 메소드 (평점 4점이상)
     @param hotelId 호텔Id
     @param pageable 정렬 조건
    *
     **/
    @Transactional
    public Page<ReviewResponse> positiveReviewsInHotel(Long hotelId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByHotelSidAndRatingGreaterThanEqualAndDeletedFalse(hotelId, 4, pageable);
        Page<ReviewResponse> responses = this.toPageReviewResponse(pageable, reviews);
        return responses;
    }
    /**
     호텔에 달린 리뷰중 사진이 있는 리뷰를 정렬 조건에 맞춰 정렬하여 찾는 메소드
     @param hotelId 호텔Id
     @param pageable 정렬 조건
     *
     **/
    @Transactional
    public Page<ReviewResponse> existsPhotoReviewsInHotel(Long hotelId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByHotelSidAndExistsPhotoAndDeletedFalse(hotelId, pageable);
        Page<ReviewResponse> responses = this.toPageReviewResponse(pageable, reviews);
        return responses;
    }

    private Page<ReviewResponse> toPageReviewResponse(Pageable pageable, Page<Review> reviews) {
        List<ReviewResponse> results = reviews.stream()
                .map(review -> {
                    List<ReviewCategory> categories = reviewCategoryRepository.findByReviewSid(review.getSid());
                    List<ReviewTag> tags = reviewTagRepository.findByReviewSid(review.getSid());
                    return this.toReviewResponse(review, categories, tags);
                })
                .toList();
        return new PageImpl<>(results, pageable, reviews.getTotalElements());
    }

//    @Transactional
//    public ReviewWriteStatusResponse memberStatus(ReviewWriteStatusRequest request) {
//        Room room = roomRepository.findById(request.getRoomId())
//                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR,"Room Not Exist"));
//        Member member = memberRepository.findById(request.getMemberId())
//                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "Member Not Found"));
//        Hotel hotel = hotelRepository.findById(room.getHotelId().getSid())
//                .orElseThrow(()-> new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "Hotel Not Found"));
//
//        Reservation reservation = reservationRepository.findFirstByMemberSidAndRoomSidAndReservationStatusEqualCHECKED_OUTAndDeletedFalse(member.getSid(),room.getSid());
//        if(reservation == null) {
//            return this.toReviewWriteStatusResponse(false,null,false,null);
//        }
//        Review review = reviewRepository.findByReservationSidAndDeletedFalse(reservation.getSid());
//        if(review == null){
//            return this.toReviewWriteStatusResponse(true,reservation.getSid(),false,null);
//        }
//        return this.toReviewWriteStatusResponse(true,reservation.getSid(),true,review.getSid());
//    }

    private ReviewWriteStatusResponse toReviewWriteStatusResponse(Boolean checked, Long reservationId, Boolean existsReview, Long reviewId) {
        return ReviewWriteStatusResponse.builder()
                .checked(checked)
                .reservationId(reservationId)
                .existsReview(existsReview)
                .reviewId(reviewId)
                .build();
    }
}
