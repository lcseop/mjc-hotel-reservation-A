package com.mjc.hotel.refunds;

import com.mjc.hotel.hotels.entity.Hotel;
import com.mjc.hotel.hotels.entity.HotelAmenities;
import com.mjc.hotel.hotels.entity.HotelPhoto;
import com.mjc.hotel.hotels.entity.HotelType;
import com.mjc.hotel.hotels.repository.HotelAmenitiesRepository;
import com.mjc.hotel.hotels.repository.HotelPhotoRepository;
import com.mjc.hotel.hotels.repository.HotelRepository;
import com.mjc.hotel.hotels.repository.HotelTypeRepository;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.payments.entity.PaymentMethod;
import com.mjc.hotel.payments.entity.PaymentStatus;
import com.mjc.hotel.payments.entity.Payments;
import com.mjc.hotel.payments.repository.PaymentsRepository;
import com.mjc.hotel.refunds.entity.RefundStatus;
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.repository.RefundsRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomIdCardEnum;
import com.mjc.hotel.room.entity.RoomPetAndSmokeEnum;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
public class RefundServiceTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private RefundsRepository refundsRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("refundTestData")
    @Test
    @Commit
    @Transactional
    public void addRefundTest() {
        Member member = memberRepository.findById(1L).orElseThrow();

        Payments payments = paymentsRepository.findById(1L).orElseThrow();

        Refunds refunds = Refunds
                .builder()
                .payment(payments)
                .member(member)
                .pgTransactionKey("PG-REFUND-TEST-20260625-001")
                .idempotencyKey("IDEMPOTENCY-TEST-20260625-001")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 부분 환불")
                .status(RefundStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .build();

        refundsRepository.save(refunds);
    }
}
