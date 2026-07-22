package com.mjc.hotel.member.withdrawal.service;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberAuthAccount;
import com.mjc.hotel.member.entity.MemberAuthProvider;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberAuthAccountRepository;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.member.repository.MemberTermAgreementRepository;
import com.mjc.hotel.member.withdrawal.exception.WithdrawalConflictException;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalTransactionService {

    private static final List<ReservationStatus> BLOCKING_RESERVATION_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.UPCOMING,
            ReservationStatus.CHECKED_IN
    );

    private final MemberRepository memberRepository;
    private final MemberAuthAccountRepository authAccountRepository;
    private final MemberTermAgreementRepository termAgreementRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public MemberWithdrawalCandidate loadCandidate(String email) {
        Member member = memberRepository.findActiveByEmail(email)
                .filter(value -> value.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new AuthenticationFailedException("로그인할 수 없는 회원입니다."));
        validateNoBlockingReservation(member.getSid());

        List<MemberAuthAccount> activeAccounts = authAccountRepository.findByMember_Sid(member.getSid())
                .stream()
                .filter(account -> !Boolean.TRUE.equals(account.getDeleted()))
                .toList();
        boolean localAccount = activeAccounts.stream()
                .anyMatch(account -> account.getProvider() == MemberAuthProvider.LOCAL);
        String localPasswordHash = activeAccounts.stream()
                .filter(account -> account.getProvider() == MemberAuthProvider.LOCAL)
                .map(MemberAuthAccount::getPasswordHash)
                .findFirst()
                .orElse(null);
        List<MemberAuthProvider> socialProviders = activeAccounts.stream()
                .map(MemberAuthAccount::getProvider)
                .filter(provider -> provider != null && provider != MemberAuthProvider.LOCAL)
                .distinct()
                .toList();

        return new MemberWithdrawalCandidate(
                member.getSid(),
                localAccount,
                localPasswordHash,
                socialProviders
        );
    }

    @Transactional
    public void markWithdrawn(Long memberSid) {
        Member member = memberRepository.findActiveBySid(memberSid)
                .orElseThrow(() -> new AuthenticationFailedException("이미 탈퇴했거나 존재하지 않는 회원입니다."));
        validateNoBlockingReservation(memberSid);

        authAccountRepository.findByMember_Sid(memberSid).forEach(account -> {
            if (!Boolean.TRUE.equals(account.getDeleted())) {
                account.setPasswordHash(null);
                account.setProviderUserId(null);
                account.markDeleted();
            }
        });

        LocalDateTime withdrawnAt = LocalDateTime.now();
        termAgreementRepository.findByMember_Sid(memberSid).forEach(agreement -> {
            if (!Boolean.TRUE.equals(agreement.getDeleted())) {
                agreement.setWithdrawnAt(withdrawnAt);
                agreement.markDeleted();
            }
        });

        member.setStatus(MemberStatus.DELETED);
        member.setName("탈퇴회원");
        member.setPhone(null);
        member.setEmail(anonymizedEmail(memberSid));
        member.setEmailVerified(false);
        member.setPoint(0);
        member.markDeleted();
    }

    private void validateNoBlockingReservation(Long memberSid) {
        if (reservationRepository.existsByMember_SidAndReservationStatusIn(
                memberSid,
                BLOCKING_RESERVATION_STATUSES
        )) {
            throw new WithdrawalConflictException(
                    "진행 중인 예약이 있어 탈퇴할 수 없습니다. 예약을 취소하거나 이용 완료 후 다시 시도해 주세요."
            );
        }
    }

    private String anonymizedEmail(Long memberSid) {
        return "withdrawn-" + memberSid + "-" + UUID.randomUUID() + "@deleted.invalid";
    }
}
