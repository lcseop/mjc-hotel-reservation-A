package com.mjc.hotel.term;

import com.mjc.hotel.term.dto.TermRequestDto;
import com.mjc.hotel.term.dto.TermResponseDto;
import com.mjc.hotel.term.service.TermService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TermRestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TermService termService;

    @DisplayName("약관 생성 API는 ApiResponse 형식으로 생성 결과를 반환한다")
    @Test
    public void insertTermApiTest() throws Exception {
        TermRequestDto request = buildRequest("SERVICE", "서비스 이용약관", "1.0", true);

        mockMvc.perform(post("/api/term/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("term insert success"))
                .andExpect(jsonPath("$.data.sid", notNullValue()))
                .andExpect(jsonPath("$.data.title").value("서비스 이용약관"));
    }

    @DisplayName("약관 단건 조회 API는 ApiResponse 형식으로 조회 결과를 반환한다")
    @Test
    public void getTermApiTest() throws Exception {
        TermResponseDto savedTerm = termService.insert(buildRequest("PRIVACY", "개인정보 처리방침", "1.0", true));

        mockMvc.perform(get("/api/term/{sid}", savedTerm.getSid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("term select success"))
                .andExpect(jsonPath("$.data.sid").value(savedTerm.getSid()))
                .andExpect(jsonPath("$.data.title").value("개인정보 처리방침"));
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

    private String toJson(TermRequestDto request) {
        return """
                {
                  "termType": "%s",
                  "title": "%s",
                  "version": "%s",
                  "isRequired": %s,
                  "effectiveAt": "%s"
                }
                """.formatted(
                request.getTermType(),
                request.getTitle(),
                request.getVersion(),
                request.getIsRequired(),
                request.getEffectiveAt()
        );
    }
}
