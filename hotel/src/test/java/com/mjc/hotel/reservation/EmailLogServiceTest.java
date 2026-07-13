package com.mjc.hotel.reservation;
import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.dto.EmailLogResponseDto;
import com.mjc.hotel.reservations.entity.EmailLog;
import com.mjc.hotel.reservations.entity.EmailStatus;
import com.mjc.hotel.reservations.entity.EmailType;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.EmailLogRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import com.mjc.hotel.reservations.service.EmailLogService;
import com.mjc.hotel.room.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailLogServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks private EmailLogService emailLogService;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member member = Member.builder().sid(1L).name("홍길동").email("test@test.com").build();
        Room room = Room.builder().sid(1L).roomNumber(101).build();
        reservation = Reservation.builder()
                .sid(1L)
                .member(member)
                .room(room)
                .reservationNumber("RSV-TEST1234")
                .checkInDate(LocalDateTime.now().plusDays(1))
                .checkOutDate(LocalDateTime.now().plusDays(2))
                .totalAmount(100000)
                .guestName("홍길동")
                .build();
    }

    @Test
    @DisplayName("sendEmailAndLog - 정상 이메일이면 SEND 상태로 로그가 저장된다")
    void sendEmailAndLog_success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(inv -> {
            EmailLog log = inv.getArgument(0);
            log.setSid(100L);
            return log;
        });

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail("test@test.com");
        request.setEmailType(EmailType.RESERVATION_CONFIRMATION);

        EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.SEND.name());
        assertThat(response.getReservationSid()).isEqualTo(1L);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmailAndLog - 이메일 형식이 잘못되면 FAILED 상태로 저장되고 발송 시도하지 않는다")
    void sendEmailAndLog_invalidEmailFormat() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail("invalid-email");

        EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.FAILED.name());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmailAndLog - 메일 서버 오류 시 FAILED 상태로 저장된다")
    void sendEmailAndLog_mailServerError() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        doThrow(new MailSendException("SMTP 연결 실패")).when(mailSender).send(any(SimpleMailMessage.class));
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail("test@test.com");

        EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.FAILED.name());
    }

    @Test
    @DisplayName("sendEmailAndLog - 존재하지 않는 예약이면 예외가 발생한다")
    void sendEmailAndLog_reservationNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail("test@test.com");

        assertThatThrownBy(() -> emailLogService.sendEmailAndLog(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("예약을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("resendEmail - 이전 로그 정보를 그대로 사용해 재발송한다")
    void resendEmail_success() {
        EmailLog previousLog = EmailLog.builder()
                .sid(100L)
                .reservation(reservation)
                .recipientEmail("test@test.com")
                .emailStatus(EmailStatus.FAILED)
                .emailType(EmailType.RESERVATION_CONFIRMATION)
                .build();

        when(emailLogRepository.findById(100L)).thenReturn(Optional.of(previousLog));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailLogResponseDto response = emailLogService.resendEmail(100L);

        assertThat(response.getRecipientEmail()).isEqualTo("test@test.com");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("resendEmail - 존재하지 않는 로그 ID면 예외가 발생한다")
    void resendEmail_logNotFound() {
        when(emailLogRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailLogService.resendEmail(100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이메일 로그를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("getLogsByReservation - 예약 ID로 로그 목록을 조회한다")
    void getLogsByReservation_success() {
        EmailLog log1 = EmailLog.builder()
                .sid(1L).reservation(reservation)
                .recipientEmail("a@test.com")
                .emailStatus(EmailStatus.SEND)
                .emailType(EmailType.RESERVATION_CONFIRMATION)
                .build();
        EmailLog log2 = EmailLog.builder()
                .sid(2L)
                .reservation(reservation)
                .recipientEmail("a@test.com")
                .emailStatus(EmailStatus.SEND)
                .emailType(EmailType.CHECK_IN_QR)
                .build();

        when(emailLogRepository.findByReservation_Sid(1L)).thenReturn(List.of(log1, log2));

        List<EmailLogResponseDto> result = emailLogService.getLogsByReservation(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("예약 확정 이메일은 예약 정보가 포함된 제목과 본문으로 발송된다")
    void sendEmailAndLog_shouldBuildReservationConfirmationMailCorrectly() {
        String testEmail = "guest@example.com";

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(invocation -> {
            EmailLog log = invocation.getArgument(0);
            log.setSid(100L);
            return log;
        });

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail(testEmail);
        request.setEmailType(EmailType.RESERVATION_CONFIRMATION);

       EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

       assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.SEND.name());

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly(testEmail);
        assertThat(sentMessage.getSubject())
                .contains("예약이 확정되었습니다")
                .contains("RSV-TEST1234");
                assertThat(sentMessage.getText())
                        .contains("홍길동님")
                        .contains("예약번호: RSV-TEST1234")
                        .contains("결제금액: 100000원");
    }

    @Test
    @DisplayName("예약 취소 이메일은 취소 안내 제목과 예약번호를 포함한다")
void sendEmailAndLog_cancellationNotice() {
        String testEmail = "guest@example.com";

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        when(emailLogRepository.save(any(EmailLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail(testEmail);
        request.setEmailType(EmailType.CANCELLATION_NOTICE);

        EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.SEND.name());

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly(testEmail);
        assertThat(message.getSubject())
                .contains("예약이 취소되었습니다")
                .contains("RSV-TEST1234");
        assertThat(message.getText())
                .contains("홍길동님")
                .contains("예약이 취소되었습니다")
                .contains("RSV-TEST1234");
    }

    @Test
    @DisplayName("체크인 QR 이메일은 QR 코드 정보를 포함한다")
    void sendEmailAndLog_checkInQr() {
        String testEmail = "guest@example.com";
        String checkInQr = "https://example.com/check-in/RSV-TEST1234";

        reservation.setCheckInQr(checkInQr);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        when(emailLogRepository.save(any(EmailLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(1L);
        request.setRecipientEmail(testEmail);
        request.setEmailType(EmailType.CHECK_IN_QR);

        EmailLogResponseDto response = emailLogService.sendEmailAndLog(request);

        assertThat(response.getEmailStatus()).isEqualTo(EmailStatus.SEND.name());

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getTo()).containsExactly(testEmail);
        assertThat(message.getSubject()).contains("체크인 QR코드 안내");
        assertThat(message.getText())
                .contains("예약번호: RSV-TEST1234")
                .contains("QR코드")
                .contains(checkInQr);
    }
}
