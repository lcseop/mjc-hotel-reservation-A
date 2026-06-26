package com.mjc.hotel.reservation;

import com.mjc.hotel.reservations.entity.EmailLog;
import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.repository.EmailLogRepository;
import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class EmailLogTest {

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Test
    @Commit
    public void EmailLogTests() {

        Reservation reservation = Reservation.builder().sid(1L).build();

        EmailLog emailLog = EmailLog
                .builder()
                .reservation(reservation)
                .recipientEmail("recipientEmail")
                .emailStatus(EmailLog.EmailStatus.SEND)
                .sentAt(LocalDateTime.now())
                .build();

        emailLogRepository.save(emailLog);
    }
}
