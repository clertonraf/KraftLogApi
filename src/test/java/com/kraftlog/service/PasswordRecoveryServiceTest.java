package com.kraftlog.service;

import com.kraftlog.entity.PasswordResetToken;
import com.kraftlog.entity.User;
import com.kraftlog.exception.BadRequestException;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.PasswordResetTokenRepository;
import com.kraftlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test")
                .surname("User")
                .password("encodedPassword")
                .build();
    }

    @Test
    void initiatePasswordRecovery_Success() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        passwordRecoveryService.initiatePasswordRecovery(testUser.getEmail());

        // Then
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(passwordResetTokenRepository).deleteByUser(testUser);
        
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(savedToken.isUsed()).isFalse();
        
        verify(emailService).sendPasswordResetEmail(eq(testUser.getEmail()), anyString());
    }

    @Test
    void initiatePasswordRecovery_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> passwordRecoveryService.initiatePasswordRecovery(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_Success() {
        // Given
        String token = "valid-token";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword";

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        passwordRecoveryService.resetPassword(token, newPassword);

        // Then
        verify(passwordResetTokenRepository).findByToken(token);
        verify(passwordEncoder).encode(newPassword);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);
        
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isUsed()).isTrue();
    }

    @Test
    void resetPassword_InvalidToken() {
        // Given
        String token = "invalid-token";
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> passwordRecoveryService.resetPassword(token, "newPassword"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid password reset token");

        verify(passwordResetTokenRepository).findByToken(token);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_TokenAlreadyUsed() {
        // Given
        String token = "used-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When & Then
        assertThatThrownBy(() -> passwordRecoveryService.resetPassword(token, "newPassword"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password reset token has already been used");

        verify(passwordResetTokenRepository).findByToken(token);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_TokenExpired() {
        // Given
        String token = "expired-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When & Then
        assertThatThrownBy(() -> passwordRecoveryService.resetPassword(token, "newPassword"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password reset token has expired");

        verify(passwordResetTokenRepository).findByToken(token);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void cleanupExpiredTokens_Success() {
        // When
        passwordRecoveryService.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(passwordResetTokenRepository).deleteByExpiryDateBefore(dateCaptor.capture());
        
        assertThat(dateCaptor.getValue()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(dateCaptor.getValue()).isAfter(LocalDateTime.now().minusSeconds(1));
    }
}
