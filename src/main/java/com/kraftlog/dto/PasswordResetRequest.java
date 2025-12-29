package com.kraftlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to reset password with token")
public class PasswordResetRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset token", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "New password", example = "newSecurePassword123")
    private String newPassword;
}
