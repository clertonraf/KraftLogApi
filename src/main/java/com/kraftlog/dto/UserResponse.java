package com.kraftlog.dto;

import com.kraftlog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private Double weightKg;
    private Double heightCm;
    private User.FitnessGoal fitnessGoal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
