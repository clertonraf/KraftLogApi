package com.kraftlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for password reset")
public class PasswordResetResponse {

    @Schema(description = "Success message", example = "Password has been reset successfully")
    private String message;
}
