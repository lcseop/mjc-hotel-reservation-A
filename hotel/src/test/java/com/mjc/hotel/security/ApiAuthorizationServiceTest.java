package com.mjc.hotel.security;

import com.mjc.hotel.hotels.repository.HotelWishlistRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.review.repository.ReviewPhotoRepository;
import com.mjc.hotel.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiAuthorizationServiceTest {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ApiAuthorizationService authorizationService = new ApiAuthorizationService(
            memberRepository,
            mock(MemberAuthAccountRepository.class),
            mock(MemberTermAgreementRepository.class),
            reservationRepository,
            mock(PaymentsRepository.class),
            mock(HotelWishlistRepository.class),
            mock(ReviewRepository.class),
            mock(ReviewPhotoRepository.class)
    );

    @Test
    void userCanAccessOnlyOwnMemberResources() {
        Authentication user = authentication("user@example.com", "ROLE_USER");
        when(memberRepository.findActiveByEmail("user@example.com"))
                .thenReturn(Optional.of(Member.builder().sid(10L).email("user@example.com").build()));

        assertThat(authorizationService.isSelfOrAdmin(user, 10L)).isTrue();
        assertThat(authorizationService.isSelfOrAdmin(user, 11L)).isFalse();
    }

    @Test
    void adminCanAccessAnotherMembersResources() {
        Authentication admin = authentication("admin@example.com", "ROLE_ADMIN");

        assertThat(authorizationService.isSelfOrAdmin(admin, 99L)).isTrue();
        assertThat(authorizationService.canSearchReservations(admin, null)).isTrue();
    }

    @Test
    void userCanAccessOnlyOwnReservation() {
        Authentication user = authentication("user@example.com", "ROLE_USER");
        Member owner = Member.builder().sid(10L).email("user@example.com").build();
        Member anotherMember = Member.builder().sid(11L).email("other@example.com").build();
        when(memberRepository.findActiveByEmail("user@example.com")).thenReturn(Optional.of(owner));
        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(Reservation.builder().sid(100L).member(owner).build()));
        when(reservationRepository.findById(200L))
                .thenReturn(Optional.of(Reservation.builder().sid(200L).member(anotherMember).build()));

        assertThat(authorizationService.ownsReservation(user, 100L)).isTrue();
        assertThat(authorizationService.ownsReservation(user, 200L)).isFalse();
    }

    private Authentication authentication(String email, String authority) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
