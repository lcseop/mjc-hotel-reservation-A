package com.mjc.hotel.member.dto;

import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Getter
@Setter
public class MemberRequestDto {
    private String name;
    private String phone;
    private String email;
    private MemberStatus status;
    private MemberRole role;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Integer point;
}
