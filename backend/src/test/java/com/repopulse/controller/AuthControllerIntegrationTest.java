package com.repopulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repopulse.dto.request.LoginRequest;
import com.repopulse.dto.request.RefreshTokenRequest;
import com.repopulse.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 * Uses an in-memory H2 database (via {@code application-test.yml}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired MockMvc    mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /register – valid request returns 201 with tokens")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("SecurePass1!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber());
    }

    @Test
    @DisplayName("POST /register – duplicate username returns 409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setEmail("bob@example.com");
        req.setPassword("SecurePass1!");

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second registration – same username
        req.setEmail("bob2@example.com");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /register – invalid payload returns 400 with field errors")
    void register_invalidPayload_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();  // all fields blank

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isMap());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login – valid credentials returns 200 with tokens")
    void login_validCredentials_returns200() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("charlie");
        reg.setEmail("charlie@example.com");
        reg.setPassword("MyPassword99!");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Now login
        LoginRequest login = new LoginRequest();
        login.setUsername("charlie");
        login.setPassword("MyPassword99!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /login – wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        // Register
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("dave");
        reg.setEmail("dave@example.com");
        reg.setPassword("RealPassword1!");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("dave");
        login.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /refresh – valid refresh token returns new access token")
    void refresh_validToken_returnsNewAccessToken() throws Exception {
        // Register and capture tokens
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("eve");
        reg.setEmail("eve@example.com");
        reg.setPassword("EvePass123!");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andReturn();

        String body         = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(body).at("/data/refreshToken").asText();

        // Use the refresh token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    @DisplayName("POST /refresh – access token rejected as refresh token returns 400")
    void refresh_accessTokenAsRefresh_returns400() throws Exception {
        // Register and capture tokens
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("frank");
        reg.setEmail("frank@example.com");
        reg.setPassword("FrankPass123!");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andReturn();

        String body        = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(body).at("/data/accessToken").asText();

        // Attempt to use the access token as a refresh token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(accessToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── /me ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /me – valid access token returns user profile")
    void me_withValidToken_returnsProfile() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("grace");
        reg.setEmail("grace@example.com");
        reg.setPassword("GracePass1!");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andReturn();

        String body        = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(body).at("/data/accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("grace"))
                .andExpect(jsonPath("$.data.email").value("grace@example.com"));
    }

    @Test
    @DisplayName("GET /me – no token returns 401")
    void me_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
