package com.mjc.hotel.term;

import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;

@SpringBootTest
public class TermServiceTest {

    @Autowired
    private TermRepository termRepository;

    @DisplayName("termTestData")
    @Test
    @Commit
    public void addTermTest() {
        Term term = Term
                .builder()
                .termType("SERVICE")
                .title("서비스 이용약관")
                .version("1.0")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();

        termRepository.save(term);
    }
}
