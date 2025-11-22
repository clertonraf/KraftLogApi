package com.kraftlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.dto.LoginRequest;
import com.kraftlog.dto.RegisterRequest;
import com.kraftlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUser() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 5, 15))
                .weightKg(75.5)
                .heightCm(180.0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.name").value("John"))
                .andExpect(jsonPath("$.user.surname").value("Doe"))
                .andExpect(jsonPath("$.user.id").exists());
    }

    @Test
    @DisplayName("Should reject registration with existing email")
    void shouldRejectDuplicateEmail() throws Exception {
        // Given - First register a user
        RegisterRequest firstRequest = RegisterRequest.builder()
                .name("John")
                .surname("Doe")
                .email("duplicate@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Try to register with same email
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .name("Jane")
                .surname("Smith")
                .email("duplicate@example.com")
                .password("different123")
                .birthDate(LocalDate.of(1992, 1, 1))
                .weightKg(65.0)
                .heightCm(170.0)
                .build();

        // Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void shouldRejectInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .surname("Doe")
                .email("invalid-email")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void shouldRejectShortPassword() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .password("12345") // Only 5 characters
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Should reject registration with missing required fields")
    void shouldRejectMissingFields() throws Exception {
        // Given - Missing name and surname
        String jsonRequest = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginWithValidCredentials() throws Exception {
        // Given - First register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Login")
                .surname("Test")
                .email("login@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // When - Login with correct credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@example.com")
                .password("password123")
                .build();

        // Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.user.name").value("Login"));
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void shouldRejectWrongPassword() throws Exception {
        // Given - First register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .password("correctPassword")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // When - Login with wrong password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        // Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with non-existent email")
    void shouldRejectNonExistentEmail() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with invalid email format")
    void shouldRejectLoginWithInvalidEmail() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should register user with minimal required fields")
    void shouldRegisterWithMinimalFields() throws Exception {
        // Given - Only required fields
        RegisterRequest request = RegisterRequest.builder()
                .name("Minimal")
                .surname("User")
                .email("minimal@example.com")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("minimal@example.com"))
                .andExpect(jsonPath("$.user.name").value("Minimal"));
    }

    @Test
    @DisplayName("Should handle concurrent registrations for different users")
    void shouldHandleConcurrentRegistrations() throws Exception {
        // Given
        RegisterRequest request1 = RegisterRequest.builder()
                .name("User")
                .surname("One")
                .email("user1@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .name("User")
                .surname("Two")
                .email("user2@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1991, 1, 1))
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
    }
}
