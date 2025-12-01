package com.mindsoccer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import com.mindsoccer.protocol.dto.request.LoginRequest;
import com.mindsoccer.protocol.dto.request.RefreshTokenRequest;
import com.mindsoccer.protocol.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUser() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "FR"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.handle").value("newuser"))
                    .andExpect(jsonPath("$.data.user.email").value("newuser@example.com"));
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void shouldRejectDuplicateEmail() throws Exception {
            // Create existing user
            UserEntity existing = new UserEntity("existing", "test@example.com", passwordEncoder.encode("pass"));
            userRepository.save(existing);

            RegisterRequest request = new RegisterRequest(
                    "newuser",
                    "test@example.com",
                    "Password123!",
                    "FR"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should reject duplicate handle")
        void shouldRejectDuplicateHandle() throws Exception {
            UserEntity existing = new UserEntity("existinguser", "existing@example.com", passwordEncoder.encode("pass"));
            userRepository.save(existing);

            RegisterRequest request = new RegisterRequest(
                    "existinguser",
                    "new@example.com",
                    "Password123!",
                    "FR"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmail() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "testuser",
                    "invalid-email",
                    "Password123!",
                    "FR"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject empty handle")
        void shouldRejectEmptyHandle() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "",
                    "test@example.com",
                    "Password123!",
                    "FR"
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        void createTestUser() {
            UserEntity user = new UserEntity("testuser", "test@example.com", passwordEncoder.encode("Password123!"));
            userRepository.save(user);
        }

        @Test
        @DisplayName("Should login with valid email")
        void shouldLoginWithEmail() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "Password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.handle").value("testuser"));
        }

        @Test
        @DisplayName("Should login with valid handle")
        void shouldLoginWithHandle() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "Password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should reject invalid password")
        void shouldRejectInvalidPassword() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "WrongPassword!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should reject non-existent user")
        void shouldRejectNonExistentUser() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "Password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            // First register to get tokens
            RegisterRequest registerRequest = new RegisterRequest(
                    "refreshtest",
                    "refresh@example.com",
                    "Password123!",
                    "FR"
            );

            MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseJson = registerResult.getResponse().getContentAsString();
            String refreshToken = objectMapper.readTree(responseJson)
                    .path("data").path("refreshToken").asText();

            // Now refresh
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void shouldRejectInvalidRefreshToken() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid.refresh.token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
