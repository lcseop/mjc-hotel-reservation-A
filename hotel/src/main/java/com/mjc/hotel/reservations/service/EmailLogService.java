package com.mjc.hotel.reservations.service;

import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.dto.EmailLogResponseDto;
import com.mjc.hotel.reservations.entity.EmailLog;
import com.mjc.hotel.reservations.entity.EmailStatus;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.EmailLogRepository;
import com.mjc.hotel.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailLogService {

    private final EmailLogRepository emailLogRepository;
    private final ReservationRepository reservationRepository;

    public EmailLogResponseDto sendEmailAndLog(EmailLogRequestDto request) {
        Reservation reservation = reservationRepository.findById(request.getSid())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        EmailStatus status = EmailStatus.SEND;
        if(request.getRecipientEmail() == null || !request.getRecipientEmail().contains("@")) {
            status = EmailStatus.FAILED;
        }

        EmailLog emailLog = EmailLog.builder()
                .reservation(reservation)
                .recipientEmail(request.getRecipientEmail())
                .emailStatus(status)
                .build();

        EmailLog saveLog = emailLogRepository.save(emailLog);

        EmailLogResponseDto response = new EmailLogResponseDto();
        response.setSid(saveLog.getSid());
        response.setReservationSid(reservation.getSid());
        response.setRecipientEmail(saveLog.getRecipientEmail());
        response.setEmailStatus(saveLog.getEmailStatus().name());
        response.setSentAt(saveLog.getSentAt());

        return response;
    }

    public List<EmailLogResponseDto> getLogsByReservation(Long reservationSid) {
        List<EmailLog> logs = emailLogRepository.findByReservation_Sid(reservationSid);

        return logs.stream()
                .map(log -> {
                    EmailLogResponseDto response = new EmailLogResponseDto();
                    response.setSid(log.getSid());
                    response.setReservationSid(log.getReservation().getSid());
                    response.setRecipientEmail(log.getRecipientEmail());
                    response.setEmailStatus(log.getEmailStatus().name());
                    response.setSentAt(log.getSentAt());
                    return response;
                })
                .toList();
    }
}
