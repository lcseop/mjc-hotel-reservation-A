//package com.mjc.hotel.reservations.controller;
//
//import com.mjc.hotel.reservations.entity.EmailLog;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/email-logs")
//@RequiredArgsConstructor
//public class EmailLogController {
//
//    private final EmailLogRepository emailLogRepository;
//
//    @GetMapping
//    public ResponseEntity<List<EmailLog>> getAllEmailLogs() {return ResponseEntity.ok(emailLogRepository.findAll());}
//
//    @GetMapping("/{emailLogId")
//    public ResponseEntity<EmailLog> getEmailLogById(@PathVariable Long emilLogId) {
//        return emailLogRepository.findById(emailLogId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/reservation/{reservationId")
//    public ResponseEntity<List<EmailLog>> getEmailLogsByReservationId(@PathVariable Long reservationId) {
//        return ResponseEntity.ok(emialLogRepository.findByReservationId(reservationId));
////    }
////
////    @GetMapping("/status/{status}")
////    public ResponseEntity<List<EmailLog>> getEmailLogsByStatus(@Pathvariable EmailLog.EmailStatus status) {
////        return ResponseEntity.ok(emailLogRepository.findByStatus(status));
////    }
////
////    @PostMapping
////    public ResponseEntity<String> createEmailLog(@RequestBody EmailLog emailLog) {
////        int result = emailLogRepository.save(emailLog);
////        if (result > 0) {
////            return ResponseEntity.ok("이메일 발송 이력이 등록되었습니다.");
////        }
////        return ResponseEntity.internalServerError().body("이메일 발송 이력 등록에 실패했습니다.");
////    }
////
////    @PatchMapping("/{emailLogId}/status")
////    public ResponseEntity<String> updateEmailLogStatus(
////            @PathVariable Long emailLogId,
////            @RequestParam EmailLog.EmailStatus status) {
////        int result = emailLongRepository.updatesStatus(emailLogId, status);
////        if (result > 0) {
////            return ResponseEntity.ok("이메일 발송 상태가 변경되었습니다.");
////        }
////        return ResponseEntity.notFound().build();
////    }
////
////    @DeleteMapping("/{emailLongId}")
////    public ResponseEntity<String> deleteEmailLog(@PathVariable Long emailLongId) {
////        int result = emailLogRepository.deleteById(EmailLogId);
////        if (result > 0 ) {
////            return ResponseEntity.ok("이메일 발송 이력이 삭제되었습니다.");
////        }
////        return ResponseEntity.notFound().build();
////    }
////
////}
