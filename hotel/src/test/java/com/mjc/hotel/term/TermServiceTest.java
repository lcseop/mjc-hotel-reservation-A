package com.mjc.hotel.term;

import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.repository.TermRepository;
import com.mjc.hotel.term.service.TermService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class TermServiceTest {

    @Autowired
    private TermService termService;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private EntityManager entityManager;

    @DisplayName("약관을 생성한다")
    @Test
    public void insertTermTest() {
        TermResponseDto response = termService.insert(buildRequest("SERVICE", "서비스 이용약관", "1.0", true));

        assertThat(response.getTermId()).isNotNull();
        assertThat(response.getTermType()).isEqualTo("SERVICE");
        assertThat(response.getTitle()).isEqualTo("서비스 이용약관");
        assertThat(response.getVersion()).isEqualTo("1.0");
        assertThat(response.getIsRequired()).isTrue();
        assertThat(termRepository.findById(response.getTermId())).isPresent();
    }

    @DisplayName("약관 목록과 단건 약관을 조회한다")
    @Test
    public void readTermTest() {
        TermResponseDto savedTerm = termService.insert(buildRequest("PRIVACY", "개인정보 처리방침", "1.0", true));

        assertThat(termService.getTerms())
                .extracting(TermResponseDto::getTermId)
                .contains(savedTerm.getTermId());
        assertThat(termService.getTerm(savedTerm.getTermId()).getTitle()).isEqualTo("개인정보 처리방침");
    }

    @DisplayName("약관 정보를 수정한다")
    @Test
    public void updateTermTest() {
        TermResponseDto savedTerm = termService.insert(buildRequest("SERVICE", "수정 전 약관", "1.0", true));

        TermResponseDto updatedTerm = termService.updateTerm(
                savedTerm.getTermId(),
                buildRequest("SERVICE", "수정 후 약관", "2.0", false)
        );

        assertThat(updatedTerm.getTermId()).isEqualTo(savedTerm.getTermId());
        assertThat(updatedTerm.getTitle()).isEqualTo("수정 후 약관");
        assertThat(updatedTerm.getVersion()).isEqualTo("2.0");
        assertThat(updatedTerm.getIsRequired()).isFalse();

        entityManager.flush();
        entityManager.clear();

        assertThat(termService.getTerm(savedTerm.getTermId()).getTitle()).isEqualTo("수정 후 약관");
    }

    @DisplayName("약관을 삭제한다")
    @Test
    public void deleteTermTest() {
        TermResponseDto savedTerm = termService.insert(buildRequest("MARKETING", "마케팅 수신 동의", "1.0", false));

        termService.deleteTerm(savedTerm.getTermId());
        entityManager.flush();
        entityManager.clear();

        assertThat(termRepository.findById(savedTerm.getTermId())).isEmpty();
    }

    private TermRequestDto buildRequest(String termType, String title, String version, Boolean isRequired) {
        return TermRequestDto.builder()
                .termType(termType)
                .title(title)
                .version(version)
                .isRequired(isRequired)
                .effectiveAt(LocalDateTime.now())
                .build();
    }
}
