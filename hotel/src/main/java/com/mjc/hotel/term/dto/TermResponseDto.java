package com.mjc.hotel.term.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class TermResponseDto {
    private Long sid;
    private String termType;
    private String title;
    private String version;
    private Boolean isRequired;
    private LocalDateTime effectiveAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
