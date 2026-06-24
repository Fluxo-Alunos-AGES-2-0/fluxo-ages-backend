package com.fluxo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security — Token access validation")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ILL_FORMED_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9" +
                    ".eyJzdWIiOiIxIn0" +
                    ".intentionally_invalid_signature";

    private static final String EXPIRED_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9" +
                    ".eyJzdWIiOiIxIiwibmFtZSI6InRlc3QiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTY5OTk5OTAwMCwiZXhwIjoxNzAwMDAwMDAwfQ" +
                    ".njfoHkRZgMWnOBPvkX0Q7-FMHdn3nKtCR0p3exmSpf4";

    private static ResultMatcher statusIsNot401() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status == 401) {
                throw new AssertionError("Expected: any status except 401, but was: 401");
            }
        };
    }

    @Nested
    @DisplayName("No Authorization header")
    class NoToken {

        @Test
        @DisplayName("GET /dashboard without token - 401")
        void dashboard_noToken_returns401() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Response body contains expected error message")
        void dashboard_noToken_bodyContainsErrorMessage() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json("{\"error\":\"Token ausente ou inválido\"}"));
        }
    }

    @Nested
    @DisplayName("Illformed token")
    class IllformedToken {

        @Test
        @DisplayName("GET /dashboard with invalid signature - 401")
        void dashboard_invalidSignature_returns401() throws Exception {
            mockMvc.perform(get("/dashboard")
                            .header("Authorization", "Bearer " + ILL_FORMED_TOKEN))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /dashboard without Bearer prefix - 401")
        void dashboard_noBearerPrefix_returns401() throws Exception {
            mockMvc.perform(get("/dashboard")
                            .header("Authorization", ILL_FORMED_TOKEN))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /dashboard with empty Authorization header - 401")
        void dashboard_emptyHeader_returns401() throws Exception {
            mockMvc.perform(get("/dashboard")
                            .header("Authorization", ""))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Expired token")
    class ExpiredToken {

        @Test
        @DisplayName("GET /dashboard with expired token - 401")
        void dashboard_expiredToken_returns401() throws Exception {
            mockMvc.perform(get("/dashboard")
                            .header("Authorization", "Bearer " + EXPIRED_TOKEN))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Public endpoints (no token needed)")
    class PublicEndpoints {

        @Test
        @DisplayName("POST /auth/login without token - not 401")
        void login_noToken_doesNotReturn401() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(statusIsNot401());
        }

        @Test
        @DisplayName("POST /auth/forgot-password without token - not 401")
        void forgotPassword_noToken_doesNotReturn401() throws Exception {
            mockMvc.perform(post("/auth/forgot-password")
                            .contentType("application/json")
                            .content("{\"email\":\"test@example.com\"}"))
                    .andExpect(statusIsNot401());
        }

        @Test
        @DisplayName("POST /auth/reset-password without token - not 401")
        void resetPassword_noToken_doesNotReturn401() throws Exception {
            mockMvc.perform(post("/auth/reset-password")
                            .contentType("application/json")
                            .content("{\"token\":\"abc\",\"newPassword\":\"newPassword123\"}"))
                    .andExpect(statusIsNot401());
        }

        @Test
        @DisplayName("GET /schedule without token - not 401")
        void schedule_noToken_doesNotReturn401() throws Exception {
            mockMvc.perform(get("/schedule"))
                    .andExpect(statusIsNot401());
        }
    }
}