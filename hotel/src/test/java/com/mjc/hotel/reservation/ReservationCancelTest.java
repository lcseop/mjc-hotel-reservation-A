package com.mjc.hotel.reservation;

import com.mjc.hotel.reservations.entity.Reservation;
import com.mjc.hotel.reservations.entity.ReservationCancel;
import com.mjc.hotel.reservations.repository.ReservationCancelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class ReservationCancelTest {
    @Autowired
    private ReservationCancelRepository reservationCancelRepository;

    @Test
    @Commit
    public void ReservationCancelTests() {

        Reservation reservation = Reservation.builder().sid(1L).build();

        ReservationCancel reservationCancel = ReservationCancel
                .builder()
                .reservation(reservation)
                .cancelReason("cancelreason")
                .refundAmount(180000)
                .cancelledAt(LocalDateTime.now())
                .build();

        reservationCancelRepository.save(reservationCancel);
    }
}
