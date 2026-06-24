//package com.mjc.hotel.reservations.controller;
//
//import com.mjc.hotel.reservations.entity.ReservationCancel;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reservation-cancels")
//@RequiredArgsConstructor
//public class ReservationCancelController {
//
//    private final ReservationCancelRepository reservationCancelRepository;
//
//    @GetMapping
//    public ResponseEntity<List<ReservationCancel>> getAllCancels() {
//        return ResponseEntity.ok(reservationCancelRepository.findAll());
//    }
//
//    @GetMapping("/reservation/{reservationId}")
//    public ResponseEntity<ReservationCancel> getCancelByReservationId(@PathVariable Long reservationId) {
//        return reservationCancelRepository.findByReservationId(reservationId)
//                .map(responseEntity::ok)
//    }
//}
