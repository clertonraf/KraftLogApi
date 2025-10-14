package com.kraftlog.dto;

import com.kraftlog.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "User's first name", example = "John")
    private String name;

    @NotBlank(message = "Surname is required")
    @Schema(description = "User's last name", example = "Doe")
    private String surname;

    @Schema(description = "User's birth date", example = "1990-01-01")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User's password (min 6 characters)", example = "password123")
    private String password;

    @Schema(description = "User's weight in kilograms", example = "75.5")
    private Double weightKg;

    @Schema(description = "User's height in centimeters", example = "180.0")
    private Double heightCm;

    @NotNull(message = "Fitness goal is required")
    @Schema(description = "User's fitness goal", example = "HYPERTROPHY")
    private User.FitnessGoal fitnessGoal;
}
