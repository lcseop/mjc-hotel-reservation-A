package com.mjc.hotel.term.service;

import com.mjc.hotel.term.converter.TermDtoMapper;
import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public List<TermResponseDto> getTerms() {
        ensureDefaultTerms();
        return termRepository.findAll().stream()
                .map(termDtoMapper::toResponseDto)
                .toList();
    }

    public TermResponseDto getTerm(Long sid) {
        return termDtoMapper.toResponseDto(findTerm(sid));
    }

    @Transactional
    public TermResponseDto updateTerm(Long sid, TermRequestDto request) {
        Term term = findTerm(sid);
        term.setTermType(request.getTermType());
        term.setTitle(request.getTitle());
        term.setVersion(request.getVersion());
        term.setIsRequired(request.getIsRequired());
        term.setEffectiveAt(request.getEffectiveAt());

        return termDtoMapper.toResponseDto(term);
    }

    @Transactional
    public void deleteTerm(Long sid) {
        Term term = findTerm(sid);
        term.markDeleted();
    }

    private Term findTerm(Long sid) {
        return termRepository.findById(sid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. sid=" + sid));
    }

    private void ensureDefaultTerms() {
        createDefaultTermIfMissing("SERVICE", "이용약관 동의", true);
        createDefaultTermIfMissing("PRIVACY", "개인정보 수집 및 이용 동의", true);
        createDefaultTermIfMissing("MARKETING", "마케팅 정보 수신 동의", false);
    }

    private void createDefaultTermIfMissing(String termType, String title, boolean required) {
        termRepository.findFirstByTermTypeAndDeletedFalse(termType).orElseGet(() -> termRepository.save(Term.builder()
                .termType(termType)
                .title(title)
                .version("1.0")
                .isRequired(required)
                .effectiveAt(LocalDateTime.now())
                .build()));
    }
}
