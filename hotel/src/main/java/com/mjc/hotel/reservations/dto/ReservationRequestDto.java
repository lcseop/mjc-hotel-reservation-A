package com.mjc.hotel.reservations.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {

    @NotNull(message = "예약 ID는 필수입니다")
    private Long sid;

    @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    @NotNull(message = "객실 ID는 필수입니다")
    private Long roomId;

    private Long couponIssueId;

    @NotNull(message = "체크인 날짜는 필수입니다")
    @Future(message = "체크인 날짜는 현재 이후여야 합니다")
    private LocalDateTime checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다")
    @Future(message = "체크아웃 날짜는 현재 이후여야 합니다")
    private LocalDateTime checkOutDate;

    @NotNull(message = "성인 수는 필수입니다")
    @Min(value = 1, message = "성인은 최소 1명 이상이어야 합니다")
    private Integer adults;

    @Min(value = 0, message = "어린이 수는 0명 이상이어야 합니다")
    private Integer children;

    private String specialRequests;

    @NotBlank(message = "투숙객 이름은 필수입니다")
    @Size(max = 50, message = "투숫객 이름은 50자 이내여야 합니다")
    private String guestName;

    @Min(value = 0, message = "사용 포인트는 0 이상이어야 합니다")
    private Integer usePoint;

}
