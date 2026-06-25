package com.mjc.hotel.term.controller;

import com.mjc.hotel.term.mapper.TermMapper;
import com.mjc.hotel.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TermController {

    private final TermMapper termMapper;
    private final TermRepository termRepository;

}
