package com.mjc.hotel.member.controller;

import com.mjc.hotel.member.mapper.MemberMapper;
import com.mjc.hotel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;


}
