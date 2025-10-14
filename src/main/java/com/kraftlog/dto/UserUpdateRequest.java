package com.kraftlog.dto;

import com.kraftlog.entity.User;
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
public class UserUpdateRequest {

    private String name;

    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Positive(message = "Weight must be positive")
    private Double weightKg;

    @Positive(message = "Height must be positive")
    private Double heightCm;

    private User.FitnessGoal fitnessGoal;
}
