package com.kraftlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.dto.ChangePasswordRequest;
import com.kraftlog.dto.LoginRequest;
import com.kraftlog.dto.RegisterRequest;
import com.kraftlog.entity.User;
import com.kraftlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String regularUserToken;
    private UUID regularUserId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Create admin user
        User admin = User.builder()
                .name("Admin")
                .surname("User")
                .email("admin@kraftlog.com")
                .password(passwordEncoder.encode("admin123"))
                .birthDate(LocalDate.of(1980, 1, 1))
                .weightKg(80.0)
                .heightCm(180.0)
                .isAdmin(true)
                .build();
        userRepository.save(admin);

        // Get admin token
        LoginRequest adminLogin = LoginRequest.builder()
                .email("admin@kraftlog.com")
                .password("admin123")
                .build();

        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .get("token").asText();

        // Create regular user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Regular")
                .surname("User")
                .email("regular@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        regularUserToken = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("token").asText();
        regularUserId = UUID.fromString(objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("user").get("id").asText());
    }

    @Test
    @DisplayName("Admin should delete user successfully")
    void adminShouldDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/admin/users/{userId}", regularUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        assertThat(userRepository.findById(regularUserId)).isEmpty();
    }

    @Test
    @DisplayName("Admin should fail to delete non-existent user")
    void adminShouldFailToDeleteNonExistentUser() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/admin/users/{userId}", nonExistentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    @DisplayName("Regular user should not delete another user")
    void regularUserShouldNotDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/admin/users/{userId}", regularUserId)
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request should fail")
    void unauthenticatedRequestShouldFail() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/admin/users/{userId}", regularUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should change user password successfully")
    void adminShouldChangeUserPassword() throws Exception {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .newPassword("newPassword123")
                .build();

        // When
        mockMvc.perform(put("/api/admin/users/{userId}/password", regularUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regularUserId.toString()))
                .andExpect(jsonPath("$.email").value("regular@example.com"));

        // Verify password was changed by logging in with new password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("regular@example.com")
                .password("newPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Admin should fail to change password for non-existent user")
    void adminShouldFailToChangePasswordForNonExistentUser() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .newPassword("newPassword123")
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/users/{userId}/password", nonExistentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    @DisplayName("Regular user should not change another user's password")
    void regularUserShouldNotChangePassword() throws Exception {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .newPassword("newPassword123")
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/users/{userId}/password", regularUserId)
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should clean database successfully")
    void adminShouldCleanDatabase() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/database/clean")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value(containsString("Database cleaned successfully")));
    }

    @Test
    @DisplayName("Admin should delete all entities successfully")
    void adminShouldDeleteAllEntities() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/database/delete-entities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value(containsString("All entities deleted successfully")));
    }

    @Test
    @DisplayName("Regular user should not clean database")
    void regularUserShouldNotCleanDatabase() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/database/clean")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Regular user should not delete all entities")
    void regularUserShouldNotDeleteAllEntities() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/admin/database/delete-entities")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject password change with empty password")
    void shouldRejectEmptyPassword() throws Exception {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .newPassword("")
                .build();

        // When & Then
        mockMvc.perform(put("/api/admin/users/{userId}/password", regularUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject password change with null password")
    void shouldRejectNullPassword() throws Exception {
        // Given
        String jsonRequest = "{\"newPassword\": null}";

        // When & Then
        mockMvc.perform(put("/api/admin/users/{userId}/password", regularUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}
