package com.mjc.hotel.hotels.service;

import com.mjc.hotel.hotels.dto.HotelWishlistRequestDto;
import com.mjc.hotel.hotels.dto.HotelWishlistResponseDto;
import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelWishlist;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelWishlistRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelWishlistService {

    private final HotelWishlistRepository hotelWishlistRepository;
    private final HotelRepository hotelRepository;
    private final MemberRepository memberRepository;
    private final HotelPhotoRepository hotelPhotoRepository;
    private final HotelService hotelService;

    @Transactional(readOnly = true)
    public List<HotelWishlistResponseDto> findByMember(Long memberId) {
        return hotelWishlistRepository.findActiveByMemberId(memberId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelWishlistResponseDto status(Long memberId, Long hotelId) {
        return HotelWishlistResponseDto.builder()
                .memberId(memberId)
                .hotelId(hotelId)
                .wished(hotelWishlistRepository.existsByMemberSidAndHotelSidAndDeletedFalse(memberId, hotelId))
                .build();
    }

    @Transactional
    public HotelWishlistResponseDto add(HotelWishlistRequestDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("호텔 정보를 찾을 수 없습니다."));

        HotelWishlist wishlist = hotelWishlistRepository.findByMemberSidAndHotelSid(dto.getMemberId(), dto.getHotelId())
                .map(existing -> {
                    existing.setDeleted(false);
                    existing.setDeletedAt(null);
                    return existing;
                })
                .orElseGet(() -> HotelWishlist.builder()
                        .member(member)
                        .hotel(hotel)
                        .build());

        return toDto(hotelWishlistRepository.save(wishlist));
    }

    @Transactional
    public HotelWishlistResponseDto deleteByMemberAndHotel(Long memberId, Long hotelId) {
        HotelWishlist wishlist = hotelWishlistRepository.findByMemberSidAndHotelSid(memberId, hotelId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 정보를 찾을 수 없습니다."));
        wishlist.markDeleted();
        return toDto(wishlist);
    }

    @Transactional
    public HotelWishlistResponseDto deleteById(Long id) {
        HotelWishlist wishlist = hotelWishlistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 정보를 찾을 수 없습니다."));
        wishlist.markDeleted();
        return toDto(wishlist);
    }

    private HotelWishlistResponseDto toDto(HotelWishlist wishlist) {
        Hotel hotel = wishlist.getHotel();
        HotelPhoto photo = hotelPhotoRepository.findRandomPhoto(hotel.getSid());
        Integer maxDiscountRate = 0;
        try {
            maxDiscountRate = hotelService.findById(hotel.getSid()).getMaxDiscountRate();
        } catch (Exception ignored) {
            maxDiscountRate = 0;
        }

        return HotelWishlistResponseDto.builder()
                .sid(wishlist.getSid())
                .memberId(wishlist.getMember().getSid())
                .hotelId(hotel.getSid())
                .hotelName(hotel.getHotelName())
                .typeTitle(hotel.getType() != null ? hotel.getType().getTitle() : null)
                .hotelPrice(hotel.getHotelPrice())
                .location(hotel.getLocation())
                .starRating(hotel.getStarRating())
                .description(hotel.getDescription())
                .maxDiscountRate(maxDiscountRate)
                .imagePath(photo != null ? photo.getImagePath() : null)
                .createdAt(wishlist.getCreatedAt())
                .wished(true)
                .build();
    }
}
