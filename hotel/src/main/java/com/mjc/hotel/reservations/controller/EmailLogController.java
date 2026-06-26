package com.mjc.hotel.reservations.controller;

import com.mjc.hotel.reservations.dto.EmailLogRequest;
import com.mjc.hotel.reservations.dto.EmailLogResponse;
import com.mjc.hotel.reservations.service.EmailLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vi/email-logs")
@RequiredArgsConstructor
public class EmailLogController {

    private final EmailLogService emailLogService;

    @PostMapping
    public EmailLogResponse createLog(@RequestBody EmailLogRequest request) {
        return emailLogService.sendEmailAndLog(request);
    }

    @GetMapping("/reservation/{reservationSid}")
    public List<EmailLogResponse> getLogs(@PathVariable Long reservationSid) {
        return emailLogService.getLogsByReservation(reservationSid);
    }

}
