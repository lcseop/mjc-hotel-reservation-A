package com.mjc.hotel.member.withdrawal.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.entity.MemberTermAgreement;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalTransactionServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberAuthAccountRepository authAccountRepository;
    @Mock
    private MemberTermAgreementRepository termAgreementRepository;
    @Mock
    private ReservationRepository reservationRepository;

    private MemberWithdrawalTransactionService service;

    @BeforeEach
    void setUp() {
        service = new MemberWithdrawalTransactionService(
                memberRepository,
                authAccountRepository,
                termAgreementRepository,
                reservationRepository
        );
    }

    @Test
    void marksMemberAccountsAndAgreementsAsWithdrawn() {
        Member member = activeMember();
        MemberAuthAccount account = MemberAuthAccount.builder()
                .sid(10L)
                .member(member)
                .provider(MemberAuthProvider.NAVER)
                .providerUserId("naver-id")
                .build();
        MemberTermAgreement agreement = MemberTermAgreement.builder()
                .sid(20L)
                .member(member)
                .isAgreed(true)
                .build();
        when(memberRepository.findActiveBySid(1L)).thenReturn(Optional.of(member));
        when(authAccountRepository.findByMember_Sid(1L)).thenReturn(List.of(account));
        when(termAgreementRepository.findByMember_Sid(1L)).thenReturn(List.of(agreement));

        service.markWithdrawn(1L);

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(member.getDeleted()).isTrue();
        assertThat(member.getEmail()).startsWith("withdrawn-1-");
        assertThat(member.getName()).isEqualTo("탈퇴회원");
        assertThat(member.getPoint()).isZero();
        assertThat(account.getDeleted()).isTrue();
        assertThat(account.getProviderUserId()).isNull();
        assertThat(agreement.getDeleted()).isTrue();
        assertThat(agreement.getWithdrawnAt()).isNotNull();
    }

    @Test
    void rejectsWithdrawalWhenActiveReservationExists() {
        when(memberRepository.findActiveByEmail("member@example.com"))
                .thenReturn(Optional.of(activeMember()));
        when(reservationRepository.existsByMember_SidAndReservationStatusIn(
                org.mockito.ArgumentMatchers.eq(1L),
                anyList()
        )).thenReturn(true);

        assertThatThrownBy(() -> service.loadCandidate("member@example.com"))
                .isInstanceOf(WithdrawalConflictException.class)
                .hasMessageContaining("진행 중인 예약");
    }

    private Member activeMember() {
        return Member.builder()
                .sid(1L)
                .name("회원")
                .email("member@example.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .point(5000)
                .build();
    }
}
