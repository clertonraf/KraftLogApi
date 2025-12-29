package com.kraftlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.dto.PasswordRecoveryRequest;
import com.kraftlog.dto.PasswordResetRequest;
import com.kraftlog.dto.RegisterRequest;
import com.kraftlog.entity.PasswordResetToken;
import com.kraftlog.entity.User;
import com.kraftlog.repository.PasswordResetTokenRepository;
import com.kraftlog.repository.UserRepository;
import com.kraftlog.service.IEmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordRecoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private IEmailService emailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .birthDate(LocalDate.of(1990, 1, 1))
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Mock email service to avoid actual email sending
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @AfterEach
    void tearDown() {
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void passwordRecovery_Success() throws Exception {
        // Given
        PasswordRecoveryRequest request = PasswordRecoveryRequest.builder()
                .email(testUser.getEmail())
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password recovery email sent successfully"));

        // Verify token was created
        assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
        PasswordResetToken token = passwordResetTokenRepository.findAll().get(0);
        assertThat(token.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());

        // Verify email was sent
        verify(emailService).sendPasswordResetEmail(eq(testUser.getEmail()), anyString());
    }

    @Test
    void passwordRecovery_UserNotFound() throws Exception {
        // Given
        PasswordRecoveryRequest request = PasswordRecoveryRequest.builder()
                .email("nonexistent@example.com")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));

        // Verify no token was created
        assertThat(passwordResetTokenRepository.findAll()).isEmpty();

        // Verify no email was sent
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void passwordRecovery_InvalidEmail() throws Exception {
        // Given
        PasswordRecoveryRequest request = PasswordRecoveryRequest.builder()
                .email("invalid-email")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void passwordRecovery_DeletesOldTokens() throws Exception {
        // Given - Create an old token
        PasswordResetToken oldToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(oldToken);

        PasswordRecoveryRequest request = PasswordRecoveryRequest.builder()
                .email(testUser.getEmail())
                .build();

        // When
        mockMvc.perform(post("/api/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - Only the new token should exist
        assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
        PasswordResetToken newToken = passwordResetTokenRepository.findAll().get(0);
        assertThat(newToken.getToken()).isNotEqualTo(oldToken.getToken());
    }

    @Test
    void passwordReset_Success() throws Exception {
        // Given - Create a valid token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String newPassword = "newSecurePassword123";
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(tokenValue)
                .newPassword(newPassword)
                .build();

        // When
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));

        // Then - Verify password was updated
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();

        // Verify token was marked as used
        PasswordResetToken usedToken = passwordResetTokenRepository.findByToken(tokenValue).orElseThrow();
        assertThat(usedToken.isUsed()).isTrue();
    }

    @Test
    void passwordReset_InvalidToken() throws Exception {
        // Given
        PasswordResetRequest request = PasswordResetRequest.builder()
                .token("invalid-token")
                .newPassword("newPassword123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid password reset token")));
    }

    @Test
    void passwordReset_ExpiredToken() throws Exception {
        // Given - Create an expired token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(tokenValue)
                .newPassword("newPassword123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Password reset token has expired")));
    }

    @Test
    void passwordReset_AlreadyUsedToken() throws Exception {
        // Given - Create a used token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(true)
                .build();
        passwordResetTokenRepository.save(resetToken);

        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(tokenValue)
                .newPassword("newPassword123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Password reset token has already been used")));
    }

    @Test
    void passwordReset_ShortPassword() throws Exception {
        // Given
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        PasswordResetRequest request = PasswordResetRequest.builder()
                .token(tokenValue)
                .newPassword("123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endToEndPasswordRecoveryFlow() throws Exception {
        // Step 1: Request password recovery
        PasswordRecoveryRequest recoveryRequest = PasswordRecoveryRequest.builder()
                .email(testUser.getEmail())
                .build();

        mockMvc.perform(post("/api/auth/password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryRequest)))
                .andExpect(status().isOk());

        // Get the generated token
        PasswordResetToken token = passwordResetTokenRepository.findAll().get(0);

        // Step 2: Reset password using the token
        String newPassword = "brandNewPassword456";
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .token(token.getToken())
                .newPassword(newPassword)
                .build();

        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        // Step 3: Verify user can login with new password
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();

        // Step 4: Verify token cannot be reused
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already been used")));
    }
}
