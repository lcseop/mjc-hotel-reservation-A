package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.*;
import com.mjc.hotel.hotels.entity.*;
import com.mjc.hotel.hotels.mapper.HotelMapper;
import com.mjc.hotel.hotels.repository.*;
import com.mjc.hotel.promotion.entity.ConditionType;
import com.mjc.hotel.promotion.entity.Promotion;
import com.mjc.hotel.promotion.repository.PromotionRepository;
import com.mjc.hotel.promotion.service.PromotionDiscountCalculator;
import com.mjc.hotel.review.entity.Review;
import com.mjc.hotel.review.repository.ReviewCategoryRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import com.mjc.hotel.review.repository.ReviewTagRepository;
import com.mjc.hotel.review.response.ReviewCategoryResponse;
import com.mjc.hotel.review.response.ReviewResponse;
import com.mjc.hotel.review.response.ReviewTagResponse;
import com.mjc.hotel.room.dto.RoomResponseNoHotelDto;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.util.ResponseCode;
import com.mjc.hotel.util.excep.DataNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {
    private final HotelRepository hotelRepository;
    private final HotelPhotoRepository hotelPhotoRepository;
    private final HotelTypeRepository hotelTypeRepository;
    private final HotelInAmenitiesRepository hotelInAmenitiesRepository;
    private final HotelAmenitiesRepository hotelAmenitiesRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCategoryRepository reviewCategoryRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final JPAQueryFactory queryFactory;
    private final PromotionRepository promotionRepository;

    @Transactional
    public HotelResponseDto insert(HotelRequestDto hotel) {
        HotelType type = hotelTypeRepository.findById(hotel.getTypeId()).orElseThrow();
        Hotel insert = HotelMapper.clone(null, hotel, false, type);
        Hotel saved = hotelRepository.save(insert);

        return HotelMapper.response(saved);
    }

    @Transactional
    public HotelResponseDto update(HotelRequestDto hotel) {
        Hotel origin = hotelRepository.findById(hotel.getSid()).orElseThrow();

        if (origin.getDeleted() != null && origin.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, "data not found");
        }

        HotelType type = hotelTypeRepository.findById(hotel.getTypeId()).orElseThrow();
        Hotel update = HotelMapper.clone(origin, hotel, true, type);
        Hotel saved = hotelRepository.save(update);

        List<HotelPhotoDto> photos = hotelPhotoRepository.findByHotelSid(hotel.getSid())
                .stream()
                .map(h -> HotelPhotoDto
                        .builder()
                        .sid(h.getSid())
                        .hotelId(h.getHotel().getSid())
                        .imagePath(h.getImagePath())
                        .build()
                )
                .toList();

        return HotelMapper.response(saved);
    }

    @Transactional
    public HotelResponseDto delete(Long id) {
        Hotel target = hotelRepository.findById(id).orElseThrow();

        if (target.getDeleted() != null && target.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, target.getHotelName() + " is not found");
        }

        hotelInAmenitiesRepository.deleteByHotelSid(id);

        target.setDeleted(true);
        target.setDeletedAt(LocalDateTime.now());

        Hotel saved = hotelRepository.save(target);

        return HotelMapper.response(saved);
    }

    public Page<HotelResponseDto> search(HotelSearchRequestDto dto, Pageable pageable) {
        Page<HotelResponseDto> page = hotelRepository.search(dto, pageable);
        page.getContent().forEach(hotel -> {
            int maxDiscountRate = Math.max(
                    hotel.getMaxDiscountRate() != null ? hotel.getMaxDiscountRate() : 0,
                    calculateMaxDiscountRate(hotel.getSid())
            );
            hotel.setMaxDiscountRate(maxDiscountRate);
            hotel.setAmenities(findHotelInAmenities(hotel.getSid()));
        });
        return page;
    }

    public List<HotelResponseDto> findAllForAdmin() {
        return hotelRepository.findAll().stream()
                .filter(hotel -> !Boolean.TRUE.equals(hotel.getDeleted()))
                .map(hotel -> HotelMapper.response(hotel))
                .toList();
    }

    public HotelResponseDto findById(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();

        if (hotel.getDeleted() != null && hotel.getDeleted()) {
            throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND_ERROR, hotel.getHotelName() + " is not found");
        }

        return HotelMapper.response(hotel);
    }


    public List<HotelAmenitiesDto> findHotelInAmenities(Long hotelId) {
        List<HotelInAmenities> inAmenities = hotelInAmenitiesRepository.findByHotelSid(hotelId);
        return inAmenities.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .map(r -> {
                    HotelAmenities h = hotelAmenitiesRepository.findById(r.getAmenities().getSid()).orElseThrow();
                    return HotelAmenitiesDto.builder().sid(h.getSid()).title(h.getTitle()).description(h.getDescription()).build();
                })
                .toList();
    }

    public List<RoomResponseNoHotelDto> findHotelInRooms(Long hotelId) {
        List<Room> inRooms = roomRepository.findByHotelIdSid(hotelId);
        return inRooms.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .map(r -> {
                    RoomResponseNoHotelDto dto = RoomResponseNoHotelDto
                                .builder()
                                .sid(r.getSid())
                                .roomTypeId(r.getRoomTypeId().getSid())
                                .roomTypeTitle(r.getRoomTypeId().getTitle())
                                .roomName(r.getRoomName())
                                .roomPrice(r.getRoomPrice())
                                .roomAvailable(r.getRoomAvailable() == null || r.getRoomAvailable())
                                .roomNumber(r.getRoomNumber())
                                .floor(r.getFloor())
                                .area(r.getArea())
                                .maximumPeople(r.getMaximumPeople())
                                .checkInTime(r.getCheckInTime())
                                .checkOutTime(r.getCheckOutTime())
                                .pet(r.getPet())
                                .parking(r.getParking())
                                .smoke(r.getSmoke())
                                .idCard(r.getIdCard())
                                .build();
                    applyPromotion(dto, r);
                    return dto;
                })
                .toList();
    }

    private void applyPromotion(RoomResponseNoHotelDto dto, Room room) {
        Promotion promotion = findBestPromotion(room);
        String discountContent = promotion != null ? promotion.getDiscountContent() : null;
        dto.setPromotionDiscountContent(discountContent);
        dto.setPromotionDiscountRate(PromotionDiscountCalculator.extractDiscountRate(discountContent));
        dto.setPromotionDiscountAmount(PromotionDiscountCalculator.calculateDiscountAmount(room.getRoomPrice(), discountContent));
        dto.setDiscountedRoomPrice(PromotionDiscountCalculator.calculateDiscountedPrice(room.getRoomPrice(), discountContent));
    }

    private Promotion findBestPromotion(Room room) {
        if (room.getRoomTypeId() == null || room.getRoomTypeId().getSid() == null) {
            return null;
        }

        return PromotionDiscountCalculator.findBestPromotion(
                promotionRepository.findActivePromotionsByRoomType(
                        room.getRoomTypeId().getSid(),
                        ConditionType.ACTIVE,
                        LocalDateTime.now()
                ),
                room.getRoomPrice()
        );
    }

    private int calculateMaxDiscountRate(Long hotelId) {
        return roomRepository.findByHotelIdSid(hotelId).stream()
                .filter(room -> !Boolean.TRUE.equals(room.getDeleted()))
                .map(this::findBestPromotion)
                .filter(Objects::nonNull)
                .mapToInt(promotion -> PromotionDiscountCalculator.extractDiscountRate(promotion.getDiscountContent()))
                .max()
                .orElse(0);
    }

    public List<HotelPopularResponseDto> findPopularHotels() {
        List<Hotel> populars = hotelRepository.findTop4Popular();
        return populars.stream()
                .filter(h -> !Boolean.TRUE.equals(h.getDeleted()))
                .map(h -> {
                    HotelPhoto photo = hotelPhotoRepository.findRandomPhoto(h.getSid());
                    String imagePath = (photo == null || photo.getImagePath() == null) ? "https://gunsancci.korcham.net/images/no-image01.gif" : photo.getImagePath();
                    Double rating = reviewRepository.findByHotelSidAndDeletedFalse(h.getSid(), Pageable.unpaged())
                            .stream()
                            .filter(review -> review.getRating() != null)
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0d);
                    return HotelPopularResponseDto.builder()
                            .sid(h.getSid())
                            .hotelName(h.getHotelName())
                            .location(h.getLocation())
                            .firstImage(imagePath)
                            .rating(Math.round(rating * 10) / 10.0)
                            .price(h.getHotelPrice())
                            .build();
                })
                .toList();
    }

    public Page<ReviewResponse> findHotelInReviews(Long hotelId, Pageable pageable) {
        Page<Review> inReviews = reviewRepository.findByHotelSid(hotelId, pageable);
        List<ReviewResponse> review = inReviews.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .map(r -> {
                    List<ReviewCategoryResponse> categories = reviewCategoryRepository.findByReviewSid(r.getSid())
                            .stream()
                            .map(category -> ReviewCategoryResponse
                                    .builder()
                                    .sid(category.getSid())
                                    .reviewId(category.getReview().getSid())
                                    .reviewCategoryId(category.getReviewCategoryMaster().getSid())
                                    .rating(category.getRating())
                                    .build()
                            )
                            .toList();
                    List<ReviewTagResponse> tags = reviewTagRepository.findByReviewSid(r.getSid())
                            .stream()
                            .map(tag -> ReviewTagResponse
                                    .builder()
                                    .reviewId(tag.getReview().getSid())
                                    .reviewTagId(tag.getReviewTagMaster().getSid())
                                    .build()
                            )
                            .toList();
                    return ReviewResponse
                                    .builder()
                                    .sid(r.getSid())
                                    .hotelId(r.getHotel().getSid())
                                    .memberId(r.getMember().getSid())
                                    .reservationId(r.getReservation().getSid())
                                    .rating(r.getRating())
                                    .travelType(r.getTravelType())
                                    .content(r.getContent())
                                    .likeCount(r.getLikeCount())
                                    .dislikeCount(r.getDislikeCount())
                                    .categories(categories)
                                    .tags(tags)
                                    .createdAt(r.getCreatedAt())
                                    .updatedAt(r.getUpdatedAt())
                                    .deletedAt(r.getDeletedAt())
                                    .deleted(r.getDeleted())
                                    .build();
                        }
                )
                .toList();
        return new PageImpl<>(review, pageable, inReviews.getTotalElements());
    }

    public List<HotelPhotoDto> findHotelInPhotos(Long hotelId) {
        List<HotelPhoto> photos = hotelPhotoRepository.findByHotelSid(hotelId);
        return photos.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(p -> HotelPhotoDto.builder()
                        .sid(p.getSid())
                        .hotelId(p.getHotel().getSid())
                        .imagePath(p.getImagePath())
                        .build())
                .toList();

    }
}
