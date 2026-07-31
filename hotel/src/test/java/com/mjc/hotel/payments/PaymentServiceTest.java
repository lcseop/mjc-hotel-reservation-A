package com.mjc.hotel.payments;

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
public class PaymentServiceTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("paymentTestData")
    @Test
    @Commit
    @Transactional
    public void addPaymentTest() {
        Member member = memberRepository.findById(1L).orElseThrow();
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Payments payments = Payments
                .builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionNo("TXN-TEST-20260625-001")
                .paidAt(LocalDateTime.now())
                .point(1800)
                .build();

        paymentsRepository.save(payments);
    }
}
