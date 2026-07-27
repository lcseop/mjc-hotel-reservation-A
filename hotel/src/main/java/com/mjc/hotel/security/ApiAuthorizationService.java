package com.mjc.hotel.security;

import com.mjc.hotel.hotels.repository.HotelWishlistRepository;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.review.repository.ReviewPhotoRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("apiAuthorization")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiAuthorizationService {

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository memberAuthAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentsRepository paymentsRepository;
    private final HotelWishlistRepository hotelWishlistRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;

    public boolean isSelf(Authentication authentication, Long memberId) {
        if (!isAuthenticated(authentication) || memberId == null) {
            return false;
        }

        return memberRepository.findActiveByEmail(authentication.getName())
                .map(member -> memberId.equals(member.getSid()))
                .orElse(false);
    }

    public boolean isSelfOrAdmin(Authentication authentication, Long memberId) {
        return isAdmin(authentication) || isSelf(authentication, memberId);
    }

    public boolean canSearchReservations(Authentication authentication, Long memberId) {
        return isAdmin(authentication) || isSelf(authentication, memberId);
    }

    public boolean ownsReservation(Authentication authentication, Long reservationId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || reservationId == null) {
            return false;
        }

        return reservationRepository.findById(reservationId)
                .map(reservation -> isSelf(authentication, reservation.getMember().getSid()))
                .orElse(false);
    }

    public boolean ownsPaymentOrder(Authentication authentication, String orderId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || orderId == null || orderId.isBlank()) {
            return false;
        }

        return paymentsRepository.findByOrderId(orderId)
                .map(payment -> isSelf(authentication, payment.getMember().getSid()))
                .orElse(false);
    }

    public boolean ownsWishlist(Authentication authentication, Long wishlistId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || wishlistId == null) {
            return false;
        }

        return hotelWishlistRepository.findById(wishlistId)
                .map(wishlist -> isSelf(authentication, wishlist.getMember().getSid()))
                .orElse(false);
    }

    public boolean ownsReview(Authentication authentication, Long reviewId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || reviewId == null) {
            return false;
        }

        return reviewRepository.findById(reviewId)
                .map(review -> isSelf(authentication, review.getMember().getSid()))
                .orElse(false);
    }

    public boolean ownsReviewPhoto(Authentication authentication, Long photoId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || photoId == null) {
            return false;
        }

        return reviewPhotoRepository.findById(photoId)
                .map(photo -> ownsReview(authentication, photo.getReview().getSid()))
                .orElse(false);
    }

    public boolean ownsAuthAccount(Authentication authentication, Long authAccountId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || authAccountId == null) {
            return false;
        }

        return memberAuthAccountRepository.findById(authAccountId)
                .map(account -> isSelf(authentication, account.getMember().getSid()))
                .orElse(false);
    }

    public boolean ownsTermAgreement(Authentication authentication, Long agreementId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!isAuthenticated(authentication) || agreementId == null) {
            return false;
        }

        return memberTermAgreementRepository.findById(agreementId)
                .map(agreement -> isSelf(authentication, agreement.getMember().getSid()))
                .orElse(false);
    }

    public boolean isAdmin(Authentication authentication) {
        return isAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}
