package com.mjc.hotel.reservations.controller;

import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.dto.EmailLogResponseDto;
import com.mjc.hotel.reservations.service.EmailLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-logs")
@RequiredArgsConstructor
public class EmailLogController {

    private final EmailLogService emailLogService;

    @PostMapping
    public EmailLogResponseDto createLog(@RequestBody EmailLogRequestDto request) {
        return emailLogService.sendEmailAndLog(request);
    }

    @GetMapping("/reservation/{reservationSid}")
    public List<EmailLogResponseDto> getLogs(@PathVariable Long reservationSid) {
        return emailLogService.getLogsByReservation(reservationSid);
    }

}
