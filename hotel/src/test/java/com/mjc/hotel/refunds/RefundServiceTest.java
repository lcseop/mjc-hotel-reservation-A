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
import com.mjc.hotel.refunds.entity.Refunds;
import com.mjc.hotel.refunds.repository.RefundsRepository;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationStatus;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.room.entity.Room;
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

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
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelAmenitiesRepository hotelAmenitiesRepository;
    @Autowired
    private HotelPhotoRepository hotelPhotoRepository;
    @Autowired
    private HotelTypeRepository hotelTypeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomPhotoRepository roomPhotoRepository;
    @Autowired
    private RoomTagRepository roomTagRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @DisplayName("refundTestData")
    @Test
    @Commit
    @Transactional
    public void addRefundTest() {
        Member member = memberRepository.save(Member
                .builder()
                .name("환불 테스트 회원")
                .phone("010-3333-4444")
                .email("refund-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        HotelAmenities hotelAmenities = hotelAmenitiesRepository.save(HotelAmenities
                .builder()
                .title("조식")
                .description("테스트 조식 제공")
                .build());

        HotelPhoto hotelPhoto = hotelPhotoRepository.save(HotelPhoto
                .builder()
                .imagePath("https://example.com/refund-hotel.jpg")
                .build());

        HotelType hotelType = hotelTypeRepository.save(HotelType
                .builder()
                .title("호텔")
                .build());

        Hotel hotel = hotelRepository.save(Hotel
                .builder()
                .type(hotelType)
                .photo(hotelPhoto)
                .amenities(hotelAmenities)
                .hotelName("환불 테스트 호텔")
                .hotelPrice(180000)
                .location("부산시 테스트구")
                .starRating(5)
                .description("환불 테스트용 호텔")
                .build());

        RoomPhoto roomPhoto = roomPhotoRepository.save(RoomPhoto
                .builder()
                .imagePath("https://example.com/refund-room.jpg")
                .build());

        RoomTag roomTag = roomTagRepository.save(RoomTag
                .builder()
                .title("오션뷰")
                .build());

        RoomType roomType = roomTypeRepository.save(RoomType
                .builder()
                .title("디럭스")
                .build());

        Room room = roomRepository.save(Room
                .builder()
                .hotelId(hotel)
                .roomTagId(roomTag)
                .roomPhotoId(roomPhoto)
                .roomTypeId(roomType)
                .roomName("환불 테스트 객실")
                .roomPrice(180000)
                .roomNumber(901)
                .floor(9)
                .area(35)
                .maximumPeople(3)
                .build());

        Reservation reservation = reservationRepository.save(Reservation
                .builder()
                .member(member)
                .room(room)
                .reservationNumber("REFUND-RESERVATION-TEST-001")
                .checkInDate(LocalDateTime.now().plusDays(2))
                .checkOutDate(LocalDateTime.now().plusDays(4))
                .adults(2)
                .children(1)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(180000)
                .specialRequests("환불 테스트 요청")
                .checkInQr("REFUND-QR-TEST")
                .totalNights(2)
                .guestName("환불 테스트 회원")
                .build());

        Payments payments = Payments
                .builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PARTIALLY_REFUNDED)
                .transactionNo("TXN-TEST-20260625-REFUND-001")
                .paidAt(LocalDateTime.now())
                .point(1800)
                .build();

        paymentsRepository.save(payments);

        Refunds refunds = Refunds
                .builder()
                .payment(payments)
                .member(member)
                .pgTransactionKey("PG-REFUND-TEST-20260625-001")
                .idempotencyKey("IDEMPOTENCY-TEST-20260625-001")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 부분 환불")
                .status("COMPLETED")
                .requestedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .build();

        refundsRepository.save(refunds);
    }
}
