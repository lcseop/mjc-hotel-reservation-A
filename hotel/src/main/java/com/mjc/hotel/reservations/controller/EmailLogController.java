package com.mjc.hotel.reservations.controller;

import com.mjc.hotel.reservations.dto.EmailLogRequestDto;
import com.mjc.hotel.reservations.dto.EmailLogResponseDto;
import com.mjc.hotel.reservations.service.EmailLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.naming.Name;
import javax.swing.text.html.HTML;
import java.util.List;

@RestController
@RequestMapping("/api/v1/email-logs")
@RequiredArgsConstructor
@Tag(name = "이메일 로그", description = "이메일 로그 관리 API")
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

    @GetMapping("/{emailLogSid}/resend")
    public EmailLogResponseDto resendLog(@PathVariable Long emailLogSid) {
        return emailLogService.resendEmail(emailLogSid);
    }
}
