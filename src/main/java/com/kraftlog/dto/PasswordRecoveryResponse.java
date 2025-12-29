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
@Schema(description = "Response for password recovery request")
public class PasswordRecoveryResponse {

    @Schema(description = "Success message", example = "Password recovery email sent successfully")
    private String message;
}
