package com.mjc.hotel.reservations.service;

import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.dto.EmailLogResponseDto;
import com.mjc.hotel.reservations.entity.EmailLog;
import com.mjc.hotel.reservations.entity.EmailStatus;
import com.mjc.hotel.reservations.entity.EmailType;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.EmailLogRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailLogService {

    private final EmailLogRepository emailLogRepository;
    private final ReservationRepository reservationRepository;
    private final JavaMailSender mailSender;

    public EmailLogResponseDto sendEmailAndLog(EmailLogRequestDto request) {
        Reservation reservation = reservationRepository.findById(request.getSid())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        EmailType emailType = request.getEmailType() != null ? request.getEmailType() : EmailType.RESERVATION_CONFIRMATION;
        EmailStatus status;

        if (request.getRecipientEmail() == null || !request.getRecipientEmail().contains("@")) {
            status = EmailStatus.FAILED;
        } else {
            status = trySendMail(request.getRecipientEmail(), reservation, emailType);
        }

        EmailLog emailLog = EmailLog.builder()
                .reservation(reservation)
                .recipientEmail(request.getRecipientEmail())
                .emailStatus(status)
                .emailType(emailType)
                .build();

        EmailLog savedLog = emailLogRepository.save(emailLog);
        return toResponseDto(savedLog);
    }

    public EmailLogResponseDto resendEmail(Long emailLogSid) {
        EmailLog previousLog = emailLogRepository.findById(emailLogSid)
                .orElseThrow(() -> new RuntimeException("이메일 로그를 찾을 수 없습니다."));

        EmailLogRequestDto request = new EmailLogRequestDto();
        request.setSid(previousLog.getReservation().getSid());
        request.setRecipientEmail(previousLog.getRecipientEmail());
        request.setEmailType(previousLog.getEmailType());

        return sendEmailAndLog(request);
    }

    public List<EmailLogResponseDto> getLogsByReservation(Long reservationSid) {
        List<EmailLog> logs = emailLogRepository.findByReservation_Sid(reservationSid);
        return logs.stream().map(this::toResponseDto).toList();
    }

    private EmailStatus trySendMail(String recipientEmail, Reservation reservation, EmailType emailType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(buildSubject(emailType, reservation));
            message.setText(buildBody(emailType, reservation));
            mailSender.send(message);
            return EmailStatus.SEND;
        } catch (Exception e) {
            return EmailStatus.FAILED;
        }
    }

    private String buildSubject(EmailType emailType, Reservation reservation) {
        return switch (emailType) {
            case RESERVATION_CONFIRMATION -> "[호텔예약] 예약이 확정되었습니다 - " + reservation.getReservationNumber();
            case CANCELLATION_NOTICE -> "[호텔예약] 예약이 취소되었습니다 - " + reservation.getReservationNumber();
            case CHECK_IN_QR -> "[호텔예약] 체크인 QR코드 안내 - " + reservation.getReservationNumber();
        };
    }

    private String buildBody(EmailType emailType, Reservation reservation) {
        return switch (emailType) {
            case RESERVATION_CONFIRMATION -> String.format(
                    "%s님, 예약이 확정되었습니다.\n예약번호: %s\n체크인: %s\n체크아웃: %s\n결제금액: %d원",
                    reservation.getGuestName(), reservation.getReservationNumber(),
                    reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getTotalAmount());
            case CANCELLATION_NOTICE -> String.format(
                    "%s님, 예약이 취소되었습니다.\n예약번호: %s", reservation.getGuestName(), reservation.getReservationNumber());
            case CHECK_IN_QR -> String.format(
                    "%s님, 체크인 QR코드입니다.\n예약번호: %s\nQR코드: %s",
                    reservation.getGuestName(), reservation.getReservationNumber(), reservation.getCheckInQr());
        };
    }

    private EmailLogResponseDto toResponseDto(EmailLog log) {
        EmailLogResponseDto response = new EmailLogResponseDto();
        response.setSid(log.getSid());
        response.setReservationSid(log.getReservation().getSid());
        response.setRecipientEmail(log.getRecipientEmail());
        response.setEmailStatus(log.getEmailStatus().name());
        response.setSentAt(log.getSentAt());
        return response;
    }
}
