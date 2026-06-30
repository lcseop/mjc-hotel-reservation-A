package com.mjc.hotel.member.dto;

import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class MemberResponseDto {
    private Long sid;
    private String name;
    private String phone;
    private String email;
    private MemberStatus status;
    private MemberRole role;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
