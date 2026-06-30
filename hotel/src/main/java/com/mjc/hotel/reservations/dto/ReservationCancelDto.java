package com.mjc.hotel.reservations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelDto {

    @NotNull(message = "예약 ID는 필수입니다")
    private Long sid;

    @NotBlank(message = "취소 사유는 필수입니다")
    @Size(max = 255, message = "취소 사유는 255자 이내여야 합니다")
    private String cancelReason;
}
