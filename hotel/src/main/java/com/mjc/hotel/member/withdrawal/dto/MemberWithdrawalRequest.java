package com.mjc.hotel.member.withdrawal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithdrawalRequest {

    private String password;
    private Boolean confirmed;
}
