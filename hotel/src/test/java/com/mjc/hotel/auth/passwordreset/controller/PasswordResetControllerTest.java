package com.mjc.hotel.auth.passwordreset.controller;

import com.mjc.hotel.auth.passwordreset.exception.PasswordResetAccountNotFoundException;
import com.mjc.hotel.auth.passwordreset.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordResetControllerTest {

    private PasswordResetService passwordResetService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        passwordResetService = mock(PasswordResetService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PasswordResetController(passwordResetService)
        ).build();
    }

    @Test
    void sendCodeUsesRequestedMemberApiPath() throws Exception {
        mockMvc.perform(post("/api/member/password-reset/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk());

        verify(passwordResetService).requestVerificationCode("user@example.com");
    }

    @Test
    void unknownEmailReturnsNotFound() throws Exception {
        doThrow(new PasswordResetAccountNotFoundException())
                .when(passwordResetService)
                .requestVerificationCode("unknown@example.com");

        mockMvc.perform(post("/api/member/password-reset/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyCodeUsesRequestedMemberApiPath() throws Exception {
        mockMvc.perform(post("/api/member/password-reset/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"code\":\"123456\"}"))
                .andExpect(status().isOk());

        verify(passwordResetService).verifyCode("user@example.com", "123456");
    }

    @Test
    void passwordResetUsesPatchAndRequestedBody() throws Exception {
        mockMvc.perform(patch("/api/member/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "code": "123456",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isOk());

        verify(passwordResetService).resetPassword(any());
    }
}
