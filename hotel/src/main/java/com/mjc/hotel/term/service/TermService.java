package com.mjc.hotel.term.service;

import com.mjc.hotel.term.converter.TermDtoMapper;
import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermRepository termRepository;
    private final TermDtoMapper termDtoMapper;

    @Transactional
    public TermResponseDto insert(TermRequestDto request) {
        return termDtoMapper.toResponseDto(termRepository.save(termDtoMapper.toEntity(request)));
    }

    public List<TermResponseDto> getTerms() {
        return termRepository.findAll().stream()
                .map(termDtoMapper::toResponseDto)
                .toList();
    }

    public TermResponseDto getTerm(Long termId) {
        return termDtoMapper.toResponseDto(findTerm(termId));
    }

    @Transactional
    public TermResponseDto updateTerm(Long termId, TermRequestDto request) {
        Term term = findTerm(termId);
        term.setTermType(request.getTermType());
        term.setTitle(request.getTitle());
        term.setVersion(request.getVersion());
        term.setIsRequired(request.getIsRequired());
        term.setEffectiveAt(request.getEffectiveAt());

        return termDtoMapper.toResponseDto(term);
    }

    @Transactional
    public void deleteTerm(Long termId) {
        termRepository.deleteById(termId);
    }

    private Term findTerm(Long termId) {
        return termRepository.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. termId=" + termId));
    }
}
