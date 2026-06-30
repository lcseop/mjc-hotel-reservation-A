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
import com.mjc.hotel.room.entity.RoomPhoto;
import com.mjc.hotel.room.entity.RoomTag;
import com.mjc.hotel.room.entity.RoomType;
import com.mjc.hotel.room.repository.RoomPhotoRepository;
import com.mjc.hotel.room.repository.RoomRepository;
import com.mjc.hotel.room.repository.RoomTagRepository;
import com.mjc.hotel.room.repository.RoomTypeRepository;
import jakarta.persistence.EntityManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RefundsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RefundsRepository refundsRepository;
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
    @Autowired
    private EntityManager entityManager;

    @DisplayName("환불 생성 API는 ApiResponse 형식으로 생성 결과를 반환한다")
    @Test
    public void insertRefundApiTest() throws Exception {
        Member member = saveMember("환불 생성 회원", "create-refund-api@mjc.com");
        Payments payment = savePayment(member);

        mockMvc.perform(post("/api/refunds/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRefundJson(payment.getSid(), member.getSid())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("refunds insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.sid").value(payment.getSid()))
                .andExpect(jsonPath("$.data.sid").value(member.getSid()))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @DisplayName("환불 단건 조회 API는 ApiResponse 형식으로 조회 결과를 반환한다")
    @Test
    public void getRefundApiTest() throws Exception {
        Member member = saveMember("환불 조회 회원", "read-refund-api@mjc.com");
        Payments payment = savePayment(member);
        Refunds refund = refundsRepository.save(Refunds.builder()
                .payment(payment)
                .member(member)
                .pgTransactionKey("PG-REFUND-API-READ")
                .idempotencyKey("IDEMPOTENCY-REFUND-API-READ")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 환불 조회")
                .status(RefundStatus.COMPLETED)
                .requestedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/refunds/{sid}", refund.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("refunds select success"))
                .andExpect(jsonPath("$.data.sid").value(refund.getSid()))
                .andExpect(jsonPath("$.data.pgTransactionKey").value("PG-REFUND-API-READ"));
    }

    @DisplayName("환불 수정 API는 완료 상태로 변경할 때 완료 일시를 자동으로 기록한다")
    @Test
    public void updateRefundApiSetsCompletedAtTest() throws Exception {
        Member member = saveMember("환불 완료 회원", "complete-refund-api@mjc.com");
        Payments payment = savePayment(member);
        LocalDateTime requestedAt = LocalDateTime.of(2026, 6, 29, 9, 0, 0);
        Refunds refund = refundsRepository.save(Refunds.builder()
                .payment(payment)
                .member(member)
                .pgTransactionKey("PG-REFUND-API-COMPLETE-BEFORE")
                .idempotencyKey("IDEMPOTENCY-REFUND-API-COMPLETE")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 환불 완료")
                .status(RefundStatus.REQUESTED)
                .requestedAt(requestedAt)
                .build());

        mockMvc.perform(put("/api/refunds/{sid}", refund.getSid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRefundCompleteJson(payment.getSid(), member.getSid(), requestedAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("refunds update success"))
                .andExpect(jsonPath("$.data.sid").value(refund.getSid()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt", notNullValue()));
    }

    @DisplayName("환불 삭제 API는 물리 삭제하지 않고 삭제 표시한다")
    @Test
    public void deleteRefundApiSoftDeleteTest() throws Exception {
        Member member = saveMember("환불 삭제 회원", "delete-refund-api@mjc.com");
        Payments payment = savePayment(member);
        Refunds refund = refundsRepository.save(Refunds.builder()
                .payment(payment)
                .member(member)
                .pgTransactionKey("PG-REFUND-API-DELETE")
                .idempotencyKey("IDEMPOTENCY-REFUND-API-DELETE")
                .refundAmount(new BigDecimal("50000.00"))
                .reason("테스트 환불 삭제")
                .status(RefundStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(delete("/api/refunds/{sid}", refund.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("refunds delete success"));

        entityManager.flush();
        entityManager.clear();

        Refunds deletedRefund = refundsRepository.findById(refund.getSid()).orElseThrow();
        assertThat(deletedRefund.getDeleted()).isTrue();
        assertThat(deletedRefund.getDeletedAt()).isNotNull();

        mockMvc.perform(get("/api/refunds/{sid}", refund.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.deletedAt", notNullValue()));
    }

    private Member saveMember(String name, String email) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phone("010-3333-4444")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .emailVerified(true)
                .phoneVerified(true)
                .build());
    }

    private Payments savePayment(Member member) {
        Reservation reservation = saveReservation(member);

        return paymentsRepository.save(Payments.builder()
                .reservation(reservation)
                .member(member)
                .paymentAmount(new BigDecimal("180000.00"))
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PARTIALLY_REFUNDED)
                .transactionNo("TXN-REFUND-API-" + System.nanoTime())
                .paidAt(LocalDateTime.now())
                .point(1800)
                .build());
    }

    private Reservation saveReservation(Member member) {
        HotelAmenities hotelAmenities = hotelAmenitiesRepository.save(HotelAmenities.builder()
                .title("조식")
                .description("테스트 조식 제공")
                .build());
        HotelPhoto hotelPhoto = hotelPhotoRepository.save(HotelPhoto.builder()
                .imagePath("https://example.com/refund-hotel.jpg")
                .build());
        HotelType hotelType = hotelTypeRepository.save(HotelType.builder()
                .title("호텔")
                .build());
        Hotel hotel = hotelRepository.save(Hotel.builder()
                .type(hotelType)
                .photo(hotelPhoto)
                .amenities(hotelAmenities)
                .hotelName("환불 테스트 호텔")
                .hotelPrice(180000)
                .location("부산시 테스트구")
                .starRating(5)
                .description("환불 테스트용 호텔")
                .build());
        RoomPhoto roomPhoto = roomPhotoRepository.save(RoomPhoto.builder()
                .imagePath("https://example.com/refund-room.jpg")
                .build());
        RoomTag roomTag = roomTagRepository.save(RoomTag.builder()
                .title("오션뷰")
                .build());
        RoomType roomType = roomTypeRepository.save(RoomType.builder()
                .title("디럭스")
                .build());
        Room room = roomRepository.save(Room.builder()
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

        return reservationRepository.save(Reservation.builder()
                .member(member)
                .room(room)
                .reservationNumber("REFUND-API-" + System.nanoTime())
                .checkInDate(LocalDateTime.now().plusDays(2))
                .checkOutDate(LocalDateTime.now().plusDays(4))
                .adults(2)
                .children(1)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .totalAmount(180000)
                .specialRequests("환불 테스트 요청")
                .checkInQr("REFUND-QR-TEST")
                .totalNights(2)
                .guestName(member.getName())
                .build());
    }

    private String toRefundJson(Long paymentSid, Long memberSid) {
        return """
                {
                  "sid": %d,
                  "sid": %d,
                  "pgTransactionKey": "PG-REFUND-API-CREATE",
                  "idempotencyKey": "IDEMPOTENCY-REFUND-API-CREATE",
                  "refundAmount": 50000.00,
                  "reason": "테스트 환불 생성",
                  "status": "REQUESTED",
                  "requestedAt": "%s"
                }
                """.formatted(paymentSid, memberSid, LocalDateTime.now());
    }

    private String toRefundCompleteJson(Long paymentSid, Long memberSid, LocalDateTime requestedAt) {
        return """
                {
                  "sid": %d,
                  "sid": %d,
                  "pgTransactionKey": "PG-REFUND-API-COMPLETE-AFTER",
                  "idempotencyKey": "IDEMPOTENCY-REFUND-API-COMPLETE",
                  "refundAmount": 50000.00,
                  "reason": "테스트 환불 완료",
                  "status": "COMPLETED",
                  "requestedAt": "%s",
                  "completedAt": null,
                  "failedAt": null,
                  "failureReason": null
                }
                """.formatted(paymentSid, memberSid, requestedAt);
    }
}
