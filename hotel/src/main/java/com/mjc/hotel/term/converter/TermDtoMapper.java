package com.mjc.hotel.term.converter;

import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.entity.Term;
import org.springframework.stereotype.Component;

@Component
public class TermDtoMapper {

    public Term toEntity(TermRequestDto dto) {
        return Term.builder()
                .termType(dto.getTermType())
                .title(dto.getTitle())
                .version(dto.getVersion())
                .isRequired(dto.getIsRequired())
                .effectiveAt(dto.getEffectiveAt())
                .build();
    }

    public TermResponseDto toResponseDto(Term term) {
        return TermResponseDto.builder()
                .sid(term.getSid())
                .termType(term.getTermType())
                .title(term.getTitle())
                .version(term.getVersion())
                .isRequired(term.getIsRequired())
                .effectiveAt(term.getEffectiveAt())
                .build();
    }
}
