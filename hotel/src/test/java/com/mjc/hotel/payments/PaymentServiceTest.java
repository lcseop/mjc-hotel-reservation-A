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
public class PaymentServiceTest {

    @Autowired
    private PaymentsRepository paymentsRepository;
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

    @DisplayName("paymentTestData")
    @Test
    @Commit
    @Transactional
    public void addPaymentTest() {
        Member member = memberRepository.save(Member
                .builder()
                .name("결제 테스트 회원")
                .phone("010-1111-2222")
                .email("payment-test@mjc.com")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        HotelAmenities hotelAmenities = hotelAmenitiesRepository.save(HotelAmenities
                .builder()
                .title("와이파이")
                .description("전 구역 가능")
                .build());

        HotelPhoto hotelPhoto = hotelPhotoRepository.save(HotelPhoto
                .builder()
                .imagePath("https://example.com/payment-hotel.jpg")
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
                .hotelName("결제 테스트 호텔")
                .hotelPrice(180000)
                .location("서울시 테스트구")
                .starRating(4)
                .description("결제 테스트용 호텔")
                .build());

        RoomPhoto roomPhoto = roomPhotoRepository.save(RoomPhoto
                .builder()
                .imagePath("https://example.com/payment-room.jpg")
                .build());

        RoomTag roomTag = roomTagRepository.save(RoomTag
                .builder()
                .title("시티뷰")
                .build());

        RoomType roomType = roomTypeRepository.save(RoomType
                .builder()
                .title("스탠다드")
                .build());

        Room room = roomRepository.save(Room
                .builder()
                .hotelId(hotel)
                .roomTagId(roomTag)
                .roomPhotoId(roomPhoto)
                .roomTypeId(roomType)
                .roomName("결제 테스트 객실")
                .roomPrice(180000)
                .roomNumber(701)
                .floor(7)
                .area(28)
                .maximumPeople(2)
                .build());

        Reservation reservation = reservationRepository.save(Reservation
                .builder()
                .member(member)
                .room(room)
                .reservationNumber("PAYMENT-RESERVATION-TEST-001")
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .adults(2)
                .children(0)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(180000)
                .specialRequests("결제 테스트 요청")
                .checkInQr("PAYMENT-QR-TEST")
                .totalNights(2)
                .guestName("결제 테스트 회원")
                .build());

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
