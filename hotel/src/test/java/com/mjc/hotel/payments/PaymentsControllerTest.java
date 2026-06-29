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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;
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

    @DisplayName("결제 생성 API는 ApiResponse 형식으로 생성 결과를 반환한다")
    @Test
    public void insertPaymentApiTest() throws Exception {
        Member member = saveMember("결제 생성 회원", "create-payment-api@mjc.com");
        Reservation reservation = saveReservation(member);

        mockMvc.perform(post("/api/payments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toPaymentJson(reservation.getSid(), member.getSid())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("payments insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.sid").value(member.getSid()))
                .andExpect(jsonPath("$.data.reservationId").value(reservation.getSid()))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));
    }

    @DisplayName("결제 단건 조회 API는 ApiResponse 형식으로 조회 결과를 반환한다")
    @Test
    public void getPaymentApiTest() throws Exception {
        Member member = saveMember("결제 조회 회원", "read-payment-api@mjc.com");
        Reservation reservation = saveReservation(member);
        LocalDateTime paidAt = LocalDateTime.of(2026, 6, 29, 10, 15, 30);
        Payments payment = paymentsRepository.save(Payments.builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionNo("TXN-PAYMENT-API-READ")
                .paidAt(paidAt)
                .point(1800)
                .build());

        mockMvc.perform(get("/api/payments/{sid}", payment.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("payments select success"))
                .andExpect(jsonPath("$.data.sid").value(payment.getSid()))
                .andExpect(jsonPath("$.data.transactionNo").value("TXN-PAYMENT-API-READ"))
                .andExpect(jsonPath("$.data.paidAt").value("2026-06-29T10:15:30"));
    }

    @DisplayName("결제 수정 API는 예약과 회원 ID를 생략해도 기존 관계를 유지하고 수정 결과를 반환한다")
    @Test
    public void updatePaymentApiWithoutRelationIdsTest() throws Exception {
        Member member = saveMember("결제 수정 회원", "update-payment-api@mjc.com");
        Reservation reservation = saveReservation(member);
        Payments payment = paymentsRepository.save(Payments.builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .transactionNo("TXN-PAYMENT-API-BEFORE")
                .paidAt(LocalDateTime.now())
                .point(1800)
                .build());

        mockMvc.perform(put("/api/payments/{sid}", payment.getSid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toPaymentUpdateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("payments update success"))
                .andExpect(jsonPath("$.data.sid").value(payment.getSid()))
                .andExpect(jsonPath("$.data.sid").value(member.getSid()))
                .andExpect(jsonPath("$.data.reservationId").value(reservation.getSid()))
                .andExpect(jsonPath("$.data.paymentAmount").value(170000.00))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.transactionNo").value("TXN-PAYMENT-API-AFTER"));
    }

    private Member saveMember(String name, String email) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phone("010-1111-2222")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .build());
    }

    private Reservation saveReservation(Member member) {
        HotelAmenities hotelAmenities = hotelAmenitiesRepository.save(HotelAmenities.builder()
                .title("와이파이")
                .description("전 구역 가능")
                .build());
        HotelPhoto hotelPhoto = hotelPhotoRepository.save(HotelPhoto.builder()
                .imagePath("https://example.com/payment-hotel.jpg")
                .build());
        HotelType hotelType = hotelTypeRepository.save(HotelType.builder()
                .title("호텔")
                .build());
        Hotel hotel = hotelRepository.save(Hotel.builder()
                .type(hotelType)
                .photo(hotelPhoto)
                .amenities(hotelAmenities)
                .hotelName("결제 테스트 호텔")
                .hotelPrice(180000)
                .location("서울시 테스트구")
                .starRating(4)
                .description("결제 테스트용 호텔")
                .build());
        RoomPhoto roomPhoto = roomPhotoRepository.save(RoomPhoto.builder()
                .imagePath("https://example.com/payment-room.jpg")
                .build());
        RoomTag roomTag = roomTagRepository.save(RoomTag.builder()
                .title("시티뷰")
                .build());
        RoomType roomType = roomTypeRepository.save(RoomType.builder()
                .title("스탠다드")
                .build());
        Room room = roomRepository.save(Room.builder()
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

        return reservationRepository.save(Reservation.builder()
                .member(member)
                .room(room)
                .reservationNumber("PAYMENT-API-" + System.nanoTime())
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(3))
                .adults(2)
                .children(0)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(180000)
                .specialRequests("결제 테스트 요청")
                .checkInQr("PAYMENT-QR-TEST")
                .totalNights(2)
                .guestName(member.getName())
                .build());
    }

    private String toPaymentJson(Long reservationId, Long sid) {
        return """
                {
                  "reservationId": %d,
                  "sid": %d,
                  "paymentAmount": 180000.00,
                  "paymentMethod": "CARD",
                  "paymentStatus": "COMPLETED",
                  "transactionNo": "TXN-PAYMENT-API-CREATE",
                  "paidAt": "%s",
                  "point": 1800
                }
                """.formatted(reservationId, sid, LocalDateTime.now());
    }

    private String toPaymentUpdateJson() {
        return """
                {
                  "paymentAmount": 170000.00,
                  "paymentMethod": "CARD",
                  "paymentStatus": "COMPLETED",
                  "transactionNo": "TXN-PAYMENT-API-AFTER",
                  "paidAt": "%s",
                  "point": 1700
                }
                """.formatted(LocalDateTime.now());
    }
}
