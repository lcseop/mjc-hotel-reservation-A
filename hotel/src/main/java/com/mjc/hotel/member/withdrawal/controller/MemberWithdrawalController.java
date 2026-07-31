package com.mjc.hotel.member.withdrawal.controller;

import com.mjc.hotel.member.withdrawal.dto.MemberWithdrawalRequest;
import com.mjc.hotel.member.withdrawal.service.MemberWithdrawalService;
import com.mjc.hotel.util.excep.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberWithdrawalController {

    private final MemberWithdrawalService memberWithdrawalService;

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            Authentication authentication,
            @RequestBody MemberWithdrawalRequest request
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationFailedException("로그인이 필요합니다.");
        }

        memberWithdrawalService.withdraw(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
