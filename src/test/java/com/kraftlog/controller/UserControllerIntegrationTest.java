package com.kraftlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.TestDataBuilder;
import com.kraftlog.dto.UserCreateRequest;
import com.kraftlog.dto.UserUpdateRequest;
import com.kraftlog.entity.User;
import com.kraftlog.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .password("password123")
                .weightKg(75.5)
                .heightCm(180.0)
                .fitnessGoal(User.FitnessGoal.HYPERTROPHY)
                .build();

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.weightKg").value(75.5))
                .andExpect(jsonPath("$.heightCm").value(180.0))
                .andExpect(jsonPath("$.fitnessGoal").value("HYPERTROPHY"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserWithExistingEmail() throws Exception {
        // Given
        User existingUser = TestDataBuilder.defaultUser().build();
        userRepository.save(existingUser);

        UserCreateRequest request = UserCreateRequest.builder()
                .name("Jane")
                .surname("Doe")
                .birthDate(LocalDate.of(1992, 1, 1))
                .email("john.doe@example.com") // Same email
                .password("password456")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void shouldReturnValidationErrorWhenCreatingUserWithInvalidData() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .name("") // Invalid: empty name
                .surname("Doe")
                .email("invalid-email") // Invalid: not a valid email
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        // Given
        User user1 = TestDataBuilder.defaultUser().build();
        User user2 = TestDataBuilder.defaultUser()
                .email("jane.doe@example.com")
                .name("Jane")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", "550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void shouldGetUserByEmail() throws Exception {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/users/email/{email}", "john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .weightKg(80.0)
                .heightCm(182.0)
                .fitnessGoal(User.FitnessGoal.STRENGTH)
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.weightKg").value(80.0))
                .andExpect(jsonPath("$.heightCm").value(182.0))
                .andExpect(jsonPath("$.fitnessGoal").value("STRENGTH"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/{id}", "550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound());
    }
}
