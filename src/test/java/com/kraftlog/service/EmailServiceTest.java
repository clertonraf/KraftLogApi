package com.kraftlog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String FROM_EMAIL = "noreply@kraftlog.com";
    private static final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Given
        String toEmail = "user@example.com";
        String token = "test-token-123";
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPasswordResetEmail(toEmail, token);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("Password Reset Request - KraftLog");
        assertThat(sentMessage.getText())
                .contains(FRONTEND_URL + "/reset-password?token=" + token)
                .contains("You have requested to reset your password")
                .contains("This link will expire in 24 hours");
    }

    @Test
    void sendPasswordResetEmail_MailSenderThrowsException() {
        // Given
        String toEmail = "user@example.com";
        String token = "test-token-123";
        
        doThrow(new RuntimeException("SMTP server unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendPasswordResetEmail(toEmail, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send password reset email");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_VerifyEmailContent() {
        // Given
        String toEmail = "test@example.com";
        String token = "unique-token-456";
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPasswordResetEmail(toEmail, token);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String expectedUrl = FRONTEND_URL + "/reset-password?token=" + token;
        
        assertThat(sentMessage.getText()).contains(expectedUrl);
        assertThat(sentMessage.getText()).contains("KraftLog");
        assertThat(sentMessage.getText()).contains("Hello");
    }
}
