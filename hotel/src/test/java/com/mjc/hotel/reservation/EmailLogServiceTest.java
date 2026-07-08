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
        EmailLog log1 = EmailLog.builder().sid(1L).reservation(reservation).recipientEmail("a@test.com").emailStatus(EmailStatus.SEND).emailType(EmailType.RESERVATION_CONFIRMATION).build();
        EmailLog log2 = EmailLog.builder().sid(2L).reservation(reservation).recipientEmail("a@test.com").emailStatus(EmailStatus.SEND).emailType(EmailType.CEHCK_IN_QR).build();

        when(emailLogRepository.findByReservation_Sid(1L)).thenReturn(List.of(log1, log2));

        List<EmailLogResponseDto> result = emailLogService.getLogsByReservation(1L);

        assertThat(result).hasSize(2);
    }
}
