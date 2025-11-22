package com.kraftlog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Positive(message = "Weight must be positive")
    private Double weightKg;

    @Positive(message = "Height must be positive")
    private Double heightCm;
}
